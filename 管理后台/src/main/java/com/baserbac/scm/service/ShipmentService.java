package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.ShipmentQueryDTO;
import com.baserbac.scm.entity.LogisticsTrack;
import com.baserbac.scm.entity.PurchaseOrder;
import com.baserbac.scm.entity.PurchaseOrderItem;
import com.baserbac.scm.entity.Shipment;
import com.baserbac.scm.mapper.LogisticsTrackMapper;
import com.baserbac.scm.mapper.PurchaseOrderItemMapper;
import com.baserbac.scm.mapper.PurchaseOrderMapper;
import com.baserbac.scm.mapper.ShipmentMapper;
import com.baserbac.scm.vo.LogisticsTrackVO;
import com.baserbac.scm.vo.ShipmentVO;
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
public class ShipmentService {

    private final ShipmentMapper shipmentMapper;
    private final LogisticsTrackMapper trackMapper;
    private final PurchaseOrderMapper orderMapper;
    private final PurchaseOrderItemMapper itemMapper;

    private static final String SHIPMENT_NO_PREFIX = "SH";

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    private static final Map<Integer, String> TYPE_MAP = new HashMap<>();

    static {
        STATUS_MAP.put(0, "待发货");
        STATUS_MAP.put(1, "已发货");
        STATUS_MAP.put(2, "运输中");
        STATUS_MAP.put(3, "已送达");
        STATUS_MAP.put(4, "已签收");

        TYPE_MAP.put(1, "正常发货");
        TYPE_MAP.put(2, "紧急发货");
        TYPE_MAP.put(3, "补发");
    }

    public PageResult<ShipmentVO> pageShipments(ShipmentQueryDTO queryDTO) {
        Page<Shipment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Shipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getShipmentNo() != null, Shipment::getShipmentNo, queryDTO.getShipmentNo())
                .eq(queryDTO.getOrderId() != null, Shipment::getOrderId, queryDTO.getOrderId())
                .like(queryDTO.getOrderNo() != null, Shipment::getOrderNo, queryDTO.getOrderNo())
                .eq(queryDTO.getSupplierId() != null, Shipment::getSupplierId, queryDTO.getSupplierId())
                .like(queryDTO.getSupplierName() != null, Shipment::getSupplierName, queryDTO.getSupplierName())
                .eq(queryDTO.getShipmentType() != null, Shipment::getShipmentType, queryDTO.getShipmentType())
                .eq(queryDTO.getStatus() != null, Shipment::getStatus, queryDTO.getStatus())
                .orderByDesc(Shipment::getCreateTime);

        Page<Shipment> result = shipmentMapper.selectPage(page, wrapper);

        List<ShipmentVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(
                result.getTotal(),
                voList,
                (long) result.getCurrent(),
                (long) result.getSize()
        );
    }

    public ShipmentVO getShipmentById(Long id) {
        Shipment shipment = shipmentMapper.selectById(id);
        if (shipment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(shipment);
    }

    public List<LogisticsTrackVO> getLogisticsTracks(Long shipmentId) {
        List<LogisticsTrack> tracks = trackMapper.selectList(
                new LambdaQueryWrapper<LogisticsTrack>()
                        .eq(LogisticsTrack::getShipmentId, shipmentId)
                        .eq(LogisticsTrack::getIsDeleted, 0)
                        .orderByAsc(LogisticsTrack::getTrackTime)
        );

        return tracks.stream()
                .map(this::convertTrackToVO)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public Shipment createShipment(Long orderId) {
        PurchaseOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        List<PurchaseOrderItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getOrderId, orderId)
                        .eq(PurchaseOrderItem::getIsDeleted, 0)
        );

        Shipment shipment = new Shipment();
        shipment.setShipmentNo(generateShipmentNo());
        shipment.setOrderId(order.getId());
        shipment.setOrderNo(order.getOrderNo());
        shipment.setSupplierId(order.getSupplierId());
        shipment.setSupplierName(order.getSupplierName());
        shipment.setShipmentType(1);
        shipment.setShipmentDate(LocalDate.now());
        shipment.setEstimatedArrivalDate(LocalDate.now().plusDays(3));
        shipment.setItemCount(items.size());
        shipment.setTotalQuantity(order.getTotalQuantity());
        shipment.setTotalWeight(order.getTotalQuantity().multiply(new BigDecimal(10)));
        shipment.setShippingMethod("公路运输");
        shipment.setCarrier("某某物流公司");
        shipment.setWaybillNo("WL" + System.currentTimeMillis());
        shipment.setDeparturePlace(order.getSupplierName() + "仓库");
        shipment.setDestination(order.getDeliveryAddress());
        shipment.setContactPerson(order.getContactPerson());
        shipment.setContactPhone(order.getContactPhone());
        shipment.setStatus(1);
        shipment.setIsDeleted(0);

        shipmentMapper.insert(shipment);

        order.setStatus(7);
        orderMapper.updateById(order);

        addInitialTrack(shipment);

        log.info("创建发货记录: shipmentNo={}, orderNo={}", shipment.getShipmentNo(), order.getOrderNo());
        return shipment;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateShipmentStatus(Long id, Integer status, String location, String description) {
        Shipment shipment = shipmentMapper.selectById(id);
        if (shipment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        shipment.setStatus(status);

        if (status == 3) {
            shipment.setActualArrivalDate(LocalDate.now());
        }

        shipmentMapper.updateById(shipment);

        if (location != null || description != null) {
            LogisticsTrack track = new LogisticsTrack();
            track.setShipmentId(shipment.getId());
            track.setShipmentNo(shipment.getShipmentNo());
            track.setOrderId(shipment.getOrderId());
            track.setOrderNo(shipment.getOrderNo());
            track.setWaybillNo(shipment.getWaybillNo());
            track.setCarrier(shipment.getCarrier());
            track.setTrackTime(LocalDateTime.now());
            track.setLocation(location != null ? location : "当前位置");
            track.setStatus(status);
            track.setStatusName(STATUS_MAP.getOrDefault(status, "未知"));
            track.setDescription(description != null ? description : "物流状态更新");
            track.setIsDeleted(0);

            trackMapper.insert(track);
        }

        log.info("更新发货状态: shipmentNo={}, status={}", shipment.getShipmentNo(), status);
    }

    private void addInitialTrack(Shipment shipment) {
        LogisticsTrack track = new LogisticsTrack();
        track.setShipmentId(shipment.getId());
        track.setShipmentNo(shipment.getShipmentNo());
        track.setOrderId(shipment.getOrderId());
        track.setOrderNo(shipment.getOrderNo());
        track.setWaybillNo(shipment.getWaybillNo());
        track.setCarrier(shipment.getCarrier());
        track.setTrackTime(LocalDateTime.now());
        track.setLocation(shipment.getDeparturePlace());
        track.setStatus(1);
        track.setStatusName("已发货");
        track.setDescription("货物已从供应商仓库发出");
        track.setIsDeleted(0);

        trackMapper.insert(track);
    }

    private String generateShipmentNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return SHIPMENT_NO_PREFIX + dateStr + System.currentTimeMillis();
    }

    private ShipmentVO convertToVO(Shipment shipment) {
        return ShipmentVO.builder()
                .id(shipment.getId())
                .shipmentNo(shipment.getShipmentNo())
                .orderId(shipment.getOrderId())
                .orderNo(shipment.getOrderNo())
                .supplierId(shipment.getSupplierId())
                .supplierName(shipment.getSupplierName())
                .shipmentType(shipment.getShipmentType())
                .shipmentTypeName(TYPE_MAP.getOrDefault(shipment.getShipmentType(), "未知"))
                .shipmentDate(shipment.getShipmentDate())
                .estimatedArrivalDate(shipment.getEstimatedArrivalDate())
                .actualArrivalDate(shipment.getActualArrivalDate())
                .itemCount(shipment.getItemCount())
                .totalQuantity(shipment.getTotalQuantity())
                .totalWeight(shipment.getTotalWeight())
                .shippingMethod(shipment.getShippingMethod())
                .carrier(shipment.getCarrier())
                .waybillNo(shipment.getWaybillNo())
                .departurePlace(shipment.getDeparturePlace())
                .destination(shipment.getDestination())
                .contactPerson(shipment.getContactPerson())
                .contactPhone(shipment.getContactPhone())
                .status(shipment.getStatus())
                .statusName(STATUS_MAP.getOrDefault(shipment.getStatus(), "未知"))
                .remark(shipment.getRemark())
                .createTime(shipment.getCreateTime())
                .updateTime(shipment.getUpdateTime())
                .build();
    }

    private LogisticsTrackVO convertTrackToVO(LogisticsTrack track) {
        return LogisticsTrackVO.builder()
                .id(track.getId())
                .shipmentId(track.getShipmentId())
                .shipmentNo(track.getShipmentNo())
                .orderId(track.getOrderId())
                .orderNo(track.getOrderNo())
                .waybillNo(track.getWaybillNo())
                .carrier(track.getCarrier())
                .trackTime(track.getTrackTime())
                .location(track.getLocation())
                .status(track.getStatus())
                .statusName(track.getStatusName())
                .description(track.getDescription())
                .operator(track.getOperator())
                .remark(track.getRemark())
                .createTime(track.getCreateTime())
                .updateTime(track.getUpdateTime())
                .build();
    }

    public void addTestData() {
        List<PurchaseOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .eq(PurchaseOrder::getIsDeleted, 0)
                        .eq(PurchaseOrder::getStatus, 6)
                        .orderByAsc(PurchaseOrder::getId)
                        .last("LIMIT 20")
        );

        if (orders.isEmpty()) {
            log.warn("没有生产中的订单数据，无法生成发货测试数据");
            return;
        }

        String[] carriers = {"顺丰物流", "德邦物流", "中铁物流", "安能物流", "天地华宇"};
        String[] locations = {"北京市转运中心", "上海市转运中心", "广州市转运中心", "深圳市转运中心", "杭州市转运中心"};

        for (PurchaseOrder order : orders) {
            try {
                Shipment shipment = createShipment(order.getId());

                for (int i = 0; i < 3; i++) {
                    LocalDateTime trackTime = LocalDateTime.now().plusHours((i + 1) * 4);
                    String location = locations[i % locations.length];
                    String description = "货物已到达" + location + "，正在处理中";

                    LogisticsTrack track = new LogisticsTrack();
                    track.setShipmentId(shipment.getId());
                    track.setShipmentNo(shipment.getShipmentNo());
                    track.setOrderId(order.getId());
                    track.setOrderNo(order.getOrderNo());
                    track.setWaybillNo(shipment.getWaybillNo());
                    track.setCarrier(carriers[order.getId().intValue() % carriers.length]);
                    track.setTrackTime(trackTime);
                    track.setLocation(location);
                    track.setStatus(2);
                    track.setStatusName("运输中");
                    track.setDescription(description);
                    track.setIsDeleted(0);

                    trackMapper.insert(track);
                }

            } catch (Exception e) {
                log.warn("生成发货测试数据失败: orderNo={}, {}", order.getOrderNo(), e.getMessage());
            }
        }

        log.info("发货测试数据生成完成");
    }
}
