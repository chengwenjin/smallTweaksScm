package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.PurchasePlanQueryDTO;
import com.baserbac.scm.entity.*;
import com.baserbac.scm.mapper.*;
import com.baserbac.scm.vo.PurchasePlanItemVO;
import com.baserbac.scm.vo.PurchasePlanVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchasePlanService {

    private final PurchasePlanMapper planMapper;
    private final PurchasePlanItemMapper planItemMapper;
    private final ProductionWorkOrderMapper workOrderMapper;
    private final MaterialInventoryMapper inventoryMapper;
    private final BomMapper bomMapper;
    private final SupplierMapper supplierMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public PageResult<PurchasePlanVO> pagePlans(PurchasePlanQueryDTO queryDTO) {
        Page<PurchasePlan> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<PurchasePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getPlanNo() != null, PurchasePlan::getPlanNo, queryDTO.getPlanNo())
               .like(queryDTO.getPlanName() != null, PurchasePlan::getPlanName, queryDTO.getPlanName())
               .eq(queryDTO.getPlanType() != null, PurchasePlan::getPlanType, queryDTO.getPlanType())
               .eq(queryDTO.getSourceType() != null, PurchasePlan::getSourceType, queryDTO.getSourceType())
               .eq(queryDTO.getYear() != null, PurchasePlan::getYear, queryDTO.getYear())
               .eq(queryDTO.getMonth() != null, PurchasePlan::getMonth, queryDTO.getMonth())
               .eq(queryDTO.getStatus() != null, PurchasePlan::getStatus, queryDTO.getStatus())
               .orderByDesc(PurchasePlan::getCreateTime);

        Page<PurchasePlan> result = planMapper.selectPage(page, wrapper);

        List<Long> planIds = result.getRecords().stream()
            .map(PurchasePlan::getId)
            .collect(Collectors.toList());

        Map<Long, List<PurchasePlanItem>> itemMap = null;
        if (!planIds.isEmpty()) {
            List<PurchasePlanItem> items = planItemMapper.selectList(
                new LambdaQueryWrapper<PurchasePlanItem>()
                    .in(PurchasePlanItem::getPlanId, planIds)
                    .orderByAsc(PurchasePlanItem::getId)
            );
            itemMap = items.stream()
                .collect(Collectors.groupingBy(PurchasePlanItem::getPlanId));
        }

        Map<Long, List<PurchasePlanItem>> finalItemMap = itemMap;
        List<PurchasePlanVO> voList = result.getRecords().stream()
            .map(p -> convertToVO(p, finalItemMap))
            .collect(Collectors.toList());

        return PageResult.of(
            result.getTotal(),
            voList,
            (long) result.getCurrent(),
            (long) result.getSize()
        );
    }

    public PurchasePlanVO getPlanById(Long id) {
        PurchasePlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        List<PurchasePlanItem> items = planItemMapper.selectList(
            new LambdaQueryWrapper<PurchasePlanItem>()
                .eq(PurchasePlanItem::getPlanId, id)
                .orderByAsc(PurchasePlanItem::getId)
        );

        Map<Long, List<PurchasePlanItem>> itemMap = items.stream()
            .collect(Collectors.groupingBy(PurchasePlanItem::getPlanId));

        return convertToVO(plan, itemMap);
    }

    @Transactional(rollbackFor = Exception.class)
    public PurchasePlan generateReplenishmentPlan(String sourceType, Integer year, Integer month, String operator) {
        LocalDate now = LocalDate.now();
        if (year == null) year = now.getYear();
        if (month == null) month = now.getMonthValue();

        Map<String, BigDecimal> materialDemandMap = new HashMap<>();
        Map<String, String> materialInfoMap = new HashMap<>();

        if ("WORK_ORDER".equals(sourceType)) {
            calculateFromWorkOrders(year, month, materialDemandMap, materialInfoMap);
        } else if ("SAFETY_STOCK".equals(sourceType)) {
            calculateFromSafetyStock(materialDemandMap, materialInfoMap);
        } else {
            calculateFromWorkOrders(year, month, materialDemandMap, materialInfoMap);
            calculateFromSafetyStock(materialDemandMap, materialInfoMap);
        }

        if (materialDemandMap.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "未找到需要补货的物料，无需生成采购计划");
        }

        String planNo = generatePlanNo();
        String planName = "智能补货计划-" + year + "年" + month + "月";

        PurchasePlan plan = new PurchasePlan();
        plan.setPlanNo(planNo);
        plan.setPlanName(planName);
        plan.setPlanType(5);
        plan.setSourceType(sourceType != null ? sourceType : "BOTH");
        plan.setYear(year);
        plan.setMonth(month);
        plan.setStartDate(LocalDate.of(year, month, 1));
        plan.setEndDate(LocalDate.of(year, month, LocalDate.of(year, month, 1).lengthOfMonth()));
        plan.setItemCount(materialDemandMap.size());
        plan.setStatus("DRAFT");
        plan.setCreateBy(operator);
        plan.setCreateTime(LocalDateTime.now());

        List<PurchasePlanItem> planItems = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal estimatedAmount = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : materialDemandMap.entrySet()) {
            String materialCode = entry.getKey();
            BigDecimal demandQty = entry.getValue();

            MaterialInventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<MaterialInventory>()
                    .eq(MaterialInventory::getMaterialCode, materialCode)
                    .last("LIMIT 1")
            );

            BigDecimal stockQty = inventory != null && inventory.getAvailableQuantity() != null 
                ? inventory.getAvailableQuantity() : BigDecimal.ZERO;
            BigDecimal safetyStock = inventory != null && inventory.getSafetyStock() != null 
                ? inventory.getSafetyStock() : BigDecimal.ZERO;

            BigDecimal shortageQty = demandQty.add(safetyStock).subtract(stockQty);
            if (shortageQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal purchaseQty = shortageQty;
            BigDecimal unitPrice = inventory != null && inventory.getUnitPrice() != null 
                ? inventory.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal estimatedAmt = purchaseQty.multiply(unitPrice);

            totalQuantity = totalQuantity.add(purchaseQty);
            estimatedAmount = estimatedAmount.add(estimatedAmt);

            List<Supplier> suppliers = supplierMapper.selectList(
                new LambdaQueryWrapper<Supplier>()
                    .eq(Supplier::getStatus, 1)
                    .eq(Supplier::getMaterialCategory, String.valueOf(getMaterialCategory(materialInfoMap.get(materialCode))))
                    .orderByDesc(Supplier::getGrade)
                    .last("LIMIT 1")
            );

            PurchasePlanItem item = new PurchasePlanItem();
            item.setMaterialCode(materialCode);
            item.setMaterialName(getMaterialName(materialInfoMap.get(materialCode)));
            item.setMaterialSpec(getMaterialSpec(materialInfoMap.get(materialCode)));
            item.setMaterialUnit(getMaterialUnit(materialInfoMap.get(materialCode)));
            item.setMaterialCategory(String.valueOf(getMaterialCategory(materialInfoMap.get(materialCode))));
            item.setRequiredQuantity(demandQty);
            item.setStockQuantity(stockQty);
            item.setSafetyStock(safetyStock);
            item.setShortageQuantity(shortageQty);
            item.setPurchaseQuantity(purchaseQty);
            item.setUnitPrice(unitPrice);
            item.setEstimatedAmount(estimatedAmt);

            if (!suppliers.isEmpty()) {
                item.setRecommendedSupplierId(suppliers.get(0).getId());
                item.setRecommendedSupplierName(suppliers.get(0).getSupplierName());
            }

            item.setCreateBy(operator);
            item.setCreateTime(LocalDateTime.now());

            planItems.add(item);
        }

        if (planItems.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "所有物料库存充足，无需采购");
        }

        plan.setTotalQuantity(totalQuantity);
        plan.setEstimatedAmount(estimatedAmount);
        plan.setItemCount(planItems.size());

        planMapper.insert(plan);

        for (PurchasePlanItem item : planItems) {
            item.setPlanId(plan.getId());
            planItemMapper.insert(item);
        }

        log.info("生成智能补货计划成功，编号：{}", planNo);
        return plan;
    }

    private void calculateFromWorkOrders(Integer year, Integer month, 
            Map<String, BigDecimal> materialDemandMap, Map<String, String> materialInfoMap) {
        
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<ProductionWorkOrder> workOrders = workOrderMapper.selectList(
            new LambdaQueryWrapper<ProductionWorkOrder>()
                .in(ProductionWorkOrder::getStatus, 0, 1)
                .ge(ProductionWorkOrder::getPlanStartDate, startDate)
                .le(ProductionWorkOrder::getPlanEndDate, endDate)
        );

        for (ProductionWorkOrder workOrder : workOrders) {
            List<Bom> bomItems = bomMapper.selectList(
                new LambdaQueryWrapper<Bom>()
                    .eq(Bom::getParentCode, workOrder.getProductCode())
                    .orderByAsc(Bom::getSortOrder)
            );

            for (Bom bom : bomItems) {
                String materialCode = bom.getMaterialCode();
                BigDecimal usageQty = bom.getUsageQuantity() != null ? bom.getUsageQuantity() : BigDecimal.ZERO;
                BigDecimal planQty = workOrder.getPlanQuantity() != null ? workOrder.getPlanQuantity() : BigDecimal.ZERO;
                BigDecimal demandQty = usageQty.multiply(planQty);

                if (materialDemandMap.containsKey(materialCode)) {
                    materialDemandMap.put(materialCode, materialDemandMap.get(materialCode).add(demandQty));
                } else {
                    materialDemandMap.put(materialCode, demandQty);
                }

                String info = bom.getMaterialName() + "|" + (bom.getMaterialSpec() != null ? bom.getMaterialSpec() : "") + "|" +
                    (bom.getMaterialUnit() != null ? bom.getMaterialUnit() : "") + "|" + (bom.getMaterialCategory() != null ? bom.getMaterialCategory() : "");
                materialInfoMap.put(materialCode, info);
            }
        }
    }

    private void calculateFromSafetyStock(Map<String, BigDecimal> materialDemandMap, 
            Map<String, String> materialInfoMap) {
        
        List<MaterialInventory> lowStockItems = inventoryMapper.selectList(
            new LambdaQueryWrapper<MaterialInventory>()
                .isNotNull(MaterialInventory::getSafetyStock)
                .gt(MaterialInventory::getSafetyStock, BigDecimal.ZERO)
        );

        for (MaterialInventory inventory : lowStockItems) {
            String materialCode = inventory.getMaterialCode();
            BigDecimal safetyStock = inventory.getSafetyStock() != null ? inventory.getSafetyStock() : BigDecimal.ZERO;
            BigDecimal availableQty = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : BigDecimal.ZERO;
            BigDecimal demandQty = safetyStock.subtract(availableQty);

            if (demandQty.compareTo(BigDecimal.ZERO) > 0) {
                if (materialDemandMap.containsKey(materialCode)) {
                    materialDemandMap.put(materialCode, materialDemandMap.get(materialCode).add(demandQty));
                } else {
                    materialDemandMap.put(materialCode, demandQty);
                }

                String info = (inventory.getMaterialName() != null ? inventory.getMaterialName() : "") + "|" + 
                    (inventory.getMaterialSpec() != null ? inventory.getMaterialSpec() : "") + "|" +
                    (inventory.getMaterialUnit() != null ? inventory.getMaterialUnit() : "") + "|" + 
                    (inventory.getMaterialCategory() != null ? inventory.getMaterialCategory() : "");
                materialInfoMap.put(materialCode, info);
            }
        }
    }

    private String getMaterialName(String info) {
        if (info == null || info.isEmpty()) return "";
        String[] parts = info.split("\\|");
        return parts.length > 0 ? parts[0] : "";
    }

    private String getMaterialSpec(String info) {
        if (info == null || info.isEmpty()) return "";
        String[] parts = info.split("\\|");
        return parts.length > 1 ? parts[1] : "";
    }

    private String getMaterialUnit(String info) {
        if (info == null || info.isEmpty()) return "";
        String[] parts = info.split("\\|");
        return parts.length > 2 ? parts[2] : "";
    }

    private Integer getMaterialCategory(String info) {
        if (info == null || info.isEmpty()) return 1;
        String[] parts = info.split("\\|");
        if (parts.length > 3 && !parts[3].isEmpty()) {
            try {
                return Integer.parseInt(parts[3]);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    private String generatePlanNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "PP" + dateStr;

        LambdaQueryWrapper<PurchasePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(PurchasePlan::getPlanNo, prefix)
               .orderByDesc(PurchasePlan::getPlanNo)
               .last("LIMIT 1");

        PurchasePlan lastPlan = planMapper.selectOne(wrapper);

        int sequence = 1;
        if (lastPlan != null && lastPlan.getPlanNo() != null) {
            String lastNo = lastPlan.getPlanNo();
            int lastSeq = Integer.parseInt(lastNo.substring(lastNo.length() - 4));
            sequence = lastSeq + 1;
        }

        return prefix + String.format("%04d", sequence);
    }

    private PurchasePlanVO convertToVO(PurchasePlan plan, Map<Long, List<PurchasePlanItem>> itemMap) {
        PurchasePlanVO vo = PurchasePlanVO.builder()
            .id(plan.getId())
            .planNo(plan.getPlanNo())
            .planName(plan.getPlanName())
            .planType(plan.getPlanType())
            .planTypeName(getPlanTypeName(plan.getPlanType()))
            .sourceType(plan.getSourceType())
            .sourceTypeName(getSourceTypeName(plan.getSourceType()))
            .year(plan.getYear())
            .month(plan.getMonth())
            .quarter(plan.getQuarter())
            .startDate(plan.getStartDate())
            .endDate(plan.getEndDate())
            .itemCount(plan.getItemCount())
            .totalQuantity(plan.getTotalQuantity())
            .estimatedAmount(plan.getEstimatedAmount())
            .status(plan.getStatus())
            .statusName(getStatusName(plan.getStatus()))
            .remark(plan.getRemark())
            .createTime(plan.getCreateTime())
            .build();

        if (itemMap != null && itemMap.containsKey(plan.getId())) {
            List<PurchasePlanItem> items = itemMap.get(plan.getId());
            List<PurchasePlanItemVO> itemVOs = items.stream()
                .map(this::convertItemToVO)
                .collect(Collectors.toList());
            vo.setItems(itemVOs);
        }

        return vo;
    }

    private PurchasePlanItemVO convertItemToVO(PurchasePlanItem item) {
        return PurchasePlanItemVO.builder()
            .id(item.getId())
            .planId(item.getPlanId())
            .materialCode(item.getMaterialCode())
            .materialName(item.getMaterialName())
            .materialSpec(item.getMaterialSpec())
            .materialUnit(item.getMaterialUnit())
            .materialCategory(item.getMaterialCategory())
            .requiredQuantity(item.getRequiredQuantity())
            .stockQuantity(item.getStockQuantity())
            .safetyStock(item.getSafetyStock())
            .shortageQuantity(item.getShortageQuantity())
            .purchaseQuantity(item.getPurchaseQuantity())
            .unitPrice(item.getUnitPrice())
            .estimatedAmount(item.getEstimatedAmount())
            .recommendedSupplierId(item.getRecommendedSupplierId())
            .recommendedSupplierName(item.getRecommendedSupplierName())
            .remark(item.getRemark())
            .createTime(item.getCreateTime())
            .build();
    }

    private String getPlanTypeName(Integer planType) {
        if (planType == null) return "未知";
        switch (planType) {
            case 1: return "月度计划";
            case 2: return "季度计划";
            case 3: return "年度计划";
            case 4: return "紧急计划";
            case 5: return "补货计划";
            default: return "未知";
        }
    }

    private String getSourceTypeName(String sourceType) {
        if (sourceType == null) return "未知";
        switch (sourceType) {
            case "DEMAND_SUMMARY": return "需求汇总";
            case "WORK_ORDER": return "生产工单";
            case "SAFETY_STOCK": return "安全库存";
            case "MANUAL": return "人工创建";
            case "BOTH": return "综合计算";
            default: return "未知";
        }
    }

    private String getStatusName(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "DRAFT": return "草稿";
            case "SUBMITTED": return "已提交";
            case "APPROVED": return "已审批";
            case "EXECUTING": return "执行中";
            case "COMPLETED": return "已完成";
            case "CANCELLED": return "已取消";
            default: return "未知";
        }
    }
}
