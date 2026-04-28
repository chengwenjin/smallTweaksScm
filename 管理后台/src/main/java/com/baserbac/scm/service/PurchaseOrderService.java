package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.OrderCancelDTO;
import com.baserbac.scm.dto.OrderChangeDTO;
import com.baserbac.scm.dto.OrderConfirmDTO;
import com.baserbac.scm.dto.PurchaseOrderCreateDTO;
import com.baserbac.scm.dto.PurchaseOrderItemDTO;
import com.baserbac.scm.dto.PurchaseOrderQueryDTO;
import com.baserbac.scm.dto.PurchaseOrderUpdateDTO;
import com.baserbac.scm.entity.OrderChange;
import com.baserbac.scm.entity.PurchaseOrder;
import com.baserbac.scm.entity.PurchaseOrderItem;
import com.baserbac.scm.entity.Supplier;
import com.baserbac.scm.mapper.OrderChangeMapper;
import com.baserbac.scm.mapper.PurchaseOrderItemMapper;
import com.baserbac.scm.mapper.PurchaseOrderMapper;
import com.baserbac.scm.mapper.SupplierMapper;
import com.baserbac.scm.vo.OrderChangeVO;
import com.baserbac.scm.vo.PurchaseOrderItemVO;
import com.baserbac.scm.vo.PurchaseOrderVO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderMapper orderMapper;
    private final PurchaseOrderItemMapper itemMapper;
    private final OrderChangeMapper changeMapper;
    private final SupplierMapper supplierMapper;

    private static final String ORDER_NO_PREFIX = "PO";

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    private static final Map<Integer, String> TYPE_MAP = new HashMap<>();

    static {
        STATUS_MAP.put(0, "新建");
        STATUS_MAP.put(1, "待审批");
        STATUS_MAP.put(2, "审批通过");
        STATUS_MAP.put(3, "已发布");
        STATUS_MAP.put(4, "待确认");
        STATUS_MAP.put(5, "已确认");
        STATUS_MAP.put(6, "生产中");
        STATUS_MAP.put(7, "发货中");
        STATUS_MAP.put(8, "部分收货");
        STATUS_MAP.put(9, "全部收货");
        STATUS_MAP.put(10, "已完成");
        STATUS_MAP.put(11, "已取消");

        TYPE_MAP.put(1, "标准订单");
        TYPE_MAP.put(2, "紧急订单");
        TYPE_MAP.put(3, "补货订单");
    }

    public PageResult<PurchaseOrderVO> pageOrders(PurchaseOrderQueryDTO queryDTO) {
        Page<PurchaseOrder> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getOrderNo() != null, PurchaseOrder::getOrderNo, queryDTO.getOrderNo())
                .like(queryDTO.getOrderName() != null, PurchaseOrder::getOrderName, queryDTO.getOrderName())
                .eq(queryDTO.getSupplierId() != null, PurchaseOrder::getSupplierId, queryDTO.getSupplierId())
                .like(queryDTO.getSupplierName() != null, PurchaseOrder::getSupplierName, queryDTO.getSupplierName())
                .eq(queryDTO.getOrderType() != null, PurchaseOrder::getOrderType, queryDTO.getOrderType())
                .eq(queryDTO.getStatus() != null, PurchaseOrder::getStatus, queryDTO.getStatus())
                .orderByDesc(PurchaseOrder::getCreateTime);

        Page<PurchaseOrder> result = orderMapper.selectPage(page, wrapper);

        List<PurchaseOrderVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(
                result.getTotal(),
                voList,
                (long) result.getCurrent(),
                (long) result.getSize()
        );
    }

    public PurchaseOrderVO getOrderById(Long id) {
        PurchaseOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVOWithItems(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createOrder(PurchaseOrderCreateDTO createDTO) {
        Supplier supplier = supplierMapper.selectById(createDTO.getSupplierId());
        if (supplier == null) {
            throw new BusinessException(4001, "供应商不存在");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateOrderNo());
        order.setOrderName(createDTO.getOrderName());
        order.setSupplierId(createDTO.getSupplierId());
        order.setSupplierCode(supplier.getSupplierCode());
        order.setSupplierName(supplier.getSupplierName());
        order.setOrderType(createDTO.getOrderType());
        order.setOrderDate(createDTO.getOrderDate() != null ? createDTO.getOrderDate() : LocalDate.now());
        order.setExpectedDeliveryDate(createDTO.getExpectedDeliveryDate());
        order.setPaymentTerms(createDTO.getPaymentTerms());
        order.setDeliveryTerms(createDTO.getDeliveryTerms());
        order.setDeliveryAddress(createDTO.getDeliveryAddress());
        order.setContactPerson(createDTO.getContactPerson());
        order.setContactPhone(createDTO.getContactPhone());
        order.setSourceRequestId(createDTO.getSourceRequestId());
        order.setStatus(0);
        order.setIsDeleted(0);
        order.setRemark(createDTO.getRemark());

        orderMapper.insert(order);

        if (createDTO.getItems() != null && !createDTO.getItems().isEmpty()) {
            BigDecimal totalQuantity = BigDecimal.ZERO;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (PurchaseOrderItemDTO itemDTO : createDTO.getItems()) {
                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setOrderId(order.getId());
                item.setOrderNo(order.getOrderNo());
                item.setMaterialCode(itemDTO.getMaterialCode());
                item.setMaterialName(itemDTO.getMaterialName());
                item.setMaterialSpec(itemDTO.getMaterialSpec());
                item.setMaterialUnit(itemDTO.getMaterialUnit());
                item.setMaterialCategory(itemDTO.getMaterialCategory());
                item.setQuantity(itemDTO.getQuantity());
                item.setUnitPrice(itemDTO.getUnitPrice() != null ? itemDTO.getUnitPrice() : BigDecimal.ZERO);
                item.setTotalPrice(itemDTO.getUnitPrice() != null 
                    ? itemDTO.getQuantity().multiply(itemDTO.getUnitPrice()) 
                    : BigDecimal.ZERO);
                item.setReceivedQuantity(BigDecimal.ZERO);
                item.setInspectedQuantity(BigDecimal.ZERO);
                item.setPassQuantity(BigDecimal.ZERO);
                item.setFailQuantity(BigDecimal.ZERO);
                item.setStatus(0);
                item.setIsDeleted(0);
                item.setRemark(itemDTO.getRemark());

                itemMapper.insert(item);

                totalQuantity = totalQuantity.add(item.getQuantity());
                totalAmount = totalAmount.add(item.getTotalPrice());
            }

            order.setItemCount(createDTO.getItems().size());
            order.setTotalQuantity(totalQuantity);
            order.setTotalAmount(totalAmount);
            orderMapper.updateById(order);
        }

        log.info("创建采购订单成功: orderNo={}", order.getOrderNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(PurchaseOrderUpdateDTO updateDTO) {
        PurchaseOrder order = orderMapper.selectById(updateDTO.getId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (order.getStatus() > 4) {
            throw new BusinessException(4002, "订单已确认，无法修改，请走订单变更流程");
        }

        if (updateDTO.getOrderName() != null) {
            order.setOrderName(updateDTO.getOrderName());
        }
        if (updateDTO.getOrderType() != null) {
            order.setOrderType(updateDTO.getOrderType());
        }
        if (updateDTO.getExpectedDeliveryDate() != null) {
            order.setExpectedDeliveryDate(updateDTO.getExpectedDeliveryDate());
        }
        if (updateDTO.getPaymentTerms() != null) {
            order.setPaymentTerms(updateDTO.getPaymentTerms());
        }
        if (updateDTO.getDeliveryTerms() != null) {
            order.setDeliveryTerms(updateDTO.getDeliveryTerms());
        }
        if (updateDTO.getDeliveryAddress() != null) {
            order.setDeliveryAddress(updateDTO.getDeliveryAddress());
        }
        if (updateDTO.getContactPerson() != null) {
            order.setContactPerson(updateDTO.getContactPerson());
        }
        if (updateDTO.getContactPhone() != null) {
            order.setContactPhone(updateDTO.getContactPhone());
        }
        if (updateDTO.getRemark() != null) {
            order.setRemark(updateDTO.getRemark());
        }

        orderMapper.updateById(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void issueOrder(Long id) {
        PurchaseOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (order.getStatus() != 0 && order.getStatus() != 2) {
            throw new BusinessException(4003, "订单状态不允许下发");
        }

        order.setStatus(4);
        orderMapper.updateById(order);
        log.info("下发采购订单: orderNo={}", order.getOrderNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(OrderConfirmDTO confirmDTO) {
        PurchaseOrder order = orderMapper.selectById(confirmDTO.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (order.getStatus() != 4) {
            throw new BusinessException(4004, "订单状态不允许确认");
        }

        order.setStatus(5);
        if (confirmDTO.getSupplierExpectedDeliveryDate() != null) {
            order.setExpectedDeliveryDate(confirmDTO.getSupplierExpectedDeliveryDate());
        }
        order.setRemark("供应商确认人: " + confirmDTO.getConfirmedBy() 
            + (confirmDTO.getConfirmRemark() != null ? ", 备注: " + confirmDTO.getConfirmRemark() : ""));

        orderMapper.updateById(order);
        log.info("供应商确认订单: orderNo={}, confirmedBy={}", order.getOrderNo(), confirmDTO.getConfirmedBy());
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeOrder(OrderChangeDTO changeDTO) {
        PurchaseOrder order = orderMapper.selectById(changeDTO.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (order.getStatus() < 5) {
            throw new BusinessException(4005, "订单未确认，可直接修改，无需走变更流程");
        }

        OrderChange change = new OrderChange();
        change.setChangeNo(generateChangeNo());
        change.setOrderId(order.getId());
        change.setOrderNo(order.getOrderNo());
        change.setChangeType(changeDTO.getChangeType());
        change.setChangeTitle(getChangeTypeTitle(changeDTO.getChangeType()));
        change.setChangeReason(changeDTO.getChangeReason());

        StringBuilder originalContent = new StringBuilder();
        StringBuilder changedContent = new StringBuilder();

        if (changeDTO.getNewExpectedDeliveryDate() != null) {
            originalContent.append("原交货日期: ").append(order.getExpectedDeliveryDate()).append("; ");
            changedContent.append("新交货日期: ").append(changeDTO.getNewExpectedDeliveryDate()).append("; ");
            order.setExpectedDeliveryDate(changeDTO.getNewExpectedDeliveryDate());
        }

        change.setOriginalContent(originalContent.toString());
        change.setChangedContent(changedContent.toString());
        change.setChangeDescription(changeDTO.getChangeDescription());
        change.setStatus(1);
        change.setIsDeleted(0);

        changeMapper.insert(change);
        orderMapper.updateById(order);

        log.info("创建订单变更记录: changeNo={}, orderNo={}", change.getChangeNo(), order.getOrderNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(OrderCancelDTO cancelDTO) {
        PurchaseOrder order = orderMapper.selectById(cancelDTO.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (order.getStatus() >= 9) {
            throw new BusinessException(4006, "订单已完成，无法取消");
        }

        order.setStatus(11);
        String remark = "取消原因: " + cancelDTO.getCancelReason();
        if (cancelDTO.getCancelRemark() != null) {
            remark += ", 备注: " + cancelDTO.getCancelRemark();
        }
        if (order.getRemark() != null) {
            order.setRemark(order.getRemark() + "; " + remark);
        } else {
            order.setRemark(remark);
        }

        orderMapper.updateById(order);
        log.info("取消采购订单: orderNo={}", order.getOrderNo());
    }

    public List<OrderChangeVO> getOrderChanges(Long orderId) {
        List<OrderChange> changes = changeMapper.selectList(
                new LambdaQueryWrapper<OrderChange>()
                        .eq(OrderChange::getOrderId, orderId)
                        .eq(OrderChange::getIsDeleted, 0)
                        .orderByDesc(OrderChange::getCreateTime)
        );

        return changes.stream()
                .map(this::convertChangeToVO)
                .collect(Collectors.toList());
    }

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return ORDER_NO_PREFIX + dateStr + System.currentTimeMillis();
    }

    private String generateChangeNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "OC" + dateStr + System.currentTimeMillis();
    }

    private String getChangeTypeTitle(Integer type) {
        Map<Integer, String> typeMap = new HashMap<>();
        typeMap.put(1, "交货日期变更");
        typeMap.put(2, "数量变更");
        typeMap.put(3, "价格变更");
        typeMap.put(4, "其他变更");
        return typeMap.getOrDefault(type, "其他变更");
    }

    private PurchaseOrderVO convertToVO(PurchaseOrder order) {
        return PurchaseOrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .orderName(order.getOrderName())
                .supplierId(order.getSupplierId())
                .supplierCode(order.getSupplierCode())
                .supplierName(order.getSupplierName())
                .sourceRequestId(order.getSourceRequestId())
                .sourceRequestNo(order.getSourceRequestNo())
                .orderType(order.getOrderType())
                .orderTypeName(TYPE_MAP.getOrDefault(order.getOrderType(), "未知"))
                .orderDate(order.getOrderDate())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .actualDeliveryDate(order.getActualDeliveryDate())
                .itemCount(order.getItemCount())
                .totalQuantity(order.getTotalQuantity())
                .totalAmount(order.getTotalAmount())
                .paymentTerms(order.getPaymentTerms())
                .deliveryTerms(order.getDeliveryTerms())
                .deliveryAddress(order.getDeliveryAddress())
                .contactPerson(order.getContactPerson())
                .contactPhone(order.getContactPhone())
                .status(order.getStatus())
                .statusName(STATUS_MAP.getOrDefault(order.getStatus(), "未知"))
                .remark(order.getRemark())
                .createTime(order.getCreateTime())
                .updateTime(order.getUpdateTime())
                .build();
    }

    private PurchaseOrderVO convertToVOWithItems(PurchaseOrder order) {
        PurchaseOrderVO vo = convertToVO(order);

        List<PurchaseOrderItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getOrderId, order.getId())
                        .eq(PurchaseOrderItem::getIsDeleted, 0)
                        .orderByAsc(PurchaseOrderItem::getId)
        );

        List<PurchaseOrderItemVO> itemVOList = items.stream()
                .map(this::convertItemToVO)
                .collect(Collectors.toList());
        vo.setItems(itemVOList);

        return vo;
    }

    private PurchaseOrderItemVO convertItemToVO(PurchaseOrderItem item) {
        return PurchaseOrderItemVO.builder()
                .id(item.getId())
                .orderId(item.getOrderId())
                .orderNo(item.getOrderNo())
                .materialCode(item.getMaterialCode())
                .materialName(item.getMaterialName())
                .materialSpec(item.getMaterialSpec())
                .materialUnit(item.getMaterialUnit())
                .materialCategory(item.getMaterialCategory())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .receivedQuantity(item.getReceivedQuantity())
                .inspectedQuantity(item.getInspectedQuantity())
                .passQuantity(item.getPassQuantity())
                .failQuantity(item.getFailQuantity())
                .status(item.getStatus())
                .remark(item.getRemark())
                .createTime(item.getCreateTime())
                .updateTime(item.getUpdateTime())
                .build();
    }

    private OrderChangeVO convertChangeToVO(OrderChange change) {
        return OrderChangeVO.builder()
                .id(change.getId())
                .changeNo(change.getChangeNo())
                .orderId(change.getOrderId())
                .orderNo(change.getOrderNo())
                .changeType(change.getChangeType())
                .changeTitle(change.getChangeTitle())
                .changeReason(change.getChangeReason())
                .originalContent(change.getOriginalContent())
                .changedContent(change.getChangedContent())
                .changeDescription(change.getChangeDescription())
                .status(change.getStatus())
                .approvalBy(change.getApprovalBy())
                .approvalTime(change.getApprovalTime())
                .approvalRemark(change.getApprovalRemark())
                .createTime(change.getCreateTime())
                .updateTime(change.getUpdateTime())
                .build();
    }

    public void addTestData() {
        List<Supplier> suppliers = supplierMapper.selectList(
                new LambdaQueryWrapper<Supplier>()
                        .eq(Supplier::getIsDeleted, 0)
                        .orderByAsc(Supplier::getId)
                        .last("LIMIT 10")
        );

        if (suppliers.isEmpty()) {
            log.warn("没有供应商数据，无法生成采购订单测试数据");
            return;
        }

        String[] orderNames = {
                "2024年Q1原材料采购订单", "电子元器件紧急采购订单", "设备配件补货订单",
                "生产线维护物资采购订单", "办公用品采购订单", "新生产线设备采购订单",
                "季度原材料备货订单", "安全设备采购订单", "环保设备采购订单",
                "IT设备更新采购订单", "生产工具采购订单", "模具采购订单",
                "夹具采购订单", "量具采购订单", "刀具采购订单",
                "焊接材料采购订单", "油漆涂料采购订单", "胶粘剂采购订单",
                "清洁用品采购订单", "劳保用品采购订单", "消防用品采购订单",
                "包装材料采购订单", "化工原料采购订单", "润滑油采购订单",
                "密封件采购订单", "轴承采购订单", "电缆线材采购订单",
                "传感器采购订单", "控制器采购订单", "电机马达采购订单"
        };

        String[] materialNames = {
                "不锈钢钢板", "铝合金型材", "PVC管材", "电子元器件", "电机马达",
                "传感器", "控制器", "电缆线材", "密封件", "轴承",
                "润滑油", "化工原料", "包装材料", "紧固件", "液压元件",
                "气动元件", "电气开关", "变压器", "电容器", "电阻器",
                "连接器", "散热器", "风扇", "水泵", "阀门",
                "过滤器", "压力表", "流量计", "温度传感器", "压力传感器"
        };

        LocalDate today = LocalDate.now();

        for (int i = 0; i < 30; i++) {
            Supplier supplier = suppliers.get(i % suppliers.size());

            PurchaseOrderCreateDTO createDTO = new PurchaseOrderCreateDTO();
            createDTO.setOrderName(orderNames[i % orderNames.length] + "-" + (i + 1));
            createDTO.setSupplierId(supplier.getId());
            createDTO.setOrderType((i % 3) + 1);
            createDTO.setOrderDate(today.minusDays(i * 2));
            createDTO.setExpectedDeliveryDate(today.plusDays(15 + i));
            createDTO.setPaymentTerms("月结30天");
            createDTO.setDeliveryTerms("送货上门");
            createDTO.setDeliveryAddress("某某市某某区某某工业园区A栋仓库");
            createDTO.setContactPerson("采购部-李工");
            createDTO.setContactPhone("010-88888888");
            createDTO.setRemark("测试数据" + (i + 1));

            List<PurchaseOrderItemDTO> items = new ArrayList<>();

            int itemCount = (i % 3) + 1;
            for (int j = 0; j < itemCount; j++) {
                PurchaseOrderItemDTO item = new PurchaseOrderItemDTO();
                item.setMaterialCode("MAT" + String.format("%04d", (i * 3 + j + 1)));
                item.setMaterialName(materialNames[(i * 3 + j) % materialNames.length]);
                item.setMaterialSpec("规格" + ((i + j) % 10 + 1));
                item.setMaterialUnit(j % 3 == 0 ? "件" : (j % 3 == 1 ? "台" : "套"));
                item.setMaterialCategory("原材料");
                item.setQuantity(new BigDecimal(10 + (i + j) * 5));
                item.setUnitPrice(new BigDecimal(100 + (i + j) * 20));
                item.setRemark("测试明细" + (j + 1));
                items.add(item);
            }
            createDTO.setItems(items);

            try {
                createOrder(createDTO);

                if (i % 3 == 0) {
                    List<PurchaseOrder> orders = orderMapper.selectList(
                            new LambdaQueryWrapper<PurchaseOrder>()
                                    .eq(PurchaseOrder::getSupplierId, supplier.getId())
                                    .orderByDesc(PurchaseOrder::getCreateTime)
                                    .last("LIMIT 1")
                    );
                    if (!orders.isEmpty()) {
                        PurchaseOrder order = orders.get(0);

                        issueOrder(order.getId());

                        OrderConfirmDTO confirmDTO = new OrderConfirmDTO();
                        confirmDTO.setOrderId(order.getId());
                        confirmDTO.setSupplierExpectedDeliveryDate(today.plusDays(20 + i));
                        confirmDTO.setConfirmedBy(supplier.getContactPerson());
                        confirmDTO.setConfirmRemark("供应商确认交货日期");
                        confirmOrder(confirmDTO);
                    }
                }
            } catch (Exception e) {
                log.warn("生成采购订单测试数据失败: {}", e.getMessage());
            }
        }

        log.info("采购订单测试数据生成完成");
    }
}
