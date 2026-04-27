package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.DemandSummaryQueryDTO;
import com.baserbac.scm.entity.DemandSummary;
import com.baserbac.scm.entity.DemandSummaryItem;
import com.baserbac.scm.entity.PurchaseRequest;
import com.baserbac.scm.entity.PurchaseRequestItem;
import com.baserbac.scm.mapper.DemandSummaryItemMapper;
import com.baserbac.scm.mapper.DemandSummaryMapper;
import com.baserbac.scm.mapper.PurchaseRequestItemMapper;
import com.baserbac.scm.mapper.PurchaseRequestMapper;
import com.baserbac.scm.vo.DemandSummaryItemVO;
import com.baserbac.scm.vo.DemandSummaryVO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemandSummaryService {

    private final DemandSummaryMapper summaryMapper;
    private final DemandSummaryItemMapper itemMapper;
    private final PurchaseRequestMapper requestMapper;
    private final PurchaseRequestItemMapper requestItemMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public PageResult<DemandSummaryVO> pageSummaries(DemandSummaryQueryDTO queryDTO) {
        Page<DemandSummary> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<DemandSummary> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getSummaryNo() != null, DemandSummary::getSummaryNo, queryDTO.getSummaryNo())
               .like(queryDTO.getSummaryName() != null, DemandSummary::getSummaryName, queryDTO.getSummaryName())
               .like(queryDTO.getMaterialCategory() != null, DemandSummary::getMaterialCategory, queryDTO.getMaterialCategory())
               .eq(queryDTO.getPeriodType() != null, DemandSummary::getPeriodType, queryDTO.getPeriodType())
               .eq(queryDTO.getYear() != null, DemandSummary::getYear, queryDTO.getYear())
               .eq(queryDTO.getMonth() != null, DemandSummary::getMonth, queryDTO.getMonth())
               .eq(queryDTO.getStatus() != null, DemandSummary::getStatus, queryDTO.getStatus())
               .orderByDesc(DemandSummary::getCreateTime);

        Page<DemandSummary> result = summaryMapper.selectPage(page, wrapper);

        List<Long> summaryIds = result.getRecords().stream()
            .map(DemandSummary::getId)
            .collect(Collectors.toList());

        Map<Long, List<DemandSummaryItem>> itemMap = null;
        if (!summaryIds.isEmpty()) {
            List<DemandSummaryItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<DemandSummaryItem>()
                    .in(DemandSummaryItem::getSummaryId, summaryIds)
                    .orderByAsc(DemandSummaryItem::getId)
            );
            itemMap = items.stream()
                .collect(Collectors.groupingBy(DemandSummaryItem::getSummaryId));
        }

        Map<Long, List<DemandSummaryItem>> finalItemMap = itemMap;
        List<DemandSummaryVO> voList = result.getRecords().stream()
            .map(s -> convertToVO(s, finalItemMap))
            .collect(Collectors.toList());

        return PageResult.of(
            result.getTotal(),
            voList,
            (long) result.getCurrent(),
            (long) result.getSize()
        );
    }

    public DemandSummaryVO getSummaryById(Long id) {
        DemandSummary summary = summaryMapper.selectById(id);
        if (summary == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        List<DemandSummaryItem> items = itemMapper.selectList(
            new LambdaQueryWrapper<DemandSummaryItem>()
                .eq(DemandSummaryItem::getSummaryId, id)
                .orderByAsc(DemandSummaryItem::getId)
        );

        Map<Long, List<DemandSummaryItem>> itemMap = items.stream()
            .collect(Collectors.groupingBy(DemandSummaryItem::getSummaryId));

        return convertToVO(summary, itemMap);
    }

    @Transactional(rollbackFor = Exception.class)
    public DemandSummary generateSummary(Integer periodType, Integer year, Integer month, String materialCategory, String operator) {
        LocalDate now = LocalDate.now();
        if (year == null) year = now.getYear();
        if (month == null) month = now.getMonthValue();
        if (periodType == null) periodType = 1;

        LocalDate startDate;
        LocalDate endDate;

        if (periodType == 1) {
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        } else if (periodType == 2) {
            int startMonth = ((month - 1) / 3) * 3 + 1;
            startDate = LocalDate.of(year, startMonth, 1);
            endDate = startDate.plusMonths(3).minusDays(1);
        } else {
            startDate = LocalDate.of(year, 1, 1);
            endDate = LocalDate.of(year, 12, 31);
        }

        List<PurchaseRequest> requests = requestMapper.selectList(
            new LambdaQueryWrapper<PurchaseRequest>()
                .in(PurchaseRequest::getStatus, 2, 3)
                .ge(PurchaseRequest::getCreateTime, startDate.atStartOfDay())
                .le(PurchaseRequest::getCreateTime, endDate.atTime(23, 59, 59))
                .orderByAsc(PurchaseRequest::getCreateTime)
        );

        if (requests.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "未找到指定周期内的采购申请单，无法生成需求汇总");
        }

        List<Long> requestIds = requests.stream()
            .map(PurchaseRequest::getId)
            .collect(Collectors.toList());

        List<PurchaseRequestItem> allItems = requestItemMapper.selectList(
            new LambdaQueryWrapper<PurchaseRequestItem>()
                .in(PurchaseRequestItem::getRequestId, requestIds)
        );

        if (materialCategory != null && !materialCategory.isEmpty()) {
            allItems = allItems.stream()
                .filter(item -> materialCategory.equals(item.getMaterialCategory()))
                .collect(Collectors.toList());
        }

        if (allItems.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "未找到符合条件的采购明细，无法生成需求汇总");
        }

        Map<String, List<PurchaseRequestItem>> groupedItems = allItems.stream()
            .collect(Collectors.groupingBy(
                item -> item.getMaterialCode() + "_" + item.getMaterialSpec()
            ));

        String summaryNo = generateSummaryNo();
        String summaryName = "需求汇总-" + year + "年" + (periodType == 1 ? month + "月" : periodType == 2 ? "第" + ((month - 1) / 3 + 1) + "季度" : "年度");

        DemandSummary summary = new DemandSummary();
        summary.setSummaryNo(summaryNo);
        summary.setSummaryName(summaryName);
        summary.setMaterialCategory(materialCategory);
        summary.setPeriodType(periodType);
        summary.setYear(year);
        summary.setMonth(month);
        summary.setStartDate(startDate);
        summary.setEndDate(endDate);
        summary.setRequestCount(requests.size());
        summary.setItemCount(groupedItems.size());
        summary.setStatus("DRAFT");
        summary.setCreateBy(operator);
        summary.setCreateTime(LocalDateTime.now());

        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal estimatedAmount = BigDecimal.ZERO;

        List<DemandSummaryItem> summaryItems = new ArrayList<>();

        for (Map.Entry<String, List<PurchaseRequestItem>> entry : groupedItems.entrySet()) {
            List<PurchaseRequestItem> items = entry.getValue();
            PurchaseRequestItem firstItem = items.get(0);

            BigDecimal itemTotalQty = items.stream()
                .map(PurchaseRequestItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgUnitPrice = items.stream()
                .filter(item -> item.getUnitPrice() != null)
                .map(PurchaseRequestItem::getUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(items.size()), 2, RoundingMode.HALF_UP);

            BigDecimal itemEstimatedAmount = itemTotalQty.multiply(avgUnitPrice);

            totalQuantity = totalQuantity.add(itemTotalQty);
            estimatedAmount = estimatedAmount.add(itemEstimatedAmount);

            DemandSummaryItem summaryItem = new DemandSummaryItem();
            summaryItem.setMaterialCode(firstItem.getMaterialCode());
            summaryItem.setMaterialName(firstItem.getMaterialName());
            summaryItem.setMaterialSpec(firstItem.getMaterialSpec());
            summaryItem.setMaterialUnit(firstItem.getMaterialUnit());
            summaryItem.setMaterialCategory(firstItem.getMaterialCategory());
            summaryItem.setSourceRequestCount(items.size());
            summaryItem.setTotalQuantity(itemTotalQty);
            summaryItem.setAvgUnitPrice(avgUnitPrice);
            summaryItem.setEstimatedAmount(itemEstimatedAmount);
            summaryItem.setCreateBy(operator);
            summaryItem.setCreateTime(LocalDateTime.now());

            summaryItems.add(summaryItem);
        }

        summary.setTotalQuantity(totalQuantity);
        summary.setEstimatedAmount(estimatedAmount);

        summaryMapper.insert(summary);

        for (DemandSummaryItem item : summaryItems) {
            item.setSummaryId(summary.getId());
            itemMapper.insert(item);
        }

        log.info("生成需求汇总成功，编号：{}", summaryNo);
        return summary;
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmSummary(Long id, String operator) {
        DemandSummary summary = summaryMapper.selectById(id);
        if (summary == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (!"DRAFT".equals(summary.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "当前状态不允许确认");
        }

        summary.setStatus("CONFIRMED");
        summary.setUpdateBy(operator);
        summary.setUpdateTime(LocalDateTime.now());

        summaryMapper.updateById(summary);

        log.info("确认需求汇总，ID：{}", id);
    }

    private String generateSummaryNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "DS" + dateStr;

        LambdaQueryWrapper<DemandSummary> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(DemandSummary::getSummaryNo, prefix)
               .orderByDesc(DemandSummary::getSummaryNo)
               .last("LIMIT 1");

        DemandSummary lastSummary = summaryMapper.selectOne(wrapper);

        int sequence = 1;
        if (lastSummary != null && lastSummary.getSummaryNo() != null) {
            String lastNo = lastSummary.getSummaryNo();
            int lastSeq = Integer.parseInt(lastNo.substring(lastNo.length() - 4));
            sequence = lastSeq + 1;
        }

        return prefix + String.format("%04d", sequence);
    }

    private DemandSummaryVO convertToVO(DemandSummary summary, Map<Long, List<DemandSummaryItem>> itemMap) {
        DemandSummaryVO vo = DemandSummaryVO.builder()
            .id(summary.getId())
            .summaryNo(summary.getSummaryNo())
            .summaryName(summary.getSummaryName())
            .materialCategory(summary.getMaterialCategory())
            .periodType(summary.getPeriodType())
            .periodTypeName(getPeriodTypeName(summary.getPeriodType()))
            .year(summary.getYear())
            .month(summary.getMonth())
            .startDate(summary.getStartDate())
            .endDate(summary.getEndDate())
            .requestCount(summary.getRequestCount())
            .itemCount(summary.getItemCount())
            .totalQuantity(summary.getTotalQuantity())
            .estimatedAmount(summary.getEstimatedAmount())
            .status(summary.getStatus())
            .statusName(getStatusName(summary.getStatus()))
            .remark(summary.getRemark())
            .createTime(summary.getCreateTime())
            .build();

        if (itemMap != null && itemMap.containsKey(summary.getId())) {
            List<DemandSummaryItem> items = itemMap.get(summary.getId());
            List<DemandSummaryItemVO> itemVOs = items.stream()
                .map(this::convertItemToVO)
                .collect(Collectors.toList());
            vo.setItems(itemVOs);
        }

        return vo;
    }

    private DemandSummaryItemVO convertItemToVO(DemandSummaryItem item) {
        return DemandSummaryItemVO.builder()
            .id(item.getId())
            .summaryId(item.getSummaryId())
            .materialCode(item.getMaterialCode())
            .materialName(item.getMaterialName())
            .materialSpec(item.getMaterialSpec())
            .materialUnit(item.getMaterialUnit())
            .materialCategory(item.getMaterialCategory())
            .sourceRequestCount(item.getSourceRequestCount())
            .totalQuantity(item.getTotalQuantity())
            .avgUnitPrice(item.getAvgUnitPrice())
            .estimatedAmount(item.getEstimatedAmount())
            .remark(item.getRemark())
            .createTime(item.getCreateTime())
            .build();
    }

    private String getPeriodTypeName(Integer periodType) {
        if (periodType == null) return "未知";
        switch (periodType) {
            case 1: return "月度";
            case 2: return "季度";
            case 3: return "年度";
            default: return "未知";
        }
    }

    private String getStatusName(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "DRAFT": return "草稿";
            case "CONFIRMED": return "已确认";
            case "PROCESSED": return "已处理";
            default: return "未知";
        }
    }
}
