package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.ProductionProgressQueryDTO;
import com.baserbac.scm.entity.ProductionProgress;
import com.baserbac.scm.entity.PurchaseOrder;
import com.baserbac.scm.entity.PurchaseOrderItem;
import com.baserbac.scm.mapper.ProductionProgressMapper;
import com.baserbac.scm.mapper.PurchaseOrderItemMapper;
import com.baserbac.scm.mapper.PurchaseOrderMapper;
import com.baserbac.scm.vo.ProductionProgressVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionProgressService {

    private final ProductionProgressMapper progressMapper;
    private final PurchaseOrderMapper orderMapper;
    private final PurchaseOrderItemMapper itemMapper;

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();

    static {
        STATUS_MAP.put(0, "待开始");
        STATUS_MAP.put(1, "进行中");
        STATUS_MAP.put(2, "已完成");
        STATUS_MAP.put(3, "已暂停");
        STATUS_MAP.put(4, "已延误");
    }

    public PageResult<ProductionProgressVO> pageProgress(ProductionProgressQueryDTO queryDTO) {
        Page<ProductionProgress> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<ProductionProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getOrderId() != null, ProductionProgress::getOrderId, queryDTO.getOrderId())
                .like(queryDTO.getOrderNo() != null, ProductionProgress::getOrderNo, queryDTO.getOrderNo())
                .like(queryDTO.getMaterialCode() != null, ProductionProgress::getMaterialCode, queryDTO.getMaterialCode())
                .like(queryDTO.getMaterialName() != null, ProductionProgress::getMaterialName, queryDTO.getMaterialName())
                .eq(queryDTO.getProgressStatus() != null, ProductionProgress::getProgressStatus, queryDTO.getProgressStatus())
                .like(queryDTO.getResponsiblePerson() != null, ProductionProgress::getResponsiblePerson, queryDTO.getResponsiblePerson())
                .orderByDesc(ProductionProgress::getCreateTime);

        Page<ProductionProgress> result = progressMapper.selectPage(page, wrapper);

        List<ProductionProgressVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(
                result.getTotal(),
                voList,
                (long) result.getCurrent(),
                (long) result.getSize()
        );
    }

    public ProductionProgressVO getProgressById(Long id) {
        ProductionProgress progress = progressMapper.selectById(id);
        if (progress == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(progress);
    }

    public List<ProductionProgressVO> getProgressByOrderId(Long orderId) {
        List<ProductionProgress> progressList = progressMapper.selectList(
                new LambdaQueryWrapper<ProductionProgress>()
                        .eq(ProductionProgress::getOrderId, orderId)
                        .eq(ProductionProgress::getIsDeleted, 0)
                        .orderByAsc(ProductionProgress::getId)
        );

        return progressList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void createProgressForOrder(Long orderId) {
        PurchaseOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        List<PurchaseOrderItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getOrderId, orderId)
                        .eq(PurchaseOrderItem::getIsDeleted, 0)
        );

        if (items.isEmpty()) {
            throw new BusinessException(4001, "订单没有明细项");
        }

        for (PurchaseOrderItem item : items) {
            ProductionProgress progress = new ProductionProgress();
            progress.setOrderId(order.getId());
            progress.setOrderNo(order.getOrderNo());
            progress.setOrderItemId(item.getId());
            progress.setMaterialCode(item.getMaterialCode());
            progress.setMaterialName(item.getMaterialName());
            progress.setMaterialSpec(item.getMaterialSpec());
            progress.setMaterialUnit(item.getMaterialUnit());
            progress.setTotalQuantity(item.getQuantity());
            progress.setCompletedQuantity(BigDecimal.ZERO);
            progress.setProgressRate(BigDecimal.ZERO);
            progress.setProgressStatus(0);
            progress.setWorkStation("生产车间-" + (item.getId() % 5 + 1));
            progress.setResponsiblePerson("生产负责人-" + (item.getId() % 10 + 1));
            progress.setEstimatedStartDate(LocalDate.now());
            progress.setEstimatedFinishDate(LocalDate.now().plusDays(7));
            progress.setIsDeleted(0);

            progressMapper.insert(progress);
        }

        order.setStatus(6);
        orderMapper.updateById(order);

        log.info("为订单创建生产进度记录: orderNo={}, itemCount={}", order.getOrderNo(), items.size());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long id, BigDecimal completedQuantity, String remark) {
        ProductionProgress progress = progressMapper.selectById(id);
        if (progress == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (completedQuantity != null) {
            progress.setCompletedQuantity(completedQuantity);
            if (progress.getTotalQuantity().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal rate = completedQuantity.multiply(new BigDecimal(100))
                        .divide(progress.getTotalQuantity(), 2, RoundingMode.HALF_UP);
                progress.setProgressRate(rate);

                if (rate.compareTo(new BigDecimal(100)) >= 0) {
                    progress.setProgressStatus(2);
                    progress.setActualFinishDate(LocalDate.now());
                } else if (rate.compareTo(BigDecimal.ZERO) > 0) {
                    progress.setProgressStatus(1);
                    if (progress.getActualStartDate() == null) {
                        progress.setActualStartDate(LocalDate.now());
                    }
                }
            }
        }

        if (remark != null) {
            progress.setRemark(remark);
        }

        progressMapper.updateById(progress);
        log.info("更新生产进度: id={}, completedQuantity={}", id, completedQuantity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProgressStatus(Long id, Integer status) {
        ProductionProgress progress = progressMapper.selectById(id);
        if (progress == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        progress.setProgressStatus(status);

        if (status == 1 && progress.getActualStartDate() == null) {
            progress.setActualStartDate(LocalDate.now());
        } else if (status == 2) {
            progress.setActualFinishDate(LocalDate.now());
            progress.setCompletedQuantity(progress.getTotalQuantity());
            progress.setProgressRate(new BigDecimal(100));
        }

        progressMapper.updateById(progress);
    }

    private ProductionProgressVO convertToVO(ProductionProgress progress) {
        return ProductionProgressVO.builder()
                .id(progress.getId())
                .orderId(progress.getOrderId())
                .orderNo(progress.getOrderNo())
                .orderItemId(progress.getOrderItemId())
                .materialCode(progress.getMaterialCode())
                .materialName(progress.getMaterialName())
                .materialSpec(progress.getMaterialSpec())
                .materialUnit(progress.getMaterialUnit())
                .totalQuantity(progress.getTotalQuantity())
                .completedQuantity(progress.getCompletedQuantity())
                .progressRate(progress.getProgressRate())
                .progressStatus(progress.getProgressStatus())
                .progressStatusName(STATUS_MAP.getOrDefault(progress.getProgressStatus(), "未知"))
                .workStation(progress.getWorkStation())
                .responsiblePerson(progress.getResponsiblePerson())
                .estimatedStartDate(progress.getEstimatedStartDate())
                .actualStartDate(progress.getActualStartDate())
                .estimatedFinishDate(progress.getEstimatedFinishDate())
                .actualFinishDate(progress.getActualFinishDate())
                .remark(progress.getRemark())
                .createTime(progress.getCreateTime())
                .updateTime(progress.getUpdateTime())
                .build();
    }

    public void addTestData() {
        List<PurchaseOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .eq(PurchaseOrder::getIsDeleted, 0)
                        .eq(PurchaseOrder::getStatus, 5)
                        .orderByAsc(PurchaseOrder::getId)
                        .last("LIMIT 20")
        );

        if (orders.isEmpty()) {
            log.warn("没有已确认的订单数据，无法生成生产进度测试数据");
            return;
        }

        for (PurchaseOrder order : orders) {
            try {
                createProgressForOrder(order.getId());

                List<ProductionProgress> progressList = progressMapper.selectList(
                        new LambdaQueryWrapper<ProductionProgress>()
                                .eq(ProductionProgress::getOrderId, order.getId())
                                .eq(ProductionProgress::getIsDeleted, 0)
                );

                for (int i = 0; i < progressList.size(); i++) {
                    ProductionProgress progress = progressList.get(i);
                    double rate = 30 + Math.random() * 70;
                    BigDecimal completedQty = progress.getTotalQuantity()
                            .multiply(new BigDecimal(rate / 100))
                            .setScale(2, RoundingMode.HALF_UP);

                    updateProgress(progress.getId(), completedQty, "生产进度更新-" + (i + 1));
                }

            } catch (Exception e) {
                log.warn("生成生产进度测试数据失败: orderNo={}, {}", order.getOrderNo(), e.getMessage());
            }
        }

        log.info("生产进度测试数据生成完成");
    }
}
