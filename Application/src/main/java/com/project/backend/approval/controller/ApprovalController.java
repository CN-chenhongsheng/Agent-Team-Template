package com.project.backend.approval.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.project.backend.approval.dto.ApprovalActionDTO;
import com.project.backend.approval.dto.ApprovalFlowBindingDTO;
import com.project.backend.approval.dto.ApprovalFlowQueryDTO;
import com.project.backend.approval.dto.ApprovalFlowSaveDTO;
import com.project.backend.approval.dto.ApprovalInstanceQueryDTO;
import com.project.backend.approval.dto.ApprovalRecordQueryDTO;
import com.project.backend.approval.service.ApprovalFlowService;
import com.project.backend.approval.service.ApprovalService;
import com.project.backend.approval.vo.ApprovalFlowBindingVO;
import com.project.backend.approval.vo.ApprovalFlowVO;
import com.project.backend.approval.vo.ApprovalInstanceVO;
import com.project.backend.approval.vo.ApprovalRecordVO;
import com.project.core.annotation.Log;
import com.project.core.result.PageResult;
import com.project.core.result.R;
import com.project.backend.system.mapper.RoleMapper;
import com.project.backend.system.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审批管理控制器
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/approval")
@RequiredArgsConstructor
@Tag(name = "审批管理", description = "审批流程配置、审批操作等")
public class ApprovalController {

    private final ApprovalFlowService flowService;
    private final ApprovalService approvalService;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;

    // ==================== 流程配置接口 ====================

    @GetMapping("/flow/page")
    @Operation(summary = "查询流程列表（分页）")
    public R<PageResult<ApprovalFlowVO>> listFlows(ApprovalFlowQueryDTO queryDTO) {
        log.info("查询审批流程列表，参数：{}", queryDTO);
        PageResult<ApprovalFlowVO> result = flowService.pageList(queryDTO);
        return R.ok(result);
    }

    @GetMapping("/flow/all")
    @Operation(summary = "查询所有流程（不分页）")
    @Parameter(name = "businessType", description = "业务类型（可选）")
    public R<List<ApprovalFlowVO>> listAllFlows(@RequestParam(required = false) String businessType) {
        log.info("查询所有审批流程，业务类型：{}", businessType);
        List<ApprovalFlowVO> list = flowService.listAll(businessType);
        return R.ok(list);
    }

    @GetMapping("/flow/{id}")
    @Operation(summary = "查询流程详情")
    @Parameter(name = "id", description = "流程ID", required = true)
    public R<ApprovalFlowVO> getFlowDetail(@PathVariable Long id) {
        log.info("查询审批流程详情，ID：{}", id);
        ApprovalFlowVO vo = flowService.getDetailById(id);
        return R.ok(vo);
    }

    @PostMapping("/flow")
    @Operation(summary = "新增流程")
    @Log(title = "新增审批流程", businessType = 1)
    public R<Void> addFlow(@Valid @RequestBody ApprovalFlowSaveDTO saveDTO) {
        log.info("新增审批流程，参数：{}", saveDTO);
        saveDTO.setId(null);
        boolean success = flowService.saveFlow(saveDTO);
        return success ? R.ok("流程新增成功", null) : R.fail("流程新增失败");
    }

    @PutMapping("/flow/{id}")
    @Operation(summary = "编辑流程")
    @Parameter(name = "id", description = "流程ID", required = true)
    @Log(title = "编辑审批流程", businessType = 2)
    public R<Void> updateFlow(@PathVariable Long id, @Valid @RequestBody ApprovalFlowSaveDTO saveDTO) {
        log.info("编辑审批流程，ID：{}，参数：{}", id, saveDTO);
        saveDTO.setId(id);
        boolean success = flowService.saveFlow(saveDTO);
        return success ? R.ok("流程编辑成功", null) : R.fail("流程编辑失败");
    }

    @DeleteMapping("/flow/{id}")
    @Operation(summary = "删除流程")
    @Parameter(name = "id", description = "流程ID", required = true)
    @Log(title = "删除审批流程", businessType = 3)
    public R<Void> deleteFlow(@PathVariable Long id) {
        log.info("删除审批流程，ID：{}", id);
        boolean success = flowService.deleteFlow(id);
        return success ? R.ok("流程删除成功", null) : R.fail("流程删除失败");
    }

    @PutMapping("/flow/{id}/status/{status}")
    @Operation(summary = "修改流程状态")
    @Parameter(name = "id", description = "流程ID", required = true)
    @Parameter(name = "status", description = "状态：0停用 1启用", required = true)
    @Log(title = "修改审批流程状态", businessType = 2)
    public R<Void> updateFlowStatus(@PathVariable Long id, @PathVariable Integer status) {
        log.info("修改审批流程状态，ID：{}，状态：{}", id, status);
        boolean success = flowService.updateStatus(id, status);
        return success ? R.ok(status == 1 ? "流程已启用" : "流程已停用", null) : R.fail("状态修改失败");
    }

    // ==================== 流程绑定接口 ====================

    @GetMapping("/binding/list")
    @Operation(summary = "查询所有流程绑定")
    public R<List<ApprovalFlowBindingVO>> listBindings() {
        log.info("查询所有流程绑定");
        List<ApprovalFlowBindingVO> list = flowService.listAllBindings();
        return R.ok(list);
    }

    @GetMapping("/binding/{businessType}")
    @Operation(summary = "查询业务类型绑定")
    @Parameter(name = "businessType", description = "业务类型", required = true)
    public R<ApprovalFlowBindingVO> getBinding(@PathVariable String businessType) {
        log.info("查询业务类型绑定，类型：{}", businessType);
        ApprovalFlowBindingVO vo = flowService.getBindingByBusinessType(businessType);
        return R.ok(vo);
    }

    @PostMapping("/binding")
    @Operation(summary = "绑定流程")
    @Log(title = "绑定审批流程", businessType = 1)
    public R<Void> bindFlow(@Valid @RequestBody ApprovalFlowBindingDTO bindingDTO) {
        log.info("绑定审批流程，参数：{}", bindingDTO);
        boolean success = flowService.bindFlow(bindingDTO);
        return success ? R.ok("绑定成功", null) : R.fail("绑定失败");
    }

    @DeleteMapping("/binding/{businessType}")
    @Operation(summary = "解绑流程")
    @Parameter(name = "businessType", description = "业务类型", required = true)
    @Log(title = "解绑审批流程", businessType = 3)
    public R<Void> unbindFlow(@PathVariable String businessType) {
        log.info("解绑审批流程，业务类型：{}", businessType);
        boolean success = flowService.unbindFlow(businessType);
        return success ? R.ok("解绑成功", null) : R.fail("解绑失败");
    }

    // ==================== 审批操作接口 ====================

    @GetMapping("/instance/{id}")
    @Operation(summary = "查询审批实例详情")
    @Parameter(name = "id", description = "审批实例ID", required = true)
    public R<ApprovalInstanceVO> getInstanceDetail(@PathVariable Long id) {
        log.info("查询审批实例详情，ID：{}", id);
        ApprovalInstanceVO vo = approvalService.getInstanceDetail(id);
        if (vo != null) {
            // 检查当前用户是否可审批
            Long userId = StpUtil.getLoginIdAsLong();
            List<Long> roleIds = roleMapper.selectRoleIdsByUserId(userId);
            vo.setCanApprove(approvalService.canApprove(id, userId, roleIds));
        }
        return R.ok(vo);
    }

    @GetMapping("/instance/business/{businessType}/{businessId}")
    @Operation(summary = "根据业务查询审批实例")
    @Parameter(name = "businessType", description = "业务类型", required = true)
    @Parameter(name = "businessId", description = "业务ID", required = true)
    public R<ApprovalInstanceVO> getInstanceByBusiness(
            @PathVariable String businessType,
            @PathVariable Long businessId) {
        log.info("根据业务查询审批实例，类型：{}，ID：{}", businessType, businessId);
        ApprovalInstanceVO vo = approvalService.getInstanceByBusiness(businessType, businessId);
        if (vo != null) {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Long> roleIds = roleMapper.selectRoleIdsByUserId(userId);
            vo.setCanApprove(approvalService.canApprove(vo.getId(), userId, roleIds));
        }
        return R.ok(vo);
    }

    @PutMapping("/approve")
    @Operation(summary = "执行审批")
    @Log(title = "执行审批", businessType = 0)
    public R<Void> doApprove(@Valid @RequestBody ApprovalActionDTO actionDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        String nickname = userMapper.selectById(userId).getNickname();
        log.info("执行审批，用户：{}，参数：{}", nickname, actionDTO);

        List<Long> roleIds = roleMapper.selectRoleIdsByUserId(userId);
        // 验证权限
        if (!approvalService.canApprove(actionDTO.getInstanceId(), userId, roleIds)) {
            return R.fail("您没有权限审批此申请");
        }

        boolean success = approvalService.doApprove(actionDTO, userId, nickname);
        return success ? R.ok("审批成功", null) : R.fail("审批失败");
    }

    @PutMapping("/withdraw/{instanceId}")
    @Operation(summary = "撤回审批")
    @Parameter(name = "instanceId", description = "审批实例ID", required = true)
    @Log(title = "撤回审批", businessType = 0)
    public R<Void> withdraw(@PathVariable Long instanceId) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("撤回审批，用户：{}，实例ID：{}", userId, instanceId);
        boolean success = approvalService.withdraw(instanceId, userId);
        return success ? R.ok("撤回成功", null) : R.fail("撤回失败");
    }

    // ==================== 待办/已办查询接口 ====================

    @GetMapping("/pending/page")
    @Operation(summary = "查询待办审批列表（分页）")
    public R<PageResult<ApprovalInstanceVO>> listPending(ApprovalInstanceQueryDTO queryDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Long> roleIds = roleMapper.selectRoleIdsByUserId(userId);
        log.info("查询待办审批列表，用户：{}，参数：{}", userId, queryDTO);
        PageResult<ApprovalInstanceVO> result = approvalService.pagePendingList(queryDTO, userId, roleIds);
        return R.ok(result);
    }

    @GetMapping("/pending/count")
    @Operation(summary = "查询待办审批数量统计")
    public R<Map<String, Long>> getPendingCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Long> roleIds = roleMapper.selectRoleIdsByUserId(userId);
        log.info("查询待办审批数量，用户：{}", userId);
        Map<String, Long> countMap = approvalService.getPendingCount(userId, roleIds);
        return R.ok(countMap);
    }

    @GetMapping("/record/page")
    @Operation(summary = "查询审批记录列表（分页）")
    public R<PageResult<ApprovalRecordVO>> listRecords(ApprovalRecordQueryDTO queryDTO) {
        log.info("查询审批记录列表，参数：{}", queryDTO);
        // 不传approverId则查询所有
        PageResult<ApprovalRecordVO> result = approvalService.pageRecordList(queryDTO, null);
        return R.ok(result);
    }

    @GetMapping("/record/my-page")
    @Operation(summary = "查询我的审批记录（分页）")
    public R<PageResult<ApprovalRecordVO>> listMyRecords(ApprovalRecordQueryDTO queryDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("查询我的审批记录，用户：{}，参数：{}", userId, queryDTO);
        PageResult<ApprovalRecordVO> result = approvalService.pageRecordList(queryDTO, userId);
        return R.ok(result);
    }

    @GetMapping("/record/instance/{instanceId}")
    @Operation(summary = "查询审批实例的审批记录")
    @Parameter(name = "instanceId", description = "审批实例ID", required = true)
    public R<List<ApprovalRecordVO>> getRecordsByInstance(@PathVariable Long instanceId) {
        log.info("查询审批实例的审批记录，实例ID：{}", instanceId);
        List<ApprovalRecordVO> records = approvalService.getRecordsByInstance(instanceId);
        return R.ok(records);
    }
}
