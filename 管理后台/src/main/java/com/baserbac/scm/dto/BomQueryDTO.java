package com.baserbac.scm.dto;

import com.baserbac.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "BOM查询参数")
public class BomQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "BOM编号")
    private String bomNo;

    @Schema(description = "BOM名称")
    private String bomName;

    @Schema(description = "父项编码")
    private String parentCode;

    @Schema(description = "父项名称")
    private String parentName;

    @Schema(description = "物料编码")
    private String materialCode;

    @Schema(description = "物料名称")
    private String materialName;
}
