package com.project.backend.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
@Schema(description = "字典数据实体")
public class DictData extends BaseEntity {

    @Schema(description = "字典编码")
    @TableField("dict_code")
    private String dictCode;

    @Schema(description = "字典标签")
    @TableField("label")
    private String label;

    @Schema(description = "字典说明")
    @TableField("value")
    private String value;

    @Schema(description = "CSS类名")
    @TableField("css_class")
    private String cssClass;

    @Schema(description = "表格回显样式")
    @TableField("list_class")
    private String listClass;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "是否默认：1是 0否")
    @TableField("is_default")
    private Integer isDefault;

    @Schema(description = "状态：1正常 0停用")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
