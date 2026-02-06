package com.project.backend.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core.annotation.Log;
import com.project.core.context.UserContext;
import com.project.core.util.RequestUtils;
import com.project.backend.system.entity.OperLog;
import com.project.backend.system.service.OperLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired
    private OperLogService operLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 最大参数响应长度
     */
    private static final int MAX_CONTENT_LENGTH = 2000;

    /**
     * 设备类型常量
     */
    private static final int DEVICE_TYPE_DESKTOP = 1;
    private static final int DEVICE_TYPE_MOBILE = 2;
    private static final int DEVICE_TYPE_BOT = 3;

    /**
     * 操作状态常量
     */
    private static final int STATUS_SUCCESS = 0;
    private static final int STATUS_ERROR = 1;

    /**
     * 配置织入切面
     */
    @Pointcut("@annotation(com.project.core.annotation.Log)")
    public void logPointCut() {
    }

    /**
     * 环绕通知
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        OperLog operLog = new OperLog();
        Object result = null;
        Exception exception = null;

        try {
            // 执行方法
            result = point.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 记录日志
            try {
                recordLog(point, result, exception, operLog, beginTime);
            } catch (Exception e) {
                log.error("记录操作日志异常", e);
            }
        }
    }

    /**
     * 记录日志
     */
    private void recordLog(ProceedingJoinPoint point, Object result, Exception exception,
                          OperLog operLog, long beginTime) {
        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);

            if (logAnnotation != null) {
                // 构建详细标题
                String title = buildDetailedTitle(point, logAnnotation.title(), logAnnotation.businessType());
                operLog.setTitle(title);

                // 设置业务类型
                operLog.setBusinessType(logAnnotation.businessType());
            }

            // 设置方法名称
            String className = point.getTarget().getClass().getName();
            String methodName = point.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // 设置请求方式
                operLog.setRequestMethod(request.getMethod());

                // 设置请求URL
                operLog.setOperUrl(request.getRequestURI());

                // 设置IP地址
                String ip = RequestUtils.getClientIp(request);
                operLog.setOperIp(ip);

                // 设置设备类型
                Integer deviceType = getDeviceType(request);
                operLog.setDeviceType(deviceType);

                // 设置操作地点（可以调用IP地址库获取地理位置，这里简化处理）
                operLog.setOperLocation("");

                // 设置请求参数
                if (logAnnotation != null && logAnnotation.saveRequestData()) {
                    String params = getRequestParams(point);
                    operLog.setOperParam(params);
                }

                // 设置响应参数
                if (logAnnotation != null && logAnnotation.saveResponseData() && result != null) {
                    try {
                        String jsonResult = objectMapper.writeValueAsString(result);
                        operLog.setJsonResult(truncateString(jsonResult));
                    } catch (Exception e) {
                        log.warn("序列化响应参数失败", e);
                    }
                }
            }

            // 设置操作人信息
            String username = UserContext.getUsername();
            if (username != null) {
                operLog.setOperName(username);
            }
            operLog.setOperatorType(1); // 后台用户

            // 设置操作状态
            if (exception != null) {
                operLog.setStatus(STATUS_ERROR);
                operLog.setErrorMsg(truncateString(exception.getMessage()));
            } else {
                operLog.setStatus(STATUS_SUCCESS);
            }

            // 设置操作时间
            operLog.setOperTime(LocalDateTime.now());

            // 设置消耗时间
            long costTime = System.currentTimeMillis() - beginTime;
            operLog.setCostTime(costTime);

            // 异步保存日志
            operLogService.saveOperLog(operLog);
        } catch (Exception e) {
            log.error("构建操作日志失败", e);
        }
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(ProceedingJoinPoint point) {
        try {
            Object[] args = point.getArgs();
            if (args == null || args.length == 0) {
                return "";
            }

            StringBuilder params = new StringBuilder();
            for (Object arg : args) {
                if (isServletObject(arg)) {
                    continue;
                }

                try {
                    String jsonStr = objectMapper.writeValueAsString(arg);
                    params.append(jsonStr).append(" ");
                } catch (Exception e) {
                    params.append(arg != null ? arg.toString() : "null").append(" ");
                }
            }

            return truncateString(params.toString().trim());
        } catch (Exception e) {
            log.warn("获取请求参数失败", e);
            return "";
        }
    }

    /**
     * 判断是否为Servlet对象
     */
    private boolean isServletObject(Object obj) {
        return obj instanceof jakarta.servlet.http.HttpServletRequest ||
               obj instanceof jakarta.servlet.http.HttpServletResponse;
    }

    /**
     * 截断字符串到最大长度
     */
    private String truncateString(String str) {
        if (str == null) {
            return null;
        }
        return str.length() > MAX_CONTENT_LENGTH
                ? str.substring(0, MAX_CONTENT_LENGTH) + "..."
                : str;
    }

    /**
     * 识别设备类型
     *
     * @param request HTTP请求
     * @return 设备类型：1桌面设备 2移动设备 3爬虫/Bot
     */
    private Integer getDeviceType(HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            if (userAgent == null || userAgent.isEmpty()) {
                return DEVICE_TYPE_DESKTOP;
            }

            String ua = userAgent.toLowerCase();

            // 检测爬虫/Bot
            if (isBotUserAgent(ua)) {
                return DEVICE_TYPE_BOT;
            }

            // 检测移动设备
            if (isMobileUserAgent(ua)) {
                return DEVICE_TYPE_MOBILE;
            }

            return DEVICE_TYPE_DESKTOP;
        } catch (Exception e) {
            log.warn("识别设备类型失败", e);
            return DEVICE_TYPE_DESKTOP;
        }
    }

    /**
     * 判断是否为爬虫/Bot
     */
    private boolean isBotUserAgent(String ua) {
        String[] botKeywords = {"bot", "crawler", "spider", "scraper", "curl", "wget",
                                "python-requests", "java/", "apache-httpclient"};
        for (String keyword : botKeywords) {
            if (ua.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为移动设备
     */
    private boolean isMobileUserAgent(String ua) {
        String[] mobileKeywords = {"mobile", "android", "iphone", "ipad", "ipod",
                                   "blackberry", "windows phone", "windows mobile"};
        for (String keyword : mobileKeywords) {
            if (ua.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建详细标题
     * 
     * @param point 切点
     * @param originalTitle 原始标题
     * @param businessType 业务类型
     * @return 详细标题
     */
    private String buildDetailedTitle(ProceedingJoinPoint point, String originalTitle, int businessType) {
        try {
            // 如果title已包含"-"或长度大于10，认为已经是详细标题，不自动修改
            if (originalTitle != null && (originalTitle.contains("-") || originalTitle.length() > 10)) {
                return originalTitle;
            }

            // 简单操作词列表
            String[] simpleOperations = {"新增", "编辑", "删除", "查询", "add", "update", "edit", "delete", "remove", "get", "list"};
            boolean isSimpleOperation = false;
            if (originalTitle != null) {
                for (String op : simpleOperations) {
                    if (originalTitle.equals(op)) {
                        isSimpleOperation = true;
                        break;
                    }
                }
            }

            // 如果不是简单操作词且title不为空，返回原始标题
            if (!isSimpleOperation && originalTitle != null && !originalTitle.isEmpty()) {
                return originalTitle;
            }

            // 提取模块名称
            String moduleName = extractEntityName(point);
            if (moduleName == null || moduleName.isEmpty()) {
                return originalTitle != null ? originalTitle : "";
            }

            // 根据业务类型构建标题
            String operationName = getOperationName(businessType);
            String entityName = moduleName.replace("管理", ""); // 去除"管理"后缀，如"用户管理" -> "用户"

            switch (businessType) {
                case 1: // 新增
                    String addKeyInfo = extractEntityKeyInfo(point, false);
                    if (addKeyInfo != null && !addKeyInfo.isEmpty()) {
                        return String.format("%s-%s%s(%s)", moduleName, operationName, entityName, addKeyInfo);
                    }
                    return String.format("%s-%s%s", moduleName, operationName, entityName);

                case 2: // 编辑
                    String editKeyInfo = extractEntityKeyInfo(point, true);
                    Long editId = extractPathVariableId(point);
                    if (editKeyInfo != null && !editKeyInfo.isEmpty()) {
                        return String.format("%s-%s%s(%s)", moduleName, operationName, entityName, editKeyInfo);
                    } else if (editId != null) {
                        return String.format("%s-%s%s(ID:%d)", moduleName, operationName, entityName, editId);
                    }
                    return String.format("%s-%s%s", moduleName, operationName, entityName);

                case 3: // 删除
                    Long deleteId = extractPathVariableId(point);
                    if (deleteId != null) {
                        return String.format("%s-%s%s(ID:%d)", moduleName, operationName, entityName, deleteId);
                    }
                    return String.format("%s-%s%s", moduleName, operationName, entityName);

                case 0: // 查询/其它
                default:
                    return String.format("%s-%s%s", moduleName, operationName, entityName);
            }
        } catch (Exception e) {
            log.warn("构建详细标题失败，使用原始标题", e);
            return originalTitle != null ? originalTitle : "";
        }
    }

    /**
     * 获取操作名称
     */
    private String getOperationName(int businessType) {
        switch (businessType) {
            case 1:
                return "新增";
            case 2:
                return "编辑";
            case 3:
                return "删除";
            case 0:
            default:
                return "查询";
        }
    }

    /**
     * 提取实体名称（模块名称）
     * 
     * @param point 切点
     * @return 模块名称，如"用户管理"
     */
    private String extractEntityName(ProceedingJoinPoint point) {
        try {
            Object target = point.getTarget();
            Class<?> targetClass = target.getClass();

            // 检查是否实现了BaseCrudController
            try {
                Method getEntityNameMethod = targetClass.getMethod("getEntityName");
                Object result = getEntityNameMethod.invoke(target);
                if (result != null) {
                    String entityName = result.toString();
                    // 如果返回的是实体名称（如"用户"），添加"管理"后缀
                    if (!entityName.endsWith("管理")) {
                        return entityName + "管理";
                    }
                    return entityName;
                }
            } catch (NoSuchMethodException e) {
                // 没有getEntityName方法，继续使用类名提取
            }

            // 从类名提取
            String className = targetClass.getSimpleName();
            // 去除"Controller"后缀
            if (className.endsWith("Controller")) {
                className = className.substring(0, className.length() - "Controller".length());
            }
            // 去除"Sys"前缀
            if (className.startsWith("Sys")) {
                className = className.substring("Sys".length());
            }

            // 简单的类名到模块名称的映射（可以根据实际情况扩展）
            return className + "管理";
        } catch (Exception e) {
            log.warn("提取实体名称失败", e);
            return null;
        }
    }

    /**
     * 提取关键信息（从DTO中提取名称字段）
     * 
     * @param point 切点
     * @param includeId 是否包含ID（编辑时从路径参数获取ID）
     * @return 关键信息，如"张三"东校区"ID:1"
     */
    private String extractEntityKeyInfo(ProceedingJoinPoint point, boolean includeId) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Parameter[] parameters = method.getParameters();
            Object[] args = point.getArgs();

            Object dto = null;
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                Object arg = args[i];

                // 跳过HttpServletRequest和HttpServletResponse
                if (arg instanceof jakarta.servlet.http.HttpServletRequest ||
                    arg instanceof jakarta.servlet.http.HttpServletResponse) {
                    continue;
                }

                // 查找@RequestBody注解的参数（DTO）
                if (param.isAnnotationPresent(RequestBody.class) && arg != null) {
                    // 排除基本类型和常见类
                    Class<?> paramType = param.getType();
                    if (!paramType.isPrimitive() &&
                        !paramType.equals(String.class) &&
                        !paramType.equals(Long.class) &&
                        !paramType.equals(Integer.class)) {
                        dto = arg;
                        break;
                    }
                }
            }

            if (dto == null) {
                // 如果没有找到DTO，且需要ID，尝试从路径参数获取
                if (includeId) {
                    Long id = extractPathVariableId(point);
                    if (id != null) {
                        return "ID:" + id;
                    }
                }
                return null;
            }

            // 常见的名称字段优先级列表
            String[] nameFields = {"name", "username", "nickname", "campusName", "roleName",
                                   "majorName", "className", "deptName", "title", "code"};

            // 尝试按优先级获取字段
            for (String fieldName : nameFields) {
                try {
                    // 构造getter方法
                    String getterName = "get" + capitalize(fieldName);
                    Method getter = dto.getClass().getMethod(getterName);
                    Object value = getter.invoke(dto);
                    if (value != null && !value.toString().trim().isEmpty()) {
                        return value.toString().trim();
                    }
                } catch (NoSuchMethodException e) {
                    // 继续尝试下一个字段
                }
            }

            // 如果所有名称字段都没有值，尝试获取ID
            try {
                Method getIdMethod = dto.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(dto);
                if (id != null) {
                    return "ID:" + id;
                }
            } catch (NoSuchMethodException e) {
                // 没有getId方法
            }

            // 如果includeId为true，尝试从路径参数获取ID
            if (includeId) {
                Long id = extractPathVariableId(point);
                if (id != null) {
                    return "ID:" + id;
                }
            }

            return null;
        } catch (Exception e) {
            log.warn("提取关键信息失败", e);
            return null;
        }
    }

    /**
     * 提取路径变量ID
     * 
     * @param point 切点
     * @return ID
     */
    private Long extractPathVariableId(ProceedingJoinPoint point) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Parameter[] parameters = method.getParameters();
            Object[] args = point.getArgs();

            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                Object arg = args[i];

                // 查找@PathVariable注解的Long类型参数，参数名"id"
                if (param.isAnnotationPresent(PathVariable.class)) {
                    PathVariable pathVar = param.getAnnotation(PathVariable.class);
                    String paramName = pathVar.value();
                    if (paramName.isEmpty()) {
                        paramName = param.getName();
                    }

                    if (("id".equals(paramName) || paramName.isEmpty()) && arg instanceof Long) {
                        return (Long) arg;
                    }
                }
            }

            return null;
        } catch (Exception e) {
            log.warn("提取路径变量ID失败", e);
            return null;
        }
    }

    /**
     * 首字母大写
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
