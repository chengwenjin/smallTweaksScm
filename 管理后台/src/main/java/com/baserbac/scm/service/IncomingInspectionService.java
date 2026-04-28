package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.IncomingInspectionCreateDTO;
import com.baserbac.scm.dto.IncomingInspectionQueryDTO;
import com.baserbac.scm.entity.IncomingInspection;
import com.baserbac.scm.entity.PurchaseOrder;
import com.baserbac.scm.entity.PurchaseOrderItem;
import com.baserbac.scm.entity.Shipment;
import com.baserbac.scm.mapper.IncomingInspectionMapper;
import com.baserbac.scm.mapper.PurchaseOrderItemMapper;
import com.baserbac.scm.mapper.PurchaseOrderMapper;
import com.baserbac.scm.mapper.ShipmentMapper;
import com.baserbac.scm.vo.IncomingInspectionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomingInspectionService {

    private final IncomingInspectionMapper inspectionMapper;
    private final PurchaseOrderMapper orderMapper;
    private final PurchaseOrderItemMapper itemMapper;
    private final ShipmentMapper shipmentMapper;

    private static final String INSPECTION_NO_PREFIX = "II";

    private static final Map<Integer, String> RESULT_MAP = new HashMap<>();
    private static final Map<Integer, String> TYPE_MAP = new HashMap<>();
    private static final Map<Integer, String> HANDLING_MAP = new HashMap<>();
    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();

    static {
        RESULT_MAP.put(0, "待检验");
        RESULT_MAP.put(1, "合格");
        RESULT_MAP.put(2, "让步接收");
        RESULT_MAP.put(3, "退货");
        RESULT_MAP.put(4, "待复检");

        TYPE_MAP.put(1, "抽样检验");
        TYPE_MAP.put(2, "全检");

        HANDLING_MAP.put(1, "入库");
        HANDLING_MAP.put(2, "让步接收");
        HANDLING_MAP.put(3, "退货");
        HANDLING_MAP.put(4, "待复检");

        STATUS_MAP.put(0, "草稿");
        STATUS_MAP.put(1, "已提交");
        STATUS_MAP.put(2, "已审核");
    }

    public PageResult<IncomingInspectionVO> pageInspections(IncomingInspectionQueryDTO queryDTO) {
        Page<IncomingInspection> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<IncomingInspection> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getInspectionNo() != null, IncomingInspection::getInspectionNo, queryDTO.getInspectionNo())
                .eq(queryDTO.getOrderId() != null, IncomingInspection::getOrderId, queryDTO.getOrderId())
                .like(queryDTO.getOrderNo() != null, IncomingInspection::getOrderNo, queryDTO.getOrderNo())
                .eq(queryDTO.getShipmentId() != null, IncomingInspection::getShipmentId, queryDTO.getShipmentId())
                .eq(queryDTO.getSupplierId() != null, IncomingInspection::getSupplierId, queryDTO.getSupplierId())
                .like(queryDTO.getSupplierName() != null, IncomingInspection::getSupplierName, queryDTO.getSupplierName())
                .like(queryDTO.getMaterialCode() != null, IncomingInspection::getMaterialCode, queryDTO.getMaterialCode())
                .like(queryDTO.getMaterialName() != null, IncomingInspection::getMaterialName, queryDTO.getMaterialName())
                .eq(queryDTO.getInspectionType() != null, IncomingInspection::getInspectionType, queryDTO.getInspectionType())
                .eq(queryDTO.getInspectionResult() != null, IncomingInspection::getInspectionResult, queryDTO.getInspectionResult())
                .eq(queryDTO.getStatus() != null, IncomingInspection::getStatus, queryDTO.getStatus())
                .ge(queryDTO.getInspectionDateStart() != null, IncomingInspection::getInspectionDate, queryDTO.getInspectionDateStart())
                .le(queryDTO.getInspectionDateEnd() != null, IncomingInspection::getInspectionDate, queryDTO.getInspectionDateEnd())
                .orderByDesc(IncomingInspection::getCreateTime);

        Page<IncomingInspection> result = inspectionMapper.selectPage(page, wrapper);

        List<IncomingInspectionVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(
                result.getTotal(),
                voList,
                (long) result.getCurrent(),
                (long) result.getSize()
        );
    }

    public IncomingInspectionVO getInspectionById(Long id) {
        IncomingInspection inspection = inspectionMapper.selectById(id);
        if (inspection == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(inspection);
    }

    public List<IncomingInspectionVO> getInspectionsByOrderId(Long orderId) {
        List<IncomingInspection> inspections = inspectionMapper.selectList(
                new LambdaQueryWrapper<IncomingInspection>()
                        .eq(IncomingInspection::getOrderId, orderId)
                        .eq(IncomingInspection::getIsDeleted, 0)
                        .orderByDesc(IncomingInspection::getCreateTime)
        );

        return inspections.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void createInspection(IncomingInspectionCreateDTO createDTO) {
        PurchaseOrder order = orderMapper.selectById(createDTO.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        IncomingInspection inspection = new IncomingInspection();
        inspection.setInspectionNo(generateInspectionNo());
        inspection.setOrderId(createDTO.getOrderId());
        inspection.setOrderNo(order.getOrderNo());

        if (createDTO.getShipmentId() != null) {
            Shipment shipment = shipmentMapper.selectById(createDTO.getShipmentId());
            if (shipment != null) {
                inspection.setShipmentId(shipment.getId());
                inspection.setShipmentNo(shipment.getShipmentNo());
            }
        }

        inspection.setAppointmentId(createDTO.getAppointmentId());
        inspection.setSupplierId(order.getSupplierId());
        inspection.setSupplierName(order.getSupplierName());
        inspection.setInspectionType(createDTO.getInspectionType());
        inspection.setInspectionDate(LocalDate.now());
        inspection.setInspector(createDTO.getInspector());
        inspection.setMaterialCode(createDTO.getMaterialCode());
        inspection.setMaterialName(createDTO.getMaterialName());
        inspection.setMaterialSpec(createDTO.getMaterialSpec());
        inspection.setMaterialUnit(createDTO.getMaterialUnit());
        inspection.setBatchNo(createDTO.getBatchNo());
        inspection.setProductionDate(createDTO.getProductionDate());
        inspection.setExpiryDate(createDTO.getExpiryDate());
        inspection.setTotalQuantity(createDTO.getTotalQuantity());
        inspection.setInspectionQuantity(createDTO.getInspectionQuantity() != null 
                ? createDTO.getInspectionQuantity() 
                : createDTO.getTotalQuantity());

        if (createDTO.getSampleQuantity() != null) {
            inspection.setSampleQuantity(createDTO.getSampleQuantity());
        }

        inspection.setPassQuantity(createDTO.getPassQuantity() != null ? createDTO.getPassQuantity() : BigDecimal.ZERO);
        inspection.setFailQuantity(createDTO.getFailQuantity() != null ? createDTO.getFailQuantity() : BigDecimal.ZERO);

        if (createDTO.getInspectionResult() != null) {
            inspection.setInspectionResult(createDTO.getInspectionResult());
            if (createDTO.getTotalQuantity().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal passRate = inspection.getPassQuantity()
                        .multiply(new BigDecimal(100))
                        .divide(createDTO.getTotalQuantity(), 2, RoundingMode.HALF_UP);
                inspection.setPassRate(passRate + "%");
            }
        } else {
            inspection.setInspectionResult(0);
        }

        inspection.setInspectionItems(createDTO.getInspectionItems());
        inspection.setInspectionData(createDTO.getInspectionData());
        inspection.setDefectDescription(createDTO.getDefectDescription());
        inspection.setHandlingSuggestion(createDTO.getHandlingSuggestion());
        inspection.setHandlingType(createDTO.getHandlingType());
        inspection.setStatus(0);
        inspection.setIsDeleted(0);
        inspection.setRemark(createDTO.getRemark());

        inspectionMapper.insert(inspection);

        log.info("创建来料质检记录: inspectionNo={}, orderNo={}", inspection.getInspectionNo(), order.getOrderNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitInspection(Long id) {
        IncomingInspection inspection = inspectionMapper.selectById(id);
        if (inspection == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (inspection.getStatus() != 0) {
            throw new BusinessException(4001, "质检记录状态不允许提交");
        }

        inspection.setStatus(1);
        inspectionMapper.updateById(inspection);

        log.info("提交来料质检记录: inspectionNo={}", inspection.getInspectionNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void approveInspection(Long id, Integer handlingType, String remark) {
        IncomingInspection inspection = inspectionMapper.selectById(id);
        if (inspection == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (inspection.getStatus() != 1) {
            throw new BusinessException(4002, "质检记录状态不允许审核");
        }

        inspection.setStatus(2);
        if (handlingType != null) {
            inspection.setHandlingType(handlingType);
        }
        if (remark != null) {
            if (inspection.getRemark() != null) {
                inspection.setRemark(inspection.getRemark() + "; 审核备注: " + remark);
            } else {
                inspection.setRemark("审核备注: " + remark);
            }
        }

        inspectionMapper.updateById(inspection);

        updateOrderItemInspection(inspection);

        log.info("审核来料质检记录: inspectionNo={}, handlingType={}", inspection.getInspectionNo(), handlingType);
    }

    private void updateOrderItemInspection(IncomingInspection inspection) {
        List<PurchaseOrderItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getOrderId, inspection.getOrderId())
                        .eq(PurchaseOrderItem::getIsDeleted, 0)
        );

        for (PurchaseOrderItem item : items) {
            if (item.getMaterialCode().equals(inspection.getMaterialCode())) {
                item.setInspectedQuantity(item.getInspectedQuantity() != null
                        ? item.getInspectedQuantity().add(inspection.getInspectionQuantity())
                        : inspection.getInspectionQuantity());
                item.setPassQuantity(item.getPassQuantity() != null
                        ? item.getPassQuantity().add(inspection.getPassQuantity())
                        : inspection.getPassQuantity());
                item.setFailQuantity(item.getFailQuantity() != null
                        ? item.getFailQuantity().add(inspection.getFailQuantity())
                        : inspection.getFailQuantity());

                itemMapper.updateById(item);
            }
        }
    }

    private String generateInspectionNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return INSPECTION_NO_PREFIX + dateStr + System.currentTimeMillis();
    }

    private IncomingInspectionVO convertToVO(IncomingInspection inspection) {
        return IncomingInspectionVO.builder()
                .id(inspection.getId())
                .inspectionNo(inspection.getInspectionNo())
                .orderId(inspection.getOrderId())
                .orderNo(inspection.getOrderNo())
                .shipmentId(inspection.getShipmentId())
                .shipmentNo(inspection.getShipmentNo())
                .appointmentId(inspection.getAppointmentId())
                .appointmentNo(inspection.getAppointmentNo())
                .supplierId(inspection.getSupplierId())
                .supplierName(inspection.getSupplierName())
                .inspectionType(inspection.getInspectionType())
                .inspectionTypeName(TYPE_MAP.getOrDefault(inspection.getInspectionType(), "未知"))
                .inspectionDate(inspection.getInspectionDate())
                .inspector(inspection.getInspector())
                .materialCode(inspection.getMaterialCode())
                .materialName(inspection.getMaterialName())
                .materialSpec(inspection.getMaterialSpec())
                .materialUnit(inspection.getMaterialUnit())
                .batchNo(inspection.getBatchNo())
                .productionDate(inspection.getProductionDate())
                .expiryDate(inspection.getExpiryDate())
                .totalQuantity(inspection.getTotalQuantity())
                .inspectionQuantity(inspection.getInspectionQuantity())
                .sampleQuantity(inspection.getSampleQuantity())
                .passQuantity(inspection.getPassQuantity())
                .failQuantity(inspection.getFailQuantity())
                .inspectionResult(inspection.getInspectionResult())
                .inspectionResultName(RESULT_MAP.getOrDefault(inspection.getInspectionResult(), "未知"))
                .passRate(inspection.getPassRate())
                .inspectionItems(inspection.getInspectionItems())
                .inspectionData(inspection.getInspectionData())
                .defectDescription(inspection.getDefectDescription())
                .handlingSuggestion(inspection.getHandlingSuggestion())
                .handlingType(inspection.getHandlingType())
                .handlingTypeName(HANDLING_MAP.getOrDefault(inspection.getHandlingType(), "未知"))
                .warehouseReceiptId(inspection.getWarehouseReceiptId())
                .warehouseReceiptNo(inspection.getWarehouseReceiptNo())
                .remark(inspection.getRemark())
                .status(inspection.getStatus())
                .statusName(STATUS_MAP.getOrDefault(inspection.getStatus(), "未知"))
                .createTime(inspection.getCreateTime())
                .updateTime(inspection.getUpdateTime())
                .build();
    }

    public void addTestData() {
        List<PurchaseOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .eq(PurchaseOrder::getIsDeleted, 0)
                        .eq(PurchaseOrder::getStatus, 7)
                        .orderByAsc(PurchaseOrder::getId)
                        .last("LIMIT 20")
        );

        if (orders.isEmpty()) {
            log.warn("没有发货中的订单数据，无法生成来料质检测试数据");
            return;
        }

        String[] inspectors = {"质检员-张三", "质检员-李四", "质检员-王五", "质检员-赵六"};
        String[] materialNames = {"不锈钢钢板", "铝合金型材", "电子元器件", "电机马达", "传感器"};
        String[] batchNos = {"BATCH2024001", "BATCH2024002", "BATCH2024003", "BATCH2024004"};

        for (int i = 0; i < orders.size(); i++) {
            PurchaseOrder order = orders.get(i);

            List<PurchaseOrderItem> items = itemMapper.selectList(
                    new LambdaQueryWrapper<PurchaseOrderItem>()
                            .eq(PurchaseOrderItem::getOrderId, order.getId())
                            .eq(PurchaseOrderItem::getIsDeleted, 0)
            );

            for (int j = 0; j < items.size(); j++) {
                PurchaseOrderItem item = items.get(j);
                try {
                    IncomingInspectionCreateDTO createDTO = new IncomingInspectionCreateDTO();
                    createDTO.setOrderId(order.getId());
                    createDTO.setInspectionType((i + j) % 2 + 1);
                    createDTO.setInspector(inspectors[(i + j) % inspectors.length]);
                    createDTO.setMaterialCode(item.getMaterialCode());
                    createDTO.setMaterialName(item.getMaterialName());
                    createDTO.setMaterialSpec(item.getMaterialSpec());
                    createDTO.setMaterialUnit(item.getMaterialUnit());
                    createDTO.setBatchNo(batchNos[(i + j) % batchNos.length]);
                    createDTO.setProductionDate(LocalDate.now().minusMonths((i + j) % 6));
                    createDTO.setTotalQuantity(item.getQuantity());
                    createDTO.setInspectionQuantity(item.getQuantity());
                    createDTO.setSampleQuantity(item.getQuantity().multiply(new BigDecimal("0.1")));

                    double passRate = 0.8 + Math.random() * 0.2;
                    BigDecimal passQty = item.getQuantity().multiply(new BigDecimal(passRate)).setScale(0, RoundingMode.DOWN);
                    BigDecimal failQty = item.getQuantity().subtract(passQty);

                    createDTO.setPassQuantity(passQty);
                    createDTO.setFailQuantity(failQty);

                    if (passQty.compareTo(item.getQuantity()) >= 0) {
                        createDTO.setInspectionResult(1);
                        createDTO.setHandlingType(1);
                    } else if (failQty.compareTo(item.getQuantity().multiply(new BigDecimal("0.1"))) <= 0) {
                        createDTO.setInspectionResult(2);
                        createDTO.setHandlingType(2);
                    } else {
                        createDTO.setInspectionResult(3);
                        createDTO.setHandlingType(3);
                    }

                    createDTO.setInspectionItems("外观检查,尺寸测量,性能测试,材质检验");
                    createDTO.setDefectDescription(failQty.compareTo(BigDecimal.ZERO) > 0 ? "部分产品存在表面划痕" : "无缺陷");
                    createDTO.setHandlingSuggestion(createDTO.getInspectionResult() == 1 ? "建议入库" : 
                            (createDTO.getInspectionResult() == 2 ? "建议让步接收" : "建议退货"));
                    createDTO.setRemark("测试数据" + (i * 10 + j + 1));

                    createInspection(createDTO);

                    List<IncomingInspection> inspections = inspectionMapper.selectList(
                            new LambdaQueryWrapper<IncomingInspection>()
                                    .eq(IncomingInspection::getOrderId, order.getId())
                                    .eq(IncomingInspection::getMaterialCode, item.getMaterialCode())
                                    .eq(IncomingInspection::getIsDeleted, 0)
                                    .orderByDesc(IncomingInspection::getCreateTime)
                                    .last("LIMIT 1")
                    );

                    if (!inspections.isEmpty()) {
                        submitInspection(inspections.get(0).getId());
                        if ((i + j) % 2 == 0) {
                            approveInspection(inspections.get(0).getId(), createDTO.getHandlingType(), "审核通过");
                        }
                    }

                } catch (Exception e) {
                    log.warn("生成来料质检测试数据失败: orderNo={}, {}", order.getOrderNo(), e.getMessage());
                }
            }
        }

        log.info("来料质检测试数据生成完成");
    }
}
