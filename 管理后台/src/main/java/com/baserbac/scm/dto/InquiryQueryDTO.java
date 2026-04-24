package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(description = "询价单查询参数")
public class InquiryQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "询价单编号")
    private String inquiryNo;

    @Schema(description = "询价单名称")
    private String inquiryName;

    @Schema(description = "状态：0待发布 1已发布 2报价中 3报价结束 4已比价 5已取消")
    private Integer status;
}
