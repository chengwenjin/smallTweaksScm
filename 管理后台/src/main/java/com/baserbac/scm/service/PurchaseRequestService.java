package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.PurchaseRequestCreateDTO;
import com.baserbac.scm.dto.PurchaseRequestItemDTO;
import com.baserbac.scm.dto.PurchaseRequestQueryDTO;
import com.baserbac.scm.entity.PurchaseRequest;
import com.baserbac.scm.entity.PurchaseRequestItem;
import com.baserbac.scm.mapper.PurchaseRequestItemMapper;
import com.baserbac.scm.mapper.PurchaseRequestMapper;
import com.baserbac.scm.vo.PurchaseRequestItemVO;
import com.baserbac.scm.vo.PurchaseRequestVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseRequestService {

    private final PurchaseRequestMapper requestMapper;
    private final PurchaseRequestItemMapper itemMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public PageResult<PurchaseRequestVO> pageRequests(PurchaseRequestQueryDTO queryDTO) {
        Page<PurchaseRequest> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<PurchaseRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getReqNo() != null, PurchaseRequest::getReqNo, queryDTO.getReqNo())
               .like(queryDTO.getReqTitle() != null, PurchaseRequest::getReqTitle, queryDTO.getReqTitle())
               .like(queryDTO.getReqDept() != null, PurchaseRequest::getReqDept, queryDTO.getReqDept())
               .like(queryDTO.getReqPerson() != null, PurchaseRequest::getReqPerson, queryDTO.getReqPerson())
               .eq(queryDTO.getUrgency() != null, PurchaseRequest::getUrgency, queryDTO.getUrgency())
               .eq(queryDTO.getStatus() != null, PurchaseRequest::getStatus, queryDTO.getStatus())
               .eq(queryDTO.getApprovalStatus() != null, PurchaseRequest::getApprovalStatus, queryDTO.getApprovalStatus())
               .orderByDesc(PurchaseRequest::getCreateTime);

        Page<PurchaseRequest> result = requestMapper.selectPage(page, wrapper);

        List<Long> requestIds = result.getRecords().stream()
            .map(PurchaseRequest::getId)
            .collect(Collectors.toList());

        Map<Long, List<PurchaseRequestItem>> itemMap = null;
        if (!requestIds.isEmpty()) {
            List<PurchaseRequestItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<PurchaseRequestItem>()
                    .in(PurchaseRequestItem::getRequestId, requestIds)
                    .orderByAsc(PurchaseRequestItem::getId)
            );
            itemMap = items.stream()
                .collect(Collectors.groupingBy(PurchaseRequestItem::getRequestId));
        }

        Map<Long, List<PurchaseRequestItem>> finalItemMap = itemMap;
        List<PurchaseRequestVO> voList = result.getRecords().stream()
            .map(r -> convertToVO(r, finalItemMap))
            .collect(Collectors.toList());

        return PageResult.of(
            result.getTotal(),
            voList,
            (long) result.getCurrent(),
            (long) result.getSize()
        );
    }

    public PurchaseRequestVO getRequestById(Long id) {
        PurchaseRequest request = requestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        List<PurchaseRequestItem> items = itemMapper.selectList(
            new LambdaQueryWrapper<PurchaseRequestItem>()
                .eq(PurchaseRequestItem::getRequestId, id)
                .orderByAsc(PurchaseRequestItem::getId)
        );

        Map<Long, List<PurchaseRequestItem>> itemMap = items.stream()
            .collect(Collectors.groupingBy(PurchaseRequestItem::getRequestId));

        return convertToVO(request, itemMap);
    }

    @Transactional(rollbackFor = Exception.class)
    public PurchaseRequest createRequest(PurchaseRequestCreateDTO createDTO, String operator) {
        String reqNo = generateReqNo();

        PurchaseRequest request = new PurchaseRequest();
        request.setReqNo(reqNo);
        request.setReqTitle(createDTO.getReqTitle());
        request.setReqDept(createDTO.getReqDept() != null ? createDTO.getReqDept() : "采购部");
        request.setReqPerson(createDTO.getReqPerson() != null ? createDTO.getReqPerson() : operator);
        request.setReqPhone(createDTO.getReqPhone());
        request.setRequiredDate(createDTO.getRequiredDate());
        request.setDeliveryAddress(createDTO.getDeliveryAddress());
        request.setUrgency(createDTO.getUrgency() != null ? createDTO.getUrgency() : 1);
        request.setBudgetSource(createDTO.getBudgetSource());
        request.setDescription(createDTO.getDescription());
        request.setStatus(0);
        request.setApprovalStatus("DRAFT");
        request.setRemark(createDTO.getRemark());
        request.setCreateBy(operator);
        request.setCreateTime(LocalDateTime.now());

        requestMapper.insert(request);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (createDTO.getItems() != null && !createDTO.getItems().isEmpty()) {
            for (PurchaseRequestItemDTO itemDTO : createDTO.getItems()) {
                PurchaseRequestItem item = new PurchaseRequestItem();
                item.setRequestId(request.getId());
                item.setMaterialCode(itemDTO.getMaterialCode());
                item.setMaterialName(itemDTO.getMaterialName());
                item.setMaterialSpec(itemDTO.getMaterialSpec());
                item.setMaterialUnit(itemDTO.getMaterialUnit());
                item.setMaterialCategory(itemDTO.getMaterialCategory());
                item.setQuantity(itemDTO.getQuantity());
                item.setUnitPrice(itemDTO.getUnitPrice());

                if (itemDTO.getQuantity() != null && itemDTO.getUnitPrice() != null) {
                    item.setTotalPrice(itemDTO.getQuantity().multiply(itemDTO.getUnitPrice()));
                    totalAmount = totalAmount.add(item.getTotalPrice());
                }

                item.setRemark(itemDTO.getRemark());
                item.setCreateBy(operator);
                item.setCreateTime(LocalDateTime.now());

                itemMapper.insert(item);
            }
        }

        request.setTotalAmount(totalAmount);
        requestMapper.updateById(request);

        log.info("创建采购申请单成功，编号：{}", reqNo);
        return request;
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitRequest(Long id, String operator) {
        PurchaseRequest request = requestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (request.getStatus() != 0 && request.getStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "当前状态不允许提交");
        }

        Long itemCount = itemMapper.selectCount(
            new LambdaQueryWrapper<PurchaseRequestItem>()
                .eq(PurchaseRequestItem::getRequestId, id)
        );

        if (itemCount == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "请添加采购明细后再提交");
        }

        request.setStatus(2);
        request.setApprovalStatus("APPROVING");
        request.setSubmitTime(LocalDateTime.now());
        request.setCurrentApprover("审批专员");
        request.setUpdateBy(operator);
        request.setUpdateTime(LocalDateTime.now());

        requestMapper.updateById(request);

        log.info("提交采购申请单，ID：{}", id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRequestStatus(Long id, Integer status, String approvalStatus, String remark, String operator) {
        PurchaseRequest request = requestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (status != null) {
            request.setStatus(status);
        }
        if (approvalStatus != null) {
            request.setApprovalStatus(approvalStatus);
        }
        if (remark != null) {
            request.setApprovalRemark(remark);
        }
        request.setUpdateBy(operator);
        request.setUpdateTime(LocalDateTime.now());

        if ("APPROVED".equals(approvalStatus)) {
            request.setApprovalTime(LocalDateTime.now());
            request.setStatus(3);
        } else if ("REJECTED".equals(approvalStatus)) {
            request.setStatus(4);
        }

        requestMapper.updateById(request);
    }

    private String generateReqNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "PR" + dateStr;

        LambdaQueryWrapper<PurchaseRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(PurchaseRequest::getReqNo, prefix)
               .orderByDesc(PurchaseRequest::getReqNo)
               .last("LIMIT 1");

        PurchaseRequest lastRequest = requestMapper.selectOne(wrapper);

        int sequence = 1;
        if (lastRequest != null && lastRequest.getReqNo() != null) {
            String lastNo = lastRequest.getReqNo();
            int lastSeq = Integer.parseInt(lastNo.substring(lastNo.length() - 4));
            sequence = lastSeq + 1;
        }

        return prefix + String.format("%04d", sequence);
    }

    private PurchaseRequestVO convertToVO(PurchaseRequest request, Map<Long, List<PurchaseRequestItem>> itemMap) {
        PurchaseRequestVO vo = PurchaseRequestVO.builder()
            .id(request.getId())
            .reqNo(request.getReqNo())
            .reqTitle(request.getReqTitle())
            .reqDept(request.getReqDept())
            .reqPerson(request.getReqPerson())
            .reqPhone(request.getReqPhone())
            .requiredDate(request.getRequiredDate())
            .deliveryAddress(request.getDeliveryAddress())
            .urgency(request.getUrgency())
            .urgencyName(getUrgencyName(request.getUrgency()))
            .totalAmount(request.getTotalAmount())
            .budgetSource(request.getBudgetSource())
            .description(request.getDescription())
            .status(request.getStatus())
            .statusName(getStatusName(request.getStatus()))
            .approvalStatus(request.getApprovalStatus())
            .currentApprover(request.getCurrentApprover())
            .submitTime(request.getSubmitTime())
            .approvalTime(request.getApprovalTime())
            .approvalRemark(request.getApprovalRemark())
            .generatedOrderId(request.getGeneratedOrderId())
            .remark(request.getRemark())
            .createBy(request.getCreateBy())
            .createTime(request.getCreateTime())
            .updateTime(request.getUpdateTime())
            .build();

        if (itemMap != null && itemMap.containsKey(request.getId())) {
            List<PurchaseRequestItem> items = itemMap.get(request.getId());
            List<PurchaseRequestItemVO> itemVOs = items.stream()
                .map(this::convertItemToVO)
                .collect(Collectors.toList());
            vo.setItems(itemVOs);
        }

        return vo;
    }

    private PurchaseRequestItemVO convertItemToVO(PurchaseRequestItem item) {
        return PurchaseRequestItemVO.builder()
            .id(item.getId())
            .requestId(item.getRequestId())
            .materialCode(item.getMaterialCode())
            .materialName(item.getMaterialName())
            .materialSpec(item.getMaterialSpec())
            .materialUnit(item.getMaterialUnit())
            .materialCategory(item.getMaterialCategory())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalPrice(item.getTotalPrice())
            .remark(item.getRemark())
            .createTime(item.getCreateTime())
            .build();
    }

    private String getUrgencyName(Integer urgency) {
        if (urgency == null) return "未知";
        switch (urgency) {
            case 1: return "普通";
            case 2: return "紧急";
            case 3: return "特急";
            default: return "未知";
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "草稿";
            case 1: return "待提交";
            case 2: return "审批中";
            case 3: return "审批通过";
            case 4: return "审批拒绝";
            case 5: return "已转订单";
            case 6: return "已取消";
            default: return "未知";
        }
    }
}
