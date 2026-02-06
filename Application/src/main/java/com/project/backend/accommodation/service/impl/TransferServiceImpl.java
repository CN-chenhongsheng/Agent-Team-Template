package com.project.backend.accommodation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.backend.accommodation.dto.transfer.TransferQueryDTO;
import com.project.backend.accommodation.dto.transfer.TransferSaveDTO;
import com.project.backend.accommodation.entity.Transfer;
import com.project.backend.accommodation.mapper.TransferMapper;
import com.project.backend.accommodation.service.TransferService;
import com.project.backend.accommodation.vo.TransferVO;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.core.vo.ApprovalProgress;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.common.service.ApprovalProgressBuilder;
import com.project.backend.common.service.StudentInfoEnricher;
import com.project.core.util.EntityUtils;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.mapper.CampusMapper;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.entity.Room;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.mapper.FloorMapper;
import com.project.backend.room.mapper.RoomMapper;
import com.project.backend.room.mapper.BedMapper;
import com.project.backend.approval.service.ApprovalService;
import com.project.backend.approval.mapper.ApprovalInstanceMapper;
import com.project.backend.approval.mapper.ApprovalRecordMapper;
import com.project.backend.approval.entity.ApprovalRecord;
import com.project.backend.util.DictUtils;
import com.project.core.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 调宿管理Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl extends ServiceImpl<TransferMapper, Transfer> implements TransferService {

    private final StudentMapper studentMapper;
    private final CampusMapper campusMapper;
    private final FloorMapper floorMapper;
    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;
    private final StudentInfoEnricher studentInfoEnricher;
    private final ApprovalService approvalService;
    private final ApprovalInstanceMapper approvalInstanceMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final ApprovalProgressBuilder approvalProgressBuilder;

    @Override
    public PageResult<TransferVO> pageList(TransferQueryDTO queryDTO) {
        LambdaQueryWrapper<Transfer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getStudentNo()), Transfer::getStudentNo, queryDTO.getStudentNo())
               .like(StrUtil.isNotBlank(queryDTO.getStudentName()), Transfer::getStudentName, queryDTO.getStudentName())
               .eq(queryDTO.getStudentId() != null, Transfer::getStudentId, queryDTO.getStudentId())
               .eq(StrUtil.isNotBlank(queryDTO.getOriginalCampusCode()), Transfer::getOriginalCampusCode, queryDTO.getOriginalCampusCode())
               .eq(StrUtil.isNotBlank(queryDTO.getTargetCampusCode()), Transfer::getTargetCampusCode, queryDTO.getTargetCampusCode())
               .eq(queryDTO.getStatus() != null, Transfer::getStatus, queryDTO.getStatus())
               .ge(queryDTO.getApplyDateStart() != null, Transfer::getApplyDate, queryDTO.getApplyDateStart())
               .le(queryDTO.getApplyDateEnd() != null, Transfer::getApplyDate, queryDTO.getApplyDateEnd())
               .orderByDesc(Transfer::getCreateTime);

        Page<Transfer> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<TransferVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public TransferVO getDetailById(Long id) {
        Transfer transfer = EntityUtils.requireNonNull(getById(id), "调宿记录");
        return convertToVO(transfer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveTransfer(TransferSaveDTO saveDTO) {
        // 获取学生ID：优先使用 DTO 中的值（管理端编辑），否则从 UserContext 获取（学生端提交）
        Long studentId = saveDTO.getStudentId();
        if (studentId == null) {
            studentId = UserContext.getUserId();
            if (studentId == null) {
                throw new BusinessException("用户未登录");
            }
            saveDTO.setStudentId(studentId);
        }

        // 检查学生是否存在
        Student student = EntityUtils.requireNonNull(studentMapper.selectById(studentId), "学生");

        Transfer transfer = new Transfer();
        BeanUtil.copyProperties(saveDTO, transfer);

        // 填充学生冗余字段
        transfer.setStudentName(student.getStudentName());
        transfer.setStudentNo(student.getStudentNo());

        boolean isNew = saveDTO.getId() == null;

        if (isNew) {
            // 自动设置申请日期为当前日期
            if (transfer.getApplyDate() == null) {
                transfer.setApplyDate(LocalDate.now());
            }

            // 自动填充原住宿信息（从学生表中获取当前住宿信息）
            if (student.getBedId() != null) {
                transfer.setOriginalCampusCode(student.getCampusCode());
                transfer.setOriginalFloorCode(student.getFloorCode());
                transfer.setOriginalRoomId(student.getRoomId());
                transfer.setOriginalRoomCode(student.getRoomCode());
                transfer.setOriginalBedId(student.getBedId());
                transfer.setOriginalBedCode(student.getBedCode());
                log.info("自动填充原住宿信息，学生ID：{}，原床位ID：{}", studentId, student.getBedId());
            } else {
                log.warn("学生当前无住宿信息，学生ID：{}", studentId);
            }

            // 自动填充目标房间编码和床位编码（根据ID查询）
            if (transfer.getTargetRoomId() != null && StrUtil.isBlank(transfer.getTargetRoomCode())) {
                Room targetRoom = roomMapper.selectById(transfer.getTargetRoomId());
                if (targetRoom != null) {
                    transfer.setTargetRoomCode(targetRoom.getRoomNumber());
                }
            }
            if (transfer.getTargetBedId() != null && StrUtil.isBlank(transfer.getTargetBedCode())) {
                Bed targetBed = bedMapper.selectById(transfer.getTargetBedId());
                if (targetBed != null) {
                    transfer.setTargetBedCode(targetBed.getBedNumber());
                }
            }

            // 新增时先保存记录（需要获取ID）
            if (transfer.getStatus() == null) {
                transfer.setStatus(1); // 临时状态，后续会根据审批结果更新
            }
            save(transfer);

            // 发起审批流程
            Long instanceId = approvalService.startApproval(
                "transfer",
                transfer.getId(),
                saveDTO.getStudentId(),
                student.getStudentName()
            );

            if (instanceId != null) {
                // 有审批流程，状态设为"待审批"（状态1）
                transfer.setApprovalInstanceId(instanceId);
                transfer.setStatus(1);
                log.info("调宿申请已发起审批，申请ID：{}，审批实例ID：{}", transfer.getId(), instanceId);
            } else {
                // 无审批流程，直接通过，状态设为"已通过"（状态2）
                transfer.setStatus(2);
                log.info("调宿申请无需审批，直接通过，申请ID：{}", transfer.getId());
            }

            return updateById(transfer);
        } else {
            return updateById(transfer);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTransfer(Long id) {
        if (id == null) {
            throw new BusinessException("调宿记录ID不能为空");
        }

        // 查询调宿记录
        Transfer transfer = EntityUtils.requireNonNull(getById(id), "调宿记录");

        // 如果存在审批实例，删除关联的审批记录和审批实例
        if (transfer.getApprovalInstanceId() != null) {
            Long instanceId = transfer.getApprovalInstanceId();

            // 删除审批记录
            LambdaQueryWrapper<ApprovalRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(ApprovalRecord::getInstanceId, instanceId);
            approvalRecordMapper.delete(recordWrapper);

            // 删除审批实例
            approvalInstanceMapper.deleteById(instanceId);

            log.info("删除调宿记录时同步删除审批实例，调宿记录ID：{}，审批实例ID：{}", id, instanceId);
        }

        // 删除调宿记录
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("调宿记录ID不能为空");
        }

        // 查询所有调宿记录
        List<Transfer> transferList = listByIds(Arrays.asList(ids));
        if (transferList.isEmpty()) {
            throw new BusinessException("调宿记录不存在");
        }

        // 收集所有需要删除的审批实例ID
        List<Long> instanceIds = transferList.stream()
                .map(Transfer::getApprovalInstanceId)
                .filter(instanceId -> instanceId != null)
                .distinct()
                .collect(Collectors.toList());

        // 批量删除审批记录和审批实例
        if (!instanceIds.isEmpty()) {
            // 删除审批记录
            LambdaQueryWrapper<ApprovalRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.in(ApprovalRecord::getInstanceId, instanceIds);
            approvalRecordMapper.delete(recordWrapper);

            // 删除审批实例（循环删除，因为 BaseMapper 没有批量删除方法）
            for (Long instanceId : instanceIds) {
                approvalInstanceMapper.deleteById(instanceId);
            }

            log.info("批量删除调宿记录时同步删除审批实例，调宿记录数量：{}，审批实例数量：{}",
                    transferList.size(), instanceIds.size());
        }

        // 批量删除调宿记录
        return removeByIds(Arrays.asList(ids));
    }

    /**
     * 撤回调宿申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTransfer(Long id) {
        if (id == null) {
            throw new BusinessException("调宿记录ID不能为空");
        }

        Transfer transfer = EntityUtils.requireNonNull(getById(id), "调宿记录");

        // 只有待审核状态才能撤回
        if (transfer.getStatus() != 1) {
            throw new BusinessException("只有待审核状态的申请才能撤回");
        }

        // 更新状态为已撤回（状态5）
        transfer.setStatus(5);
        return updateById(transfer);
    }

    /**
     * 实体转VO
     */
    private TransferVO convertToVO(Transfer transfer) {
        TransferVO vo = new TransferVO();
        BeanUtil.copyProperties(transfer, vo);
        vo.setStatusText(DictUtils.getLabel("transfer_status", transfer.getStatus(), "未知"));

        // 查询原校区名称
        if (StrUtil.isNotBlank(transfer.getOriginalCampusCode())) {
            LambdaQueryWrapper<Campus> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Campus::getCampusCode, transfer.getOriginalCampusCode());
            Campus campus = campusMapper.selectOne(wrapper);
            if (campus != null) {
                vo.setOriginalCampusName(campus.getCampusName());
            }
        }

        // 查询原楼栋名称
        if (StrUtil.isNotBlank(transfer.getOriginalFloorCode())) {
            LambdaQueryWrapper<Floor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Floor::getFloorCode, transfer.getOriginalFloorCode());
            Floor floor = floorMapper.selectOne(wrapper);
            if (floor != null) {
                vo.setOriginalFloorName(floor.getFloorName());
            }
        }

        // 查询原房间名称（不覆盖历史 roomCode，保留创建时的值）
        if (transfer.getOriginalRoomId() != null) {
            Room room = roomMapper.selectById(transfer.getOriginalRoomId());
            if (room != null) {
                vo.setOriginalRoomName(room.getRoomNumber());
            }
        }

        // 查询原床位名称（不覆盖历史 bedCode，保留创建时的值）
        if (transfer.getOriginalBedId() != null) {
            Bed bed = bedMapper.selectById(transfer.getOriginalBedId());
            if (bed != null) {
                vo.setOriginalBedName(bed.getBedNumber());
            }
        }

        // 查询目标校区名称
        if (StrUtil.isNotBlank(transfer.getTargetCampusCode())) {
            LambdaQueryWrapper<Campus> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Campus::getCampusCode, transfer.getTargetCampusCode());
            Campus campus = campusMapper.selectOne(wrapper);
            if (campus != null) {
                vo.setTargetCampusName(campus.getCampusName());
            }
        }

        // 查询目标楼栋名称
        if (StrUtil.isNotBlank(transfer.getTargetFloorCode())) {
            LambdaQueryWrapper<Floor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Floor::getFloorCode, transfer.getTargetFloorCode());
            Floor floor = floorMapper.selectOne(wrapper);
            if (floor != null) {
                vo.setTargetFloorName(floor.getFloorName());
            }
        }

        // 查询目标房间名称（不覆盖历史 roomCode，保留创建时的值）
        if (transfer.getTargetRoomId() != null) {
            Room room = roomMapper.selectById(transfer.getTargetRoomId());
            if (room != null) {
                vo.setTargetRoomName(room.getRoomNumber());
            }
        }

        // 查询目标床位名称（不覆盖历史 bedCode，保留创建时的值）
        if (transfer.getTargetBedId() != null) {
            Bed bed = bedMapper.selectById(transfer.getTargetBedId());
            if (bed != null) {
                vo.setTargetBedName(bed.getBedNumber());
            }
        }

        // 填充学生详细信息
        if (transfer.getStudentId() != null) {
            Student student = studentMapper.selectById(transfer.getStudentId());
            if (student != null) {
                studentInfoEnricher.enrichStudentInfo(student, vo, "originalCampusName");
            }
        }

        // 填充审批进度信息
        if (transfer.getApprovalInstanceId() != null) {
            vo.setApprovalInstanceId(transfer.getApprovalInstanceId());
            vo.setApprovalProgress(approvalProgressBuilder.buildProgress(transfer.getApprovalInstanceId(), transfer.getStatus(), "transfer_status"));
        }

        return vo;
    }

    /**
     * 构建审批进度信息
     */
}