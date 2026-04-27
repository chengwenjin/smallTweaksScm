package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.entity.OaApproval;
import com.baserbac.scm.entity.PurchaseOrder;
import com.baserbac.scm.entity.PurchaseRequest;
import com.baserbac.scm.mapper.OaApprovalMapper;
import com.baserbac.scm.mapper.PurchaseOrderMapper;
import com.baserbac.scm.mapper.PurchaseRequestMapper;
import com.baserbac.scm.vo.OaApprovalVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OaApprovalService {

    private final OaApprovalMapper approvalMapper;
    private final PurchaseRequestMapper requestMapper;
    private final PurchaseOrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public PageResult<OaApprovalVO> pageApprovals(Integer sourceType, String approvalStatus, int pageNum, int pageSize) {
        Page<OaApproval> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<OaApproval> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(sourceType != null, OaApproval::getSourceType, sourceType)
               .eq(approvalStatus != null, OaApproval::getApprovalStatus, approvalStatus)
               .orderByDesc(OaApproval::getCreateTime);

        Page<OaApproval> result = approvalMapper.selectPage(page, wrapper);

        List<OaApprovalVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        return PageResult.of(
            result.getTotal(),
            voList,
            (long) result.getCurrent(),
            (long) result.getSize()
        );
    }

    public OaApprovalVO getApprovalById(Long id) {
        OaApproval approval = approvalMapper.selectById(id);
        if (approval == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(approval);
    }

    @Transactional(rollbackFor = Exception.class)
    public OaApproval submitToOA(Integer sourceType, Long sourceId, String sourceNo, String approvalTitle, String operator) {
        OaApproval existingApproval = approvalMapper.selectOne(
            new LambdaQueryWrapper<OaApproval>()
                .eq(OaApproval::getSourceType, sourceType)
                .eq(OaApproval::getSourceId, sourceId)
                .in(OaApproval::getApprovalStatus, "DRAFT", "APPROVING")
                .last("LIMIT 1")
        );

        if (existingApproval != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该单据已有审批流程进行中");
        }

        String approvalNo = generateApprovalNo();

        OaApproval approval = new OaApproval();
        approval.setApprovalNo(approvalNo);
        approval.setSourceType(sourceType);
        approval.setSourceId(sourceId);
        approval.setSourceNo(sourceNo);
        approval.setApprovalTitle(approvalTitle);
        approval.setCurrentApproverId("1");
        approval.setCurrentApproverName("审批专员");
        approval.setApprovalStatus("APPROVING");
        approval.setSubmitTime(LocalDateTime.now());
        approval.setCreateBy(operator);
        approval.setCreateTime(LocalDateTime.now());

        approvalMapper.insert(approval);

        updateSourceStatus(sourceType, sourceId, "APPROVING", operator);

        log.info("提交OA审批成功，审批单编号：{}，来源类型：{}，来源ID：{}", approvalNo, sourceType, sourceId);
        return approval;
    }

    @Transactional(rollbackFor = Exception.class)
    public void processApproval(Long approvalId, String action, String remark, String operator) {
        OaApproval approval = approvalMapper.selectById(approvalId);
        if (approval == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (!"APPROVING".equals(approval.getApprovalStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "当前审批状态不允许操作");
        }

        String newStatus;
        if ("APPROVE".equals(action)) {
            newStatus = "APPROVED";
            approval.setApprovalTime(LocalDateTime.now());
        } else if ("REJECT".equals(action)) {
            newStatus = "REJECTED";
            approval.setApprovalTime(LocalDateTime.now());
        } else if ("TRANSFER".equals(action)) {
            newStatus = "APPROVING";
            approval.setCurrentApproverName("总监");
        } else {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的审批操作");
        }

        approval.setApprovalStatus(newStatus);
        approval.setApprovalRemark(remark);

        ObjectNode historyNode = objectMapper.createObjectNode();
        historyNode.put("operator", operator);
        historyNode.put("action", action);
        historyNode.put("remark", remark);
        historyNode.put("time", LocalDateTime.now().toString());

        String existingHistory = approval.getApprovalHistory();
        if (existingHistory == null || existingHistory.isEmpty()) {
            approval.setApprovalHistory(historyNode.toString());
        } else {
            approval.setApprovalHistory(existingHistory + ";" + historyNode.toString());
        }

        approval.setUpdateBy(operator);
        approval.setUpdateTime(LocalDateTime.now());

        approvalMapper.updateById(approval);

        updateSourceStatus(approval.getSourceType(), approval.getSourceId(), newStatus, operator);

        if ("APPROVED".equals(newStatus) && approval.getSourceType() == 1) {
            generatePurchaseOrder(approval, operator);
        }

        log.info("处理审批完成，审批单ID：{}，操作：{}", approvalId, action);
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdrawApproval(Long approvalId, String operator) {
        OaApproval approval = approvalMapper.selectById(approvalId);
        if (approval == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (!"APPROVING".equals(approval.getApprovalStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "当前审批状态不允许撤回");
        }

        approval.setApprovalStatus("WITHDRAWN");
        approval.setUpdateBy(operator);
        approval.setUpdateTime(LocalDateTime.now());

        approvalMapper.updateById(approval);

        updateSourceStatus(approval.getSourceType(), approval.getSourceId(), "WITHDRAWN", operator);

        log.info("撤回审批，审批单ID：{}", approvalId);
    }

    private void updateSourceStatus(Integer sourceType, Long sourceId, String approvalStatus, String operator) {
        if (sourceType == 1) {
            PurchaseRequest request = requestMapper.selectById(sourceId);
            if (request != null) {
                request.setApprovalStatus(approvalStatus);
                if ("APPROVED".equals(approvalStatus)) {
                    request.setStatus(3);
                } else if ("REJECTED".equals(approvalStatus)) {
                    request.setStatus(4);
                }
                request.setUpdateBy(operator);
                request.setUpdateTime(LocalDateTime.now());
                requestMapper.updateById(request);
            }
        }
    }

    private void generatePurchaseOrder(OaApproval approval, String operator) {
        if (approval.getSourceType() == 1 && approval.getSourceId() != null) {
            PurchaseRequest request = requestMapper.selectById(approval.getSourceId());
            if (request != null) {
                String orderNo = generateOrderNo();

                PurchaseOrder order = new PurchaseOrder();
                order.setOrderNo(orderNo);
                order.setOrderName(request.getReqTitle() + "-采购订单");
                order.setSourceRequestId(request.getId());
                order.setSourceRequestNo(request.getReqNo());
                order.setApprovalId(approval.getId());
                order.setApprovalNo(approval.getApprovalNo());
                order.setOrderType(1);
                order.setOrderDate(LocalDate.now());
                order.setExpectedDeliveryDate(request.getRequiredDate());
                order.setTotalAmount(request.getTotalAmount());
                order.setDeliveryAddress(request.getDeliveryAddress());
                order.setContactPerson(request.getReqPerson());
                order.setContactPhone(request.getReqPhone());
                order.setStatus(0);
                order.setCreateBy(operator);
                order.setCreateTime(LocalDateTime.now());

                orderMapper.insert(order);

                request.setGeneratedOrderId(order.getId());
                request.setStatus(5);
                requestMapper.updateById(request);

                log.info("根据采购申请生成采购订单，订单号：{}", orderNo);
            }
        }
    }

    private String generateApprovalNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "OA" + dateStr;

        LambdaQueryWrapper<OaApproval> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(OaApproval::getApprovalNo, prefix)
               .orderByDesc(OaApproval::getApprovalNo)
               .last("LIMIT 1");

        OaApproval lastApproval = approvalMapper.selectOne(wrapper);

        int sequence = 1;
        if (lastApproval != null && lastApproval.getApprovalNo() != null) {
            String lastNo = lastApproval.getApprovalNo();
            int lastSeq = Integer.parseInt(lastNo.substring(lastNo.length() - 4));
            sequence = lastSeq + 1;
        }

        return prefix + String.format("%04d", sequence);
    }

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "PO" + dateStr;

        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(PurchaseOrder::getOrderNo, prefix)
               .orderByDesc(PurchaseOrder::getOrderNo)
               .last("LIMIT 1");

        PurchaseOrder lastOrder = orderMapper.selectOne(wrapper);

        int sequence = 1;
        if (lastOrder != null && lastOrder.getOrderNo() != null) {
            String lastNo = lastOrder.getOrderNo();
            int lastSeq = Integer.parseInt(lastNo.substring(lastNo.length() - 4));
            sequence = lastSeq + 1;
        }

        return prefix + String.format("%04d", sequence);
    }

    private OaApprovalVO convertToVO(OaApproval approval) {
        return OaApprovalVO.builder()
            .id(approval.getId())
            .approvalNo(approval.getApprovalNo())
            .sourceType(approval.getSourceType())
            .sourceTypeName(getSourceTypeName(approval.getSourceType()))
            .sourceId(approval.getSourceId())
            .sourceNo(approval.getSourceNo())
            .approvalTitle(approval.getApprovalTitle())
            .currentApproverId(approval.getCurrentApproverId())
            .currentApproverName(approval.getCurrentApproverName())
            .approvalStatus(approval.getApprovalStatus())
            .approvalStatusName(getApprovalStatusName(approval.getApprovalStatus()))
            .submitTime(approval.getSubmitTime())
            .approvalTime(approval.getApprovalTime())
            .approvalRemark(approval.getApprovalRemark())
            .approvalHistory(approval.getApprovalHistory())
            .remark(approval.getRemark())
            .createTime(approval.getCreateTime())
            .updateTime(approval.getUpdateTime())
            .build();
    }

    private String getSourceTypeName(Integer sourceType) {
        if (sourceType == null) return "未知";
        switch (sourceType) {
            case 1: return "采购申请";
            case 2: return "采购计划";
            case 3: return "采购订单";
            default: return "未知";
        }
    }

    private String getApprovalStatusName(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "DRAFT": return "草稿";
            case "APPROVING": return "审批中";
            case "APPROVED": return "审批通过";
            case "REJECTED": return "审批拒绝";
            case "WITHDRAWN": return "已撤回";
            default: return "未知";
        }
    }
}
