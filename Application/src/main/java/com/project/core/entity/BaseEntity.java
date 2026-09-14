package com.project.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体
 * 包含所有实体类的公共字段
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID")
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @Schema(description = "更新人ID")
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 删除标记：0-未删除 非0-已删除（已删除时值为记录主键ID）
     * 使用主键ID作为逻辑删除值，避免唯一索引包含 deleted 列时，
     * 同名记录「删除→重建→再删除」触发唯一键冲突
     * 所有继承 BaseEntity 的实体类自动获得逻辑删除功能
     */
    @Schema(description = "删除标记：0-未删除 非0-已删除")
    @TableField("deleted")
    @com.baomidou.mybatisplus.annotation.TableLogic(value = "0", delval = "id")
    private Long deleted;
}
