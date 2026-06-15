---
name: backend-java
description: Enforces Java backend coding standards for the Application module. Use when working with Java files in Application/, creating controllers, services, entities, DTOs, VOs, mappers, or any backend code. Covers package structure (core/backend/app), dependency rules, Controller/Service patterns, exception handling, naming conventions, and JavaDoc requirements.
---

# Backend Java Coding Standards

## Package Structure

**Three-layer architecture: core / backend / app**

```
com.sushe/
├── core/          # Core shared module (platform-agnostic)
│   ├── annotation/
│   ├── config/
│   ├── constant/
│   ├── context/
│   ├── entity/
│   ├── enums/
│   ├── exception/
│   ├── result/
│   └── util/
├── backend/       # Backend management business logic
│   ├── accommodation/
│   ├── approval/
│   ├── organization/
│   ├── system/
│   └── common/
└── app/           # Mobile app specific logic
    ├── controller/
    └── service/
```

## Dependency Direction Rules

**Core principle: Dependencies flow downward only**

```
✅ backend → core     (correct: backend depends on core)
✅ app → core          (correct: app depends on core)
✅ app → backend       (allowed: app can reuse backend services)
❌ core → backend      (forbidden: core cannot depend on business code)
❌ core → app          (forbidden: core cannot depend on app code)
```

## Layer Responsibilities

| Package | Responsibility | Examples |
|---------|---------------|----------|
| **core** | Infrastructure, business-agnostic code | `R`, `BaseEntity`, `UserContext`, configs, enums |
| **backend** | Backend management business logic | Controller, Service, Mapper, Entity, DTO, VO |
| **app** | Mobile app specific logic | Mobile Controller and Service |

## Java File Header

**All Java files must include JavaDoc with:**

```java
/**
 * [Brief class description]
 * 
 * @author 陈鸿昇
 * @since YYYY-MM-DD
 */
```

## Import Order

```java
// 1. Java standard library
import java.util.List;
import java.time.LocalDateTime;

// 2. Third-party libraries
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 3. Core module (core)
import com.sushe.core.result.R;
import com.sushe.core.exception.BusinessException;

// 4. Business module (backend or app)
import com.sushe.backend.accommodation.entity.Student;
import com.sushe.app.service.StudentService;
```

## RESTful API Design Standards

### API URL Design

**Must follow RESTful conventions:**

```
GET    /api/v1/system/user          # 获取用户列表
GET    /api/v1/system/user/page     # 分页查询用户
GET    /api/v1/system/user/{id}     # 获取单个用户详情
POST   /api/v1/system/user          # 创建用户
PUT    /api/v1/system/user/{id}     # 更新用户（完整更新）
PATCH  /api/v1/system/user/{id}     # 部分更新用户
DELETE /api/v1/system/user/{id}     # 删除单个用户
DELETE /api/v1/system/user/batch    # 批量删除用户
PUT    /api/v1/system/user/{id}/status/{status}  # 更新用户状态
```

### HTTP Method Usage

| Method | Purpose | Request Body | Response | Idempotent |
|--------|---------|--------------|----------|------------|
| `GET` | 查询资源 | ❌ No | ✅ Data | ✅ Yes |
| `POST` | 创建资源 | ✅ Yes | ✅ Created data or ID | ❌ No |
| `PUT` | 完整更新资源 | ✅ Yes | ✅ Updated data | ✅ Yes |
| `PATCH` | 部分更新资源 | ✅ Yes | ✅ Updated data | ✅ Yes |
| `DELETE` | 删除资源 | ❌ No (or ID list) | ✅ Success flag | ✅ Yes |

### URL Naming Rules

**✅ Correct:**
- Use nouns for resources: `/user`, `/check-in`, `/approval`
- Use plural for collections: `/users`, `/check-ins`
- Use kebab-case: `/check-in`, `/approval-instance`
- Version prefix: `/api/v1/`
- Path parameters for ID: `/user/{id}`
- Query parameters for filters: `/user?status=1&name=张三`

**❌ Forbidden:**
- Verbs in URL: `/getUser`, `/createUser`, `/deleteUser`
- Actions in path: `/user/delete`, `/user/update`
- camelCase: `/checkIn`, `/userInfo`
- Underscore: `/check_in`, `/user_info`

### Status Action Pattern

For status updates, use PUT with status in path:

```java
@PutMapping("/{id}/status/{status}")
@Operation(summary = "更新状态")
@Log(title = "更新用户状态", businessType = 2)
public R<Void> updateStatus(
    @PathVariable Long id, 
    @PathVariable Integer status
) {
    boolean success = userService.updateStatus(id, status);
    return success ? R.ok() : R.fail();
}
```

### Batch Operation Pattern

For batch operations, use DELETE/PUT with `/batch` suffix:

```java
@DeleteMapping("/batch")
@Operation(summary = "批量删除")
@Log(title = "批量删除用户", businessType = 3)
public R<Void> batchDelete(@RequestBody Long[] ids) {
    boolean success = userService.batchDelete(ids);
    return success ? R.ok() : R.fail();
}
```

### Query Parameter Standards

**✅ Use query parameters for:**
- Filtering: `?status=1&type=2`
- Sorting: `?sortBy=createTime&order=desc`
- Pagination: `?pageNum=1&pageSize=10`
- Search: `?keyword=张三`
- Date range: `?startDate=2024-01-01&endDate=2024-12-31`

**❌ Don't use path parameters for filters:**
```java
// ❌ Wrong
@GetMapping("/status/{status}")

// ✅ Correct
@GetMapping
public R<List<User>> list(@RequestParam Integer status)
```

### Response Format Standards

**Always return `R<T>` wrapper:**

```java
// Success with data
R.ok(data)                    // 200 OK
R.ok("操作成功", data)         // 200 OK with message

// Success without data
R.ok()                        // 200 OK
R.ok("操作成功", null)         // 200 OK with message

// Business error
R.fail("用户不存在")           // 200 OK (业务错误)
throw new BusinessException("用户不存在")  // Converted to R.fail()

// HTTP error codes (handled by framework)
// 400 Bad Request - validation errors
// 401 Unauthorized - not authenticated
// 403 Forbidden - no permission
// 404 Not Found - resource not found
// 500 Internal Server Error - server errors
```

### Path Parameter vs Query Parameter

**Use Path Parameters for:**
- Resource identification: `/user/{id}`
- Hierarchical relationships: `/department/{deptId}/users`
- Required parameters that identify the resource

**Use Query Parameters for:**
- Optional filters: `?status=1`
- Sorting and pagination: `?pageNum=1&pageSize=10`
- Search conditions: `?keyword=张三`
- Any optional parameters

### RESTful Checklist

- [ ] URLs use nouns, not verbs
- [ ] HTTP methods match operations (GET=query, POST=create, PUT=update, DELETE=delete)
- [ ] Path parameters for resource ID
- [ ] Query parameters for filters/pagination
- [ ] Consistent URL naming (kebab-case)
- [ ] Version prefix `/api/v1/`
- [ ] Return `R<T>` wrapper
- [ ] Proper HTTP status codes
- [ ] Swagger `@Operation` annotations

## Controller Standards

### Base Controller Pattern

**Prefer extending `BaseCrudController` for standard CRUD operations:**

```java
@Slf4j
@RestController
@RequestMapping("/v1/system/class")
@RequiredArgsConstructor
@Tag(name = "班级管理", description = "班级增删改查")
public class ClassController extends BaseCrudController<ClassVO, ClassQueryDTO, ClassSaveDTO>
        implements BatchDeleteController, StatusUpdateController {

    private final ClassService classService;

    @Override
    public String getEntityName() {
        return "班级";
    }

    @Override
    protected PageResult<ClassVO> callPageList(ClassQueryDTO queryDTO) {
        return classService.pageList(queryDTO);
    }

    @Override
    protected ClassVO callGetDetailById(Long id) {
        return classService.getDetailById(id);
    }

    @Override
    protected boolean callSave(ClassSaveDTO saveDTO) {
        return classService.saveClass(saveDTO);
    }

    @Override
    protected boolean callDelete(Long id) {
        return classService.deleteClass(id);
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return classService.batchDelete(ids);
    }

    @Override
    public boolean callUpdateStatus(Long id, Integer status) {
        return classService.updateStatus(id, status);
    }
}
```

### Controller Annotations

**Required annotations:**
- `@RestController` - REST controller
- `@RequestMapping("/v1/system/xxx")` - Base path
- `@RequiredArgsConstructor` - Lombok constructor injection
- `@Slf4j` - Logging
- `@Tag(name = "...", description = "...")` - Swagger API group
- `@Operation(summary = "...")` - Swagger operation description
- `@Log(title = "...", businessType = 1)` - Operation log (for POST/PUT/DELETE)

### Response Format

**Always return `R<T>` wrapper:**

```java
// ✅ Correct: Return R wrapper
@GetMapping("/page")
@Operation(summary = "分页查询列表")
public R<PageResult<UserVO>> list(UserQueryDTO queryDTO) {
    PageResult<UserVO> result = userService.pageList(queryDTO);
    return R.ok(result);
}

// ✅ Correct: Success with message
@PostMapping
@Operation(summary = "新增")
@Log(title = "新增用户", businessType = 1)
public R<Void> add(@Valid @RequestBody UserSaveDTO saveDTO) {
    boolean success = userService.saveUser(saveDTO);
    if (success) {
        return R.ok("用户新增成功", null);
    } else {
        return R.fail("用户新增失败");
    }
}
```

## Service Standards

### Service Interface

**Service interface extends `IService<Entity>`:**

```java
public interface ClassService extends IService<Class> {
    PageResult<ClassVO> pageList(ClassQueryDTO queryDTO);
    ClassVO getDetailById(Long id);
    boolean saveClass(ClassSaveDTO saveDTO);
    boolean deleteClass(Long id);
    boolean batchDelete(Long[] ids);
    boolean updateStatus(Long id, Integer status);
}
```

### Service Implementation

**Implementation extends `ServiceImpl<Mapper, Entity>`:**

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class> implements ClassService {

    private final MajorMapper majorMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveClass(ClassSaveDTO saveDTO) {
        // Business logic validation
        if (checkDuplicate(saveDTO)) {
            throw new BusinessException("班级编码已存在");
        }
        
        Class entity = new Class();
        BeanUtil.copyProperties(saveDTO, entity);
        return saveOrUpdate(entity);
    }
}
```

### Standard Service Methods

| Method Pattern | Purpose | Return Type |
|----------------|---------|-------------|
| `pageList(QueryDTO)` | Paginated query | `PageResult<VO>` |
| `getDetailById(Long id)` | Get single item | `VO` |
| `saveXxx(SaveDTO)` | Create or update | `boolean` |
| `deleteXxx(Long id)` | Delete single item | `boolean` |
| `batchDelete(Long[] ids)` | Delete multiple items | `boolean` |
| `updateStatus(Long id, Integer status)` | Update status | `boolean` |

## DTO/VO Standards

### DTO Naming

- **Query DTO**: `XxxQueryDTO` (e.g., `UserQueryDTO`, `ClassQueryDTO`)
- **Save DTO**: `XxxSaveDTO` (e.g., `UserSaveDTO`, `ClassSaveDTO`)
- **Action DTO**: `XxxActionDTO` (e.g., `ApprovalActionDTO`)

### VO Naming

- **View Object**: `XxxVO` (e.g., `UserVO`, `ClassVO`)

### DTO Structure

```java
@Data
@Schema(description = "用户保存请求")
public class UserSaveDTO {
    @Schema(description = "用户ID（编辑时必传）")
    private Long id;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名格式不正确")
    private String username;

    @Schema(description = "状态：1正常 0停用")
    private Integer status;
}
```

### Validation Annotations

**Use Jakarta validation:**
- `@NotBlank` - String not blank
- `@NotNull` - Not null
- `@Email` - Email format
- `@Pattern` - Regex pattern
- `@Size` - Size constraint

## Entity Standards

### Entity Structure

**Entity extends `BaseEntity`:**

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "系统用户实体")
public class User extends BaseEntity {
    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "状态：1正常 0停用")
    @TableField("status")
    private Integer status;
}
```

### BaseEntity Fields

**All entities inherit from `BaseEntity`:**
- `id` - Primary key (Long)
- `createTime` - Creation time (LocalDateTime)
- `updateTime` - Update time (LocalDateTime)
- `createBy` - Creator ID (Long)
- `updateBy` - Updater ID (Long)

## Exception Handling

### Business Exception

**Use `BusinessException` for business errors:**

```java
// ✅ Correct: Throw BusinessException
if (entity == null) {
    throw new BusinessException("班级不存在");
}

if (checkDuplicate(dto)) {
    throw new BusinessException("班级编码已存在");
}
```

**Global exception handler automatically converts to `R.fail()` response.**

### Transaction Management

**Use `@Transactional` for write operations:**

```java
@Override
@Transactional(rollbackFor = Exception.class)
public boolean saveClass(ClassSaveDTO saveDTO) {
    // Multiple database operations
    return saveOrUpdate(entity);
}
```

## Query Patterns

### MyBatis-Plus Query

**Use `LambdaQueryWrapper` for type-safe queries:**

```java
LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
wrapper.like(StrUtil.isNotBlank(queryDTO.getClassCode()), 
             Class::getClassCode, queryDTO.getClassCode())
       .eq(queryDTO.getStatus() != null, 
           Class::getStatus, queryDTO.getStatus())
       .orderByDesc(Class::getEnrollmentYear)
       .orderByAsc(Class::getId);

Page<Class> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
page(page, wrapper);
```

### Object Conversion

**Use `BeanUtil.copyProperties` for DTO/Entity conversion:**

```java
// DTO to Entity
Class entity = new Class();
BeanUtil.copyProperties(saveDTO, entity);

// Entity to VO
ClassVO vo = new ClassVO();
BeanUtil.copyProperties(entity, vo);
```

## Naming Conventions

- **Class names**: PascalCase (`StudentController`, `UserService`, `UserServiceImpl`)
- **Package names**: lowercase (`com.project.core`, `com.project.backend`)
- **File names**: Match class name (`StudentController.java`)
- **Method names**: camelCase (`saveUser`, `getDetailById`, `pageList`)
- **Variable names**: camelCase (`queryDTO`, `userService`, `entity`)
- **Constants**: UPPER_SNAKE_CASE (`SUPER_ADMIN_USERNAME`)

## Code Review Checklist

### Package Structure
- [ ] Follows package structure? (core/backend/app)
- [ ] Follows dependency direction? (downward only)
- [ ] Uses correct package imports? (import core, not backend.common)

### Controller
- [ ] Extends `BaseCrudController` when applicable?
- [ ] Uses `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`?
- [ ] Uses `@Tag` and `@Operation` for Swagger documentation?
- [ ] Uses `@Log` for POST/PUT/DELETE operations?
- [ ] Returns `R<T>` wrapper?
- [ ] Uses `@Valid` for request body validation?

### Service
- [ ] Interface extends `IService<Entity>`?
- [ ] Implementation extends `ServiceImpl<Mapper, Entity>`?
- [ ] Uses `@Service` and `@RequiredArgsConstructor`?
- [ ] Standard method naming? (`pageList`, `getDetailById`, `saveXxx`, etc.)
- [ ] Uses `@Transactional` for write operations?
- [ ] Throws `BusinessException` for business errors?

### DTO/VO
- [ ] DTO naming correct? (`XxxQueryDTO`, `XxxSaveDTO`)
- [ ] VO naming correct? (`XxxVO`)
- [ ] Uses `@Schema` for Swagger documentation?
- [ ] Uses validation annotations? (`@NotBlank`, `@Email`, `@Pattern`, etc.)
- [ ] Uses `@Data` from Lombok?

### Entity
- [ ] Extends `BaseEntity`?
- [ ] Uses `@TableName` and `@TableField`?
- [ ] Uses `@Schema` for documentation?
- [ ] Uses `@Data` and `@EqualsAndHashCode(callSuper = true)`?

### Exception Handling
- [ ] Uses `BusinessException` for business errors?
- [ ] No try-catch for business exceptions (handled globally)?

### General
- [ ] JavaDoc added? (@author 陈鸿昇, @since)
- [ ] Uses `BeanUtil.copyProperties` for object conversion?
- [ ] Uses `LambdaQueryWrapper` for type-safe queries?
- [ ] Code compiles without errors?

## Approval Progress Standards

### ApprovalProgress VO

All VOs with approval workflows must include approval progress information:

```java
@Data
@Schema(description = "审批进度信息")
public class ApprovalProgress implements Serializable {
    @Schema(description = "审批状态：1-待审核 2-已通过 3-已拒绝 4-已完成")
    private Integer status;
    
    @Schema(description = "审批状态文本")
    private String statusText;
    
    @Schema(description = "申请人姓名")
    private String applicantName;
    
    @Schema(description = "流程发起时间")
    private LocalDateTime startTime;
    
    @Schema(description = "当前审批节点名称")
    private String currentNodeName;
    
    @Schema(description = "下一审批人姓名")
    private String nextApproverName;
    
    @Schema(description = "审批进度描述文本")
    private String progressText;
    
    @Schema(description = "已完成节点数")
    private Integer completedNodes;
    
    @Schema(description = "节点总数")
    private Integer totalNodes;
    
    @Schema(description = "审批进度百分比")
    private Integer progressPercent;
    
    @Schema(description = "审批流程节点进度列表")
    private List<ApprovalProgressNode> nodeTimeline;
}
```

### VO Fields

Add these fields to all approval-related VOs:

```java
@Schema(description = "审批实例ID")
private Long approvalInstanceId;

@Schema(description = "审批进度信息")
private ApprovalProgress approvalProgress;
```

### Building ApprovalProgress

In Service `convertToVO()` method:

```java
private CheckInVO convertToVO(CheckIn checkIn) {
    CheckInVO vo = new CheckInVO();
    BeanUtil.copyProperties(checkIn, vo);
    
    // Fill approval progress
    if (checkIn.getApprovalInstanceId() != null) {
        vo.setApprovalInstanceId(checkIn.getApprovalInstanceId());
        vo.setApprovalProgress(buildApprovalProgress(
            checkIn.getApprovalInstanceId(), 
            checkIn.getStatus()
        ));
    }
    
    return vo;
}

private ApprovalProgress buildApprovalProgress(Long approvalInstanceId, Integer status) {
    ApprovalProgress progress = new ApprovalProgress();
    progress.setStatus(status);
    progress.setStatusText(DictUtils.getLabel("dict_code", status, "未知"));
    
    ApprovalInstanceVO instance = approvalService.getInstanceDetail(approvalInstanceId);
    if (instance == null) {
        progress.setProgressText("未知进度");
        progress.setProgressPercent(status != null && status != 1 ? 100 : 0);
        return progress;
    }
    
    progress.setApplicantName(instance.getApplicantName());
    progress.setStartTime(instance.getStartTime());
    progress.setCurrentNodeName(instance.getCurrentNodeName());
    
    List<ApprovalProgressNode> nodeTimeline = buildNodeTimeline(instance);
    progress.setNodeTimeline(nodeTimeline);
    progress.setTotalNodes(nodeTimeline.size());
    progress.setCompletedNodes((int) nodeTimeline.stream()
        .filter(node -> node.getStatus() != null && node.getStatus() == 2)
        .count());
    progress.setProgressPercent(status != null && status != 1
        ? 100
        : (nodeTimeline.isEmpty()
            ? 0
            : Math.round(progress.getCompletedNodes() * 100.0f / nodeTimeline.size())));
    
    if (status == 1) {
        String nodeName = instance.getCurrentNodeName() != null 
            ? instance.getCurrentNodeName() : "待审批";
        String approverName = getNextApproverName(instance) != null 
            ? getNextApproverName(instance) : "未指定";
        progress.setProgressText(String.format("%s(%s)", nodeName, approverName));
    } else if (status == 2) {
        progress.setProgressText("已通过");
    } else if (status == 3) {
        progress.setProgressText("已拒绝");
    } else if (status == 4) {
        progress.setProgressText("已完成");
    } else {
        progress.setProgressText("未知进度");
    }
    
    return progress;
}

private String getNextApproverName(ApprovalInstanceVO instance) {
    if (instance.getNodes() == null || instance.getNodes().isEmpty()) {
        return null;
    }
    
    ApprovalNodeVO currentNode = instance.getNodes().stream()
        .filter(node -> node.getId().equals(instance.getCurrentNodeId()))
        .findFirst()
        .orElse(null);
    
    if (currentNode != null && currentNode.getAssignees() != null 
        && !currentNode.getAssignees().isEmpty()) {
        List<String> approverNames = currentNode.getAssignees().stream()
            .map(ApprovalAssigneeVO::getAssigneeName)
            .collect(Collectors.toList());
        return String.join("、", approverNames);
    }
    
    return null;
}
```

## Performance Standards (N+1 Prevention)

### Mandatory: Batch VO Conversion Pattern

**所有 Service 的列表/分页查询必须使用批量转换模式，严禁在循环中查库。**

```java
// ❌ 禁止：循环中逐条查关联数据（N+1 问题）
List<RepairVO> voList = repairs.stream()
    .map(this::convertToVO)  // 每条记录触发 3 次 DB 查询
    .collect(Collectors.toList());

// ✅ 正确：先批量加载，再用 Map 查找
private List<RepairVO> convertToVOList(List<Repair> repairs) {
    if (repairs == null || repairs.isEmpty()) return new ArrayList<>();

    // 1. 收集所有关联 ID
    Set<Long> studentIds = repairs.stream()
        .map(Repair::getStudentId).filter(Objects::nonNull).collect(Collectors.toSet());
    Set<Long> roomIds = repairs.stream()
        .map(Repair::getRoomId).filter(Objects::nonNull).collect(Collectors.toSet());

    // 2. 批量查询（每种关联只查一次）
    Map<Long, Student> studentMap = studentIds.isEmpty() ? Map.of()
        : studentMapper.selectBatchIds(studentIds).stream()
            .collect(Collectors.toMap(Student::getId, s -> s, (a, b) -> a));
    Map<Long, Room> roomMap = roomIds.isEmpty() ? Map.of()
        : roomMapper.selectBatchIds(roomIds).stream()
            .collect(Collectors.toMap(Room::getId, r -> r, (a, b) -> a));

    // 3. 使用 Map 查找填充 VO
    return repairs.stream()
        .map(r -> convertToVO(r, studentMap, roomMap))
        .collect(Collectors.toList());
}
```

### 规则清单

| 场景 | 禁止做法 | 正确做法 |
|------|---------|---------|
| 列表 VO 转换 | 循环中 `selectById()` | `selectBatchIds()` + Map |
| 统计查询 | 循环中 `count()` | 单条 `GROUP BY` 查询 |
| 批量删除 | 循环中 `deleteById()` | `deleteBatchIds()` |
| 判断关联是否存在 | 循环中 `countByXxxId()` | 批量查询 + `groupingBy` |
| 子表查询 | 循环中 `selectByParentId()` | `IN (parentIds)` + `groupingBy` |

### Mapper 层支持批量查询

当 `BaseMapper` 内置方法不够时，新增自定义批量方法：

```java
// ✅ 用 GROUP BY 替代循环 COUNT
@Select("<script>" +
    "SELECT business_type, COUNT(*) AS cnt " +
    "FROM sys_approval_instance " +
    "WHERE status = 1 AND deleted = 0 " +
    "AND current_node_id IN " +
    "<foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>" +
    "#{id}</foreach> " +
    "GROUP BY business_type" +
    "</script>")
List<Map<String, Object>> countPendingGroupByBusinessType(@Param("nodeIds") Collection<Long> nodeIds);
```

### 不要保留旧的单条转换方法

当新增了批量 `convertToVOList()` 后，删除旧的单条 `convertToVO(Entity)` 方法，避免其他开发者误用。如果详情接口确实只需转换单条，也应复用批量方法：

```java
// ✅ 详情接口复用批量方法
public XxxVO getDetailById(Long id) {
    Xxx entity = EntityUtils.requireNonNull(getById(id), "记录");
    List<XxxVO> voList = convertToVOList(List.of(entity));
    return voList.get(0);
}
```

## Transaction Annotation Standards

```java
// ✅ 读操作加 readOnly（优化数据库连接）
@Override
@Transactional(readOnly = true)
public PageResult<XxxVO> pageList(XxxQueryDTO queryDTO) { ... }

@Override
@Transactional(readOnly = true)
public XxxVO getDetailById(Long id) { ... }

// ✅ 写操作加 rollbackFor
@Override
@Transactional(rollbackFor = Exception.class)
public boolean saveXxx(XxxSaveDTO saveDTO) { ... }
```

## Memory Pagination Prevention

**禁止将全部数据加载到内存再做 Java 层分页。**

```java
// ❌ 禁止：加载全部数据到内存再截取
List<MyApplyVO> allApplies = new ArrayList<>();
allApplies.addAll(queryCheckInApplies(queryDTO));   // 全量
allApplies.addAll(queryTransferApplies(queryDTO));   // 全量
// 然后 subList 分页...

// ✅ 正确：每个子查询加 LIMIT，只取需要的数量
long limit = queryDTO.getPageNum() * queryDTO.getPageSize();
wrapper.orderByDesc(CheckIn::getCreateTime)
       .last("LIMIT " + limit);
```

## Security Standards

### 配置文件安全

```yaml
# ✅ 正确：使用环境变量，带默认值用于本地开发
spring:
  datasource:
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root123}

# ❌ 禁止：直接写明文密码
spring:
  datasource:
    username: root
    password: root123
```

### SQL 日志

```yaml
# ❌ 禁止在 application.yml（主配置）中开启 SQL 日志
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# ✅ 只在 application-dev.yml 中开启
```

### Token 安全

- Access Token TTL 不超过 2 小时，依赖 Refresh Token 续期
- 微信登录等第三方接口必须在生产环境调用真实 API，禁止直接使用前端参数作为身份标识

### CORS 配置

```java
// ✅ 正确：通过配置项控制，生产环境限制域名
@Value("${cors.allowed-origins:*}")
private String allowedOrigins;

// ❌ 禁止：硬编码 addAllowedOriginPattern("*") + setAllowCredentials(true)
```

### 认证拦截器性能

- 拦截器中的用户信息查询必须走 Redis 缓存，禁止每次请求查库
- 缓存 key 格式：`project:login_user:{userId}`，TTL 30 分钟
- 用户状态变更（停用/修改）时清除对应缓存

## Approval Workflow Integration

### 审批状态回写

当审批通过或拒绝时，**必须同步更新业务表状态**：

```java
private void updateBusinessStatus(String businessType, Long businessId, Integer status) {
    switch (businessType) {
        case "check_in" -> {
            CheckIn entity = checkInMapper.selectById(businessId);
            if (entity != null) { entity.setStatus(status); checkInMapper.updateById(entity); }
        }
        case "check_out" -> { /* 同上 */ }
        case "transfer" -> { /* 同上 */ }
        case "stay" -> { /* 同上 */ }
        default -> log.warn("未知业务类型：{}", businessType);
    }
}
```

新增业务类型时，必须在此 switch 中添加对应分支。

### 审批进度构建

使用 `ApprovalProgressBuilder` 组件统一构建审批进度，禁止在各业务 Service 中重复实现：

```java
// ✅ 正确：使用统一组件
private final ApprovalProgressBuilder approvalProgressBuilder;

vo.setApprovalProgress(approvalProgressBuilder.buildProgress(
    entity.getApprovalInstanceId(), entity.getStatus(), "check_in_status"));
```

## Refactoring Guidelines

When adding shared code:

1. **Determine if it belongs to core**:
   - ✅ Business-agnostic infrastructure → `core`
   - ✅ Platform-shared utilities → `core`
   - ❌ Business-related logic → `backend` or `app`

2. **When moving existing code**:
   - From `backend.common` → `core` (if business-agnostic)
   - From `backend.config` → `core.config` (if generic config)
   - Business-related aspects/interceptors stay in `backend`

## Code Review Checklist (Extended)

### Performance
- [ ] 列表/分页接口是否使用批量 VO 转换（`convertToVOList`）？
- [ ] 是否存在循环中查库（N+1）？
- [ ] 统计查询是否使用 `GROUP BY` 而非循环 `COUNT`？
- [ ] 批量删除是否使用 `deleteBatchIds` 而非循环？
- [ ] 多表联合分页是否在 SQL 层加了 `LIMIT`？
- [ ] 是否有不必要的旧单条 `convertToVO()` 方法残留？

### Security
- [ ] 配置文件中的密码是否通过环境变量注入？
- [ ] SQL 日志是否只在 dev profile 中开启？
- [ ] Access Token TTL 是否合理（不超过 2 小时）？
- [ ] 第三方登录接口是否在生产环境调用真实 API？
- [ ] CORS 配置是否通过配置项控制？

### Transaction
- [ ] 读方法是否加了 `@Transactional(readOnly = true)`？
- [ ] 写方法是否加了 `@Transactional(rollbackFor = Exception.class)`？

### Caching
- [ ] 高频查询是否走 Redis 缓存？
- [ ] 缓存是否设置了合理的 TTL？
- [ ] 数据变更时是否清除对应缓存？
