package com.project.backend.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志展示VO
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Data
@Schema(description = "操作日志响应")
public class OperLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日志主键")
    private Long id;

    @Schema(description = "操作模块")
    private String title;

    @Schema(description = "业务类型：其它 1新增 2修改 3删除")
    private Integer businessType;

    @Schema(description = "业务类型文本")
    private String businessTypeText;

    @Schema(description = "方法名称")
    private String method;

    @Schema(description = "请求方式")
    private String requestMethod;

    @Schema(description = "操作类别：其它 1后台用户 2手机端用户）")
    private Integer operatorType;

    @Schema(description = "操作人员")
    private String operName;

    @Schema(description = "设备类型（字典sys_device_type：桌面设备 2移动设备 3爬虫/Bot）")
    private Integer deviceType;

    @Schema(description = "设备类型文本")
    private String deviceTypeText;

    @Schema(description = "请求URL")
    private String operUrl;

    @Schema(description = "主机地址")
    private String operIp;

    @Schema(description = "操作地点")
    private String operLocation;

    @Schema(description = "请求参数")
    private String operParam;

    @Schema(description = "返回参数")
    private String jsonResult;

    @Schema(description = "操作状态（0正常 1异常）")
    private Integer status;

    @Schema(description = "操作状态文本")
    private String statusText;

    @Schema(description = "错误消息")
    private String errorMsg;

    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;

    @Schema(description = "消耗时间（毫秒）")
    private Long costTime;
}
