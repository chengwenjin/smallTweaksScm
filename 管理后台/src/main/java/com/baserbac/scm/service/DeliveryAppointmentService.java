package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.DeliveryAppointmentCreateDTO;
import com.baserbac.scm.dto.DeliveryAppointmentQueryDTO;
import com.baserbac.scm.entity.DeliveryAppointment;
import com.baserbac.scm.entity.PurchaseOrder;
import com.baserbac.scm.entity.Shipment;
import com.baserbac.scm.mapper.DeliveryAppointmentMapper;
import com.baserbac.scm.mapper.PurchaseOrderMapper;
import com.baserbac.scm.mapper.ShipmentMapper;
import com.baserbac.scm.vo.DeliveryAppointmentVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryAppointmentService {

    private final DeliveryAppointmentMapper appointmentMapper;
    private final PurchaseOrderMapper orderMapper;
    private final ShipmentMapper shipmentMapper;

    private static final String APPOINTMENT_NO_PREFIX = "DA";

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();

    static {
        STATUS_MAP.put(0, "待确认");
        STATUS_MAP.put(1, "已确认");
        STATUS_MAP.put(2, "已签到");
        STATUS_MAP.put(3, "已完成");
        STATUS_MAP.put(4, "已取消");
    }

    public PageResult<DeliveryAppointmentVO> pageAppointments(DeliveryAppointmentQueryDTO queryDTO) {
        Page<DeliveryAppointment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<DeliveryAppointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getAppointmentNo() != null, DeliveryAppointment::getAppointmentNo, queryDTO.getAppointmentNo())
                .eq(queryDTO.getOrderId() != null, DeliveryAppointment::getOrderId, queryDTO.getOrderId())
                .like(queryDTO.getOrderNo() != null, DeliveryAppointment::getOrderNo, queryDTO.getOrderNo())
                .eq(queryDTO.getSupplierId() != null, DeliveryAppointment::getSupplierId, queryDTO.getSupplierId())
                .like(queryDTO.getSupplierName() != null, DeliveryAppointment::getSupplierName, queryDTO.getSupplierName())
                .eq(queryDTO.getAppointmentDate() != null, DeliveryAppointment::getAppointmentDate, queryDTO.getAppointmentDate())
                .eq(queryDTO.getWarehouseId() != null, DeliveryAppointment::getWarehouseId, queryDTO.getWarehouseId())
                .eq(queryDTO.getStatus() != null, DeliveryAppointment::getStatus, queryDTO.getStatus())
                .orderByDesc(DeliveryAppointment::getAppointmentDate)
                .orderByAsc(DeliveryAppointment::getAppointmentTimeFrom);

        Page<DeliveryAppointment> result = appointmentMapper.selectPage(page, wrapper);

        List<DeliveryAppointmentVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(
                result.getTotal(),
                voList,
                (long) result.getCurrent(),
                (long) result.getSize()
        );
    }

    public DeliveryAppointmentVO getAppointmentById(Long id) {
        DeliveryAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(appointment);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createAppointment(DeliveryAppointmentCreateDTO createDTO) {
        PurchaseOrder order = orderMapper.selectById(createDTO.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        DeliveryAppointment appointment = new DeliveryAppointment();
        appointment.setAppointmentNo(generateAppointmentNo());
        appointment.setOrderId(createDTO.getOrderId());
        appointment.setOrderNo(order.getOrderNo());
        appointment.setSupplierId(order.getSupplierId());
        appointment.setSupplierName(order.getSupplierName());

        if (createDTO.getShipmentId() != null) {
            Shipment shipment = shipmentMapper.selectById(createDTO.getShipmentId());
            if (shipment != null) {
                appointment.setShipmentId(shipment.getId());
                appointment.setShipmentNo(shipment.getShipmentNo());
            }
        }

        appointment.setAppointmentDate(createDTO.getAppointmentDate());
        appointment.setAppointmentTimeFrom(createDTO.getAppointmentTimeFrom());
        appointment.setAppointmentTimeTo(createDTO.getAppointmentTimeTo());
        appointment.setDeliveryAddress(createDTO.getDeliveryAddress() != null ? createDTO.getDeliveryAddress() : order.getDeliveryAddress());
        appointment.setWarehouseId(createDTO.getWarehouseId());
        appointment.setWarehouseName(createDTO.getWarehouseName());
        appointment.setDockNo(createDTO.getDockNo());
        appointment.setContactPerson(createDTO.getContactPerson());
        appointment.setContactPhone(createDTO.getContactPhone());
        appointment.setItemCount(order.getItemCount());
        appointment.setTotalQuantity(order.getTotalQuantity());
        appointment.setTotalWeight(order.getTotalQuantity().multiply(new BigDecimal(10)));
        appointment.setTotalVolume(order.getTotalQuantity().multiply(new BigDecimal(0.5)));
        appointment.setVehicleNo(createDTO.getVehicleNo());
        appointment.setDriverName(createDTO.getDriverName());
        appointment.setDriverPhone(createDTO.getDriverPhone());
        appointment.setStatus(0);
        appointment.setIsDeleted(0);
        appointment.setRemark(createDTO.getRemark());

        appointmentMapper.insert(appointment);

        log.info("创建送货预约: appointmentNo={}, orderNo={}", appointment.getAppointmentNo(), order.getOrderNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmAppointment(Long id) {
        DeliveryAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (appointment.getStatus() != 0) {
            throw new BusinessException(4001, "预约状态不允许确认");
        }

        appointment.setStatus(1);
        appointmentMapper.updateById(appointment);

        log.info("确认送货预约: appointmentNo={}", appointment.getAppointmentNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkInAppointment(Long id) {
        DeliveryAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (appointment.getStatus() != 1) {
            throw new BusinessException(4002, "预约状态不允许签到");
        }

        appointment.setStatus(2);
        appointment.setCheckInTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

        log.info("签到送货预约: appointmentNo={}", appointment.getAppointmentNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeAppointment(Long id, String warehouseOperator) {
        DeliveryAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (appointment.getStatus() != 2) {
            throw new BusinessException(4003, "预约状态不允许完成");
        }

        appointment.setStatus(3);
        appointment.setCheckOutTime(LocalDateTime.now());
        appointment.setWarehouseOperator(warehouseOperator);
        appointmentMapper.updateById(appointment);

        log.info("完成送货预约: appointmentNo={}", appointment.getAppointmentNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelAppointment(Long id, String reason) {
        DeliveryAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (appointment.getStatus() >= 2) {
            throw new BusinessException(4004, "预约已签到或完成，无法取消");
        }

        appointment.setStatus(4);
        if (appointment.getRemark() != null) {
            appointment.setRemark(appointment.getRemark() + "; 取消原因: " + reason);
        } else {
            appointment.setRemark("取消原因: " + reason);
        }
        appointmentMapper.updateById(appointment);

        log.info("取消送货预约: appointmentNo={}, reason={}", appointment.getAppointmentNo(), reason);
    }

    private String generateAppointmentNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return APPOINTMENT_NO_PREFIX + dateStr + System.currentTimeMillis();
    }

    private DeliveryAppointmentVO convertToVO(DeliveryAppointment appointment) {
        return DeliveryAppointmentVO.builder()
                .id(appointment.getId())
                .appointmentNo(appointment.getAppointmentNo())
                .orderId(appointment.getOrderId())
                .orderNo(appointment.getOrderNo())
                .supplierId(appointment.getSupplierId())
                .supplierName(appointment.getSupplierName())
                .shipmentId(appointment.getShipmentId())
                .shipmentNo(appointment.getShipmentNo())
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTimeFrom(appointment.getAppointmentTimeFrom())
                .appointmentTimeTo(appointment.getAppointmentTimeTo())
                .deliveryAddress(appointment.getDeliveryAddress())
                .warehouseId(appointment.getWarehouseId())
                .warehouseName(appointment.getWarehouseName())
                .dockNo(appointment.getDockNo())
                .contactPerson(appointment.getContactPerson())
                .contactPhone(appointment.getContactPhone())
                .itemCount(appointment.getItemCount())
                .totalQuantity(appointment.getTotalQuantity())
                .totalWeight(appointment.getTotalWeight())
                .totalVolume(appointment.getTotalVolume())
                .vehicleNo(appointment.getVehicleNo())
                .driverName(appointment.getDriverName())
                .driverPhone(appointment.getDriverPhone())
                .status(appointment.getStatus())
                .statusName(STATUS_MAP.getOrDefault(appointment.getStatus(), "未知"))
                .checkInTime(appointment.getCheckInTime())
                .checkOutTime(appointment.getCheckOutTime())
                .warehouseOperator(appointment.getWarehouseOperator())
                .remark(appointment.getRemark())
                .createTime(appointment.getCreateTime())
                .updateTime(appointment.getUpdateTime())
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
            log.warn("没有发货中的订单数据，无法生成送货预约测试数据");
            return;
        }

        String[] warehouseNames = {"A仓库", "B仓库", "C仓库", "D仓库"};
        String[] contactPersons = {"仓库管理员-张三", "仓库管理员-李四", "仓库管理员-王五", "仓库管理员-赵六"};
        String[] contactPhones = {"010-88888001", "010-88888002", "010-88888003", "010-88888004"};

        for (int i = 0; i < orders.size(); i++) {
            PurchaseOrder order = orders.get(i);
            try {
                DeliveryAppointmentCreateDTO createDTO = new DeliveryAppointmentCreateDTO();
                createDTO.setOrderId(order.getId());
                createDTO.setAppointmentDate(LocalDate.now().plusDays(i % 3));
                createDTO.setAppointmentTimeFrom(LocalTime.of(9 + (i % 4) * 2, 0));
                createDTO.setAppointmentTimeTo(LocalTime.of(11 + (i % 4) * 2, 0));
                createDTO.setWarehouseId((i % 4) + 1);
                createDTO.setWarehouseName(warehouseNames[i % warehouseNames.length]);
                createDTO.setDockNo((i % 10) + 1);
                createDTO.setContactPerson(contactPersons[i % contactPersons.length]);
                createDTO.setContactPhone(contactPhones[i % contactPhones.length]);
                createDTO.setVehicleNo("京A" + (10000 + i));
                createDTO.setDriverName("司机" + (i + 1));
                createDTO.setDriverPhone("13800" + String.format("%06d", 100000 + i));
                createDTO.setRemark("测试数据" + (i + 1));

                createAppointment(createDTO);

                if (i % 3 == 0) {
                    List<DeliveryAppointment> appointments = appointmentMapper.selectList(
                            new LambdaQueryWrapper<DeliveryAppointment>()
                                    .eq(DeliveryAppointment::getOrderId, order.getId())
                                    .eq(DeliveryAppointment::getIsDeleted, 0)
                                    .orderByDesc(DeliveryAppointment::getCreateTime)
                                    .last("LIMIT 1")
                    );
                    if (!appointments.isEmpty()) {
                        confirmAppointment(appointments.get(0).getId());
                    }
                }

            } catch (Exception e) {
                log.warn("生成送货预约测试数据失败: orderNo={}, {}", order.getOrderNo(), e.getMessage());
            }
        }

        log.info("送货预约测试数据生成完成");
    }
}
