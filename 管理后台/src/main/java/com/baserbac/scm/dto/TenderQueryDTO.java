package com.baserbac.scm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(description = "招标单查询参数")
public class TenderQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "招标单编号")
    private String tenderNo;

    @Schema(description = "招标单名称")
    private String tenderName;

    @Schema(description = "招标类型：1公开招标 2邀请招标")
    private Integer tenderType;

    @Schema(description = "状态：0待发布 1招标中 2投标中 3已开标 4评标中 5已定标 6已取消")
    private Integer status;
}
