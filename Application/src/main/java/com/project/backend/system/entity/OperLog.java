package com.project.backend.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode
@TableName("sys_oper_log")
@Schema(description = "操作日志实体")
public class OperLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日志主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "操作模块")
    @TableField("title")
    private String title;

    @Schema(description = "业务类型：其它 1新增 2修改 3删除")
    @TableField("business_type")
    private Integer businessType;

    @Schema(description = "方法名称")
    @TableField("method")
    private String method;

    @Schema(description = "请求方式")
    @TableField("request_method")
    private String requestMethod;

    @Schema(description = "操作类别：其它 1后台用户 2手机端用户）")
    @TableField("operator_type")
    private Integer operatorType;

    @Schema(description = "操作人员")
    @TableField("oper_name")
    private String operName;

    @Schema(description = "设备类型（字典sys_device_type：桌面设备 2移动设备 3爬虫/Bot）")
    @TableField("device_type")
    private Integer deviceType;

    @Schema(description = "请求URL")
    @TableField("oper_url")
    private String operUrl;

    @Schema(description = "主机地址")
    @TableField("oper_ip")
    private String operIp;

    @Schema(description = "操作地点")
    @TableField("oper_location")
    private String operLocation;

    @Schema(description = "请求参数")
    @TableField("oper_param")
    private String operParam;

    @Schema(description = "返回参数")
    @TableField("json_result")
    private String jsonResult;

    @Schema(description = "操作状态（0正常 1异常）")
    @TableField("status")
    private Integer status;

    @Schema(description = "错误消息")
    @TableField("error_msg")
    private String errorMsg;

    @Schema(description = "操作时间")
    @TableField("oper_time")
    private LocalDateTime operTime;

    @Schema(description = "消耗时间（毫秒）")
    @TableField("cost_time")
    private Long costTime;
}
