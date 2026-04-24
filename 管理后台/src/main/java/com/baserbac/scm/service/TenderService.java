package com.baserbac.scm.service;

import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.result.PageResult;
import com.baserbac.scm.dto.TenderAwardDTO;
import com.baserbac.scm.dto.TenderBidDTO;
import com.baserbac.scm.dto.TenderCreateDTO;
import com.baserbac.scm.dto.TenderQueryDTO;
import com.baserbac.scm.entity.*;
import com.baserbac.scm.mapper.*;
import com.baserbac.scm.vo.TenderBidVO;
import com.baserbac.scm.vo.TenderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public class TenderService {

    private final TenderMapper tenderMapper;
    private final TenderBidMapper tenderBidMapper;
    private final SupplierMapper supplierMapper;
    private final PurchaseRequirementMapper requirementMapper;

    private static final String TENDER_NO_PREFIX = "TND";

    public PageResult<TenderVO> pageTenders(TenderQueryDTO queryDTO) {
        Page<Tender> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<Tender> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getTenderNo() != null, Tender::getTenderNo, queryDTO.getTenderNo())
               .like(queryDTO.getTenderName() != null, Tender::getTenderName, queryDTO.getTenderName())
               .eq(queryDTO.getTenderType() != null, Tender::getTenderType, queryDTO.getTenderType())
               .eq(queryDTO.getStatus() != null, Tender::getStatus, queryDTO.getStatus())
               .orderByDesc(Tender::getCreateTime);
        
        Page<Tender> result = tenderMapper.selectPage(page, wrapper);
        
        List<TenderVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(
            result.getTotal(), 
            voList, 
            (long) result.getCurrent(), 
            (long) result.getSize()
        );
    }

    public TenderVO getTenderById(Long id) {
        Tender tender = tenderMapper.selectById(id);
        if (tender == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVOWithDetails(tender);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createTender(TenderCreateDTO createDTO) {
        Tender tender = new Tender();
        tender.setTenderNo(generateTenderNo());
        tender.setTenderName(createDTO.getTenderName());
        tender.setTenderType(createDTO.getTenderType() != null ? createDTO.getTenderType() : 1);
        tender.setDescription(createDTO.getDescription());
        tender.setEstimatedBudget(createDTO.getEstimatedBudget());
        tender.setPublishDate(createDTO.getPublishDate() != null ? createDTO.getPublishDate() : LocalDate.now());
        tender.setBidDeadline(createDTO.getBidDeadline());
        tender.setOpenDate(createDTO.getOpenDate());
        tender.setContactPerson(createDTO.getContactPerson());
        tender.setContactPhone(createDTO.getContactPhone());
        tender.setContactEmail(createDTO.getContactEmail());
        tender.setTenderDocs(createDTO.getTenderDocs());
        tender.setTenderAddress(createDTO.getTenderAddress());
        tender.setStatus(0);
        tender.setRemark(createDTO.getRemark());
        tender.setIsDeleted(0);
        
        if (createDTO.getReqIds() != null && !createDTO.getReqIds().isEmpty()) {
            tender.setReqIds(String.join(",", createDTO.getReqIds().stream().map(String::valueOf).collect(Collectors.toList())));
        }
        
        tenderMapper.insert(tender);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTender(Long id, TenderCreateDTO updateDTO) {
        Tender tender = tenderMapper.selectById(id);
        if (tender == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        if (updateDTO.getTenderName() != null) {
            tender.setTenderName(updateDTO.getTenderName());
        }
        if (updateDTO.getTenderType() != null) {
            tender.setTenderType(updateDTO.getTenderType());
        }
        if (updateDTO.getDescription() != null) {
            tender.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getEstimatedBudget() != null) {
            tender.setEstimatedBudget(updateDTO.getEstimatedBudget());
        }
        if (updateDTO.getPublishDate() != null) {
            tender.setPublishDate(updateDTO.getPublishDate());
        }
        if (updateDTO.getBidDeadline() != null) {
            tender.setBidDeadline(updateDTO.getBidDeadline());
        }
        if (updateDTO.getOpenDate() != null) {
            tender.setOpenDate(updateDTO.getOpenDate());
        }
        if (updateDTO.getContactPerson() != null) {
            tender.setContactPerson(updateDTO.getContactPerson());
        }
        if (updateDTO.getContactPhone() != null) {
            tender.setContactPhone(updateDTO.getContactPhone());
        }
        if (updateDTO.getContactEmail() != null) {
            tender.setContactEmail(updateDTO.getContactEmail());
        }
        if (updateDTO.getTenderDocs() != null) {
            tender.setTenderDocs(updateDTO.getTenderDocs());
        }
        if (updateDTO.getTenderAddress() != null) {
            tender.setTenderAddress(updateDTO.getTenderAddress());
        }
        if (updateDTO.getRemark() != null) {
            tender.setRemark(updateDTO.getRemark());
        }
        
        tenderMapper.updateById(tender);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publishTender(Long id) {
        Tender tender = tenderMapper.selectById(id);
        if (tender == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        tender.setStatus(1);
        tenderMapper.updateById(tender);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitBid(TenderBidDTO bidDTO) {
        Tender tender = tenderMapper.selectById(bidDTO.getTenderId());
        if (tender == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        Supplier supplier = supplierMapper.selectById(bidDTO.getSupplierId());
        if (supplier == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        TenderBid bid = new TenderBid();
        bid.setTenderId(bidDTO.getTenderId());
        bid.setSupplierId(bidDTO.getSupplierId());
        bid.setSupplierName(supplier.getSupplierName());
        bid.setBidPrice(bidDTO.getBidPrice());
        bid.setBidTime(LocalDateTime.now());
        bid.setBidDocs(bidDTO.getBidDocs());
        bid.setBidDescription(bidDTO.getBidDescription());
        bid.setTechnicalParams(bidDTO.getTechnicalParams());
        bid.setDeliveryPlan(bidDTO.getDeliveryPlan());
        bid.setAfterSalesService(bidDTO.getAfterSalesService());
        bid.setIsWin(0);
        bid.setIsDeleted(0);
        bid.setRemark(bidDTO.getRemark());
        
        tenderBidMapper.insert(bid);
    }

    @Transactional(rollbackFor = Exception.class)
    public void openTender(Long id) {
        Tender tender = tenderMapper.selectById(id);
        if (tender == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        tender.setStatus(3);
        tenderMapper.updateById(tender);
    }

    @Transactional(rollbackFor = Exception.class)
    public void awardTender(TenderAwardDTO awardDTO) {
        Tender tender = tenderMapper.selectById(awardDTO.getTenderId());
        if (tender == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        Supplier supplier = supplierMapper.selectById(awardDTO.getWinSupplierId());
        if (supplier == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        tender.setWinSupplierId(awardDTO.getWinSupplierId());
        tender.setWinSupplierName(supplier.getSupplierName());
        tender.setWinPrice(awardDTO.getWinPrice());
        tender.setWinReason(awardDTO.getWinReason());
        tender.setWinDate(awardDTO.getWinDate() != null ? awardDTO.getWinDate() : LocalDate.now());
        tender.setStatus(5);
        
        tenderMapper.updateById(tender);
        
        TenderBid winBid = tenderBidMapper.selectOne(
            new LambdaQueryWrapper<TenderBid>()
                .eq(TenderBid::getTenderId, awardDTO.getTenderId())
                .eq(TenderBid::getSupplierId, awardDTO.getWinSupplierId())
        );
        
        if (winBid != null) {
            winBid.setIsWin(1);
            winBid.setWinReason(awardDTO.getWinReason());
            tenderBidMapper.updateById(winBid);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelTender(Long id) {
        Tender tender = tenderMapper.selectById(id);
        if (tender == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        tender.setStatus(6);
        tenderMapper.updateById(tender);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTender(Long id) {
        Tender tender = tenderMapper.selectById(id);
        if (tender == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        tenderBidMapper.delete(
            new LambdaQueryWrapper<TenderBid>()
                .eq(TenderBid::getTenderId, id)
        );
        
        tenderMapper.deleteById(id);
    }

    private String generateTenderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return TENDER_NO_PREFIX + dateStr + System.currentTimeMillis();
    }

    private TenderVO convertToVO(Tender t) {
        return TenderVO.builder()
            .id(t.getId())
            .tenderNo(t.getTenderNo())
            .tenderName(t.getTenderName())
            .tenderType(t.getTenderType())
            .description(t.getDescription())
            .reqIds(t.getReqIds())
            .estimatedBudget(t.getEstimatedBudget())
            .publishDate(t.getPublishDate())
            .bidDeadline(t.getBidDeadline())
            .openDate(t.getOpenDate())
            .contactPerson(t.getContactPerson())
            .contactPhone(t.getContactPhone())
            .contactEmail(t.getContactEmail())
            .tenderDocs(t.getTenderDocs())
            .tenderAddress(t.getTenderAddress())
            .status(t.getStatus())
            .winSupplierId(t.getWinSupplierId())
            .winSupplierName(t.getWinSupplierName())
            .winPrice(t.getWinPrice())
            .winReason(t.getWinReason())
            .winDate(t.getWinDate())
            .remark(t.getRemark())
            .createTime(t.getCreateTime())
            .updateTime(t.getUpdateTime())
            .build();
    }

    private TenderVO convertToVOWithDetails(Tender tender) {
        TenderVO vo = convertToVO(tender);
        
        List<TenderBid> bids = tenderBidMapper.selectList(
            new LambdaQueryWrapper<TenderBid>()
                .eq(TenderBid::getTenderId, tender.getId())
                .orderByDesc(TenderBid::getBidTime)
        );
        
        List<TenderBidVO> bidVOList = bids.stream()
            .map(this::convertBidToVO)
            .collect(Collectors.toList());
        vo.setBids(bidVOList);
        
        return vo;
    }

    private TenderBidVO convertBidToVO(TenderBid b) {
        return TenderBidVO.builder()
            .id(b.getId())
            .tenderId(b.getTenderId())
            .supplierId(b.getSupplierId())
            .supplierName(b.getSupplierName())
            .bidPrice(b.getBidPrice())
            .bidTime(b.getBidTime())
            .bidDocs(b.getBidDocs())
            .bidDescription(b.getBidDescription())
            .technicalParams(b.getTechnicalParams())
            .deliveryPlan(b.getDeliveryPlan())
            .afterSalesService(b.getAfterSalesService())
            .isWin(b.getIsWin())
            .winReason(b.getWinReason())
            .score(b.getScore())
            .evaluateRemark(b.getEvaluateRemark())
            .remark(b.getRemark())
            .createTime(b.getCreateTime())
            .updateTime(b.getUpdateTime())
            .build();
    }
}
