package com.project.backend.statistics.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.backend.accommodation.entity.CheckIn;
import com.project.backend.accommodation.entity.CheckOut;
import com.project.backend.accommodation.entity.Stay;
import com.project.backend.student.entity.Student;
import com.project.backend.accommodation.entity.Transfer;
import com.project.backend.accommodation.mapper.CheckInMapper;
import com.project.backend.accommodation.mapper.CheckOutMapper;
import com.project.backend.accommodation.mapper.StayMapper;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.accommodation.mapper.TransferMapper;
import com.project.backend.approval.service.ApprovalService;
import com.project.backend.approval.vo.ApprovalInstanceVO;
import com.project.backend.approval.vo.ApprovalRecordVO;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.entity.Department;
import com.project.backend.organization.entity.Major;
import com.project.backend.organization.entity.Class;
import com.project.backend.organization.mapper.CampusMapper;
import com.project.backend.organization.mapper.DepartmentMapper;
import com.project.backend.organization.mapper.MajorMapper;
import com.project.backend.organization.mapper.ClassMapper;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.entity.Room;
import com.project.backend.room.mapper.BedMapper;
import com.project.backend.room.mapper.FloorMapper;
import com.project.backend.room.mapper.RoomMapper;
import com.project.backend.statistics.dto.MyApplyQueryDTO;
import com.project.backend.statistics.service.StudentStatisticsService;
import com.project.backend.statistics.vo.ApplyDetailVO;
import com.project.backend.statistics.vo.MyApplyVO;
import com.project.backend.statistics.vo.StudentHomeStatisticsVO;
import com.project.backend.util.DictUtils;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生统计服务实现
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentStatisticsServiceImpl implements StudentStatisticsService {

    private final StudentMapper studentMapper;
    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;
    private final FloorMapper floorMapper;
    private final CampusMapper campusMapper;
    private final DepartmentMapper departmentMapper;
    private final MajorMapper majorMapper;
    private final ClassMapper classMapper;
    private final CheckInMapper checkInMapper;
    private final TransferMapper transferMapper;
    private final CheckOutMapper checkOutMapper;
    private final StayMapper stayMapper;
    private final ApprovalService approvalService;

    @Override
    @Transactional(readOnly = true)
    public StudentHomeStatisticsVO getStudentHomeStatistics(Long studentId) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }

        // 查询学生基本信息
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        // 构建VO
        StudentHomeStatisticsVO vo = new StudentHomeStatisticsVO();
        vo.setStudentId(student.getId());
        vo.setStudentName(student.getStudentName());
        vo.setStudentNo(student.getStudentNo());
        vo.setGender(student.getGender());
        vo.setGenderText(DictUtils.getLabel("sys_user_sex", student.getGender(), "未知"));
        vo.setPhone(student.getPhone());
        vo.setCurrentGrade(student.getCurrentGrade());

        // 查询院系名称
        if (StrUtil.isNotBlank(student.getDeptCode())) {
            LambdaQueryWrapper<Department> deptWrapper = new LambdaQueryWrapper<>();
            deptWrapper.eq(Department::getDeptCode, student.getDeptCode());
            Department department = departmentMapper.selectOne(deptWrapper);
            if (department != null) {
                vo.setDeptName(department.getDeptName());
            }
        }

        // 查询专业名称
        if (StrUtil.isNotBlank(student.getMajorCode())) {
            LambdaQueryWrapper<Major> majorWrapper = new LambdaQueryWrapper<>();
            majorWrapper.eq(Major::getMajorCode, student.getMajorCode());
            Major major = majorMapper.selectOne(majorWrapper);
            if (major != null) {
                vo.setMajorName(major.getMajorName());
            }
        }

        // 查询班级名称
        if (student.getClassId() != null) {
            Class clazz = classMapper.selectById(student.getClassId());
            if (clazz != null) {
                vo.setClassName(clazz.getClassName());
            }
        }

        // 查询宿舍信息
        if (student.getBedId() != null) {
            StudentHomeStatisticsVO.DormInfo dormInfo = buildDormInfo(student);
            vo.setDormInfo(dormInfo);
        }

        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MyApplyVO> getMyApplies(MyApplyQueryDTO queryDTO) {
        if (queryDTO.getStudentId() == null) {
            throw new BusinessException("学生ID不能为空");
        }

        // 查询所有申请记录
        List<MyApplyVO> allApplies = new ArrayList<>();

        // 查询入住申请
        if (queryDTO.getApplyType() == null || "check_in".equals(queryDTO.getApplyType())) {
            List<MyApplyVO> checkInApplies = queryCheckInApplies(queryDTO);
            allApplies.addAll(checkInApplies);
        }

        // 查询调宿申请
        if (queryDTO.getApplyType() == null || "transfer".equals(queryDTO.getApplyType())) {
            List<MyApplyVO> transferApplies = queryTransferApplies(queryDTO);
            allApplies.addAll(transferApplies);
        }

        // 查询退宿申请
        if (queryDTO.getApplyType() == null || "check_out".equals(queryDTO.getApplyType())) {
            List<MyApplyVO> checkOutApplies = queryCheckOutApplies(queryDTO);
            allApplies.addAll(checkOutApplies);
        }

        // 查询留宿申请
        if (queryDTO.getApplyType() == null || "stay".equals(queryDTO.getApplyType())) {
            List<MyApplyVO> stayApplies = queryStayApplies(queryDTO);
            allApplies.addAll(stayApplies);
        }

        // 按createTime倒序排序
        allApplies.sort(Comparator.comparing(MyApplyVO::getCreateTime).reversed());

        // 分页或限制返回
        if (queryDTO.getLimit() != null && queryDTO.getLimit() > 0) {
            // 不分页模式，限制返回条数
            List<MyApplyVO> limitedList = allApplies.stream()
                    .limit(queryDTO.getLimit())
                    .collect(Collectors.toList());
            return PageResult.build(limitedList, (long) limitedList.size(), 1L, (long) queryDTO.getLimit());
        } else {
            // 分页模式
            long pageNum = queryDTO.getPageNum();
            long pageSize = queryDTO.getPageSize();
            long total = allApplies.size();
            long startIndex = (pageNum - 1) * pageSize;
            long endIndex = Math.min(startIndex + pageSize, total);

            List<MyApplyVO> pagedList = new ArrayList<>();
            if (startIndex < total) {
                pagedList = allApplies.subList((int) startIndex, (int) endIndex);
            }

            return PageResult.build(pagedList, total, pageNum, pageSize);
        }
    }

    /**
     * 构建宿舍信息
     */
    private StudentHomeStatisticsVO.DormInfo buildDormInfo(Student student) {
        StudentHomeStatisticsVO.DormInfo dormInfo = new StudentHomeStatisticsVO.DormInfo();

        // 查询床位信息
        Bed bed = null;
        if (student.getBedId() != null) {
            bed = bedMapper.selectById(student.getBedId());
        }

        // 查询房间信息
        Room room = null;
        if (student.getRoomId() != null) {
            room = roomMapper.selectById(student.getRoomId());
        }

        // 查询楼层信息
        Floor floor = null;
        if (student.getFloorId() != null) {
            floor = floorMapper.selectById(student.getFloorId());
        }

        // 查询校区信息
        Campus campus = null;
        if (StrUtil.isNotBlank(student.getCampusCode())) {
            LambdaQueryWrapper<Campus> campusWrapper = new LambdaQueryWrapper<>();
            campusWrapper.eq(Campus::getCampusCode, student.getCampusCode());
            campus = campusMapper.selectOne(campusWrapper);
        }

        // 填充宿舍信息
        String campusName = campus != null ? campus.getCampusName() : null;
        String floorName = floor != null ? floor.getFloorName() : null;
        String roomNumber = room != null ? room.getRoomNumber() : null;
        String bedNumber = bed != null ? bed.getBedNumber() : null;

        dormInfo.setCampusName(campusName);
        dormInfo.setFloorName(floorName);
        dormInfo.setRoomNumber(roomNumber);
        dormInfo.setBedNumber(bedNumber);

        // 构建完整地址
        if (campusName != null && floorName != null && roomNumber != null && bedNumber != null) {
            String fullAddress = String.format("%s-%s-%s-%s", campusName, floorName, roomNumber, bedNumber);
            dormInfo.setFullAddress(fullAddress);
        }

        return dormInfo;
    }

    /**
     * 计算每种类型的查询限制数量
     * 对于分页模式，每种类型最多查询 (pageNum * pageSize) 条记录，避免全表加载
     */
    private long calculateQueryLimit(MyApplyQueryDTO queryDTO) {
        if (queryDTO.getLimit() != null && queryDTO.getLimit() > 0) {
            return queryDTO.getLimit();
        }
        // 分页模式下，每种类型最多取 pageNum * pageSize 条（足够用于合并排序后分页）
        return queryDTO.getPageNum() * queryDTO.getPageSize();
    }

    /**
     * 查询入住申请
     */
    private List<MyApplyVO> queryCheckInApplies(MyApplyQueryDTO queryDTO) {
        long limit = calculateQueryLimit(queryDTO);
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getStudentId, queryDTO.getStudentId())
               .eq(queryDTO.getStatus() != null, CheckIn::getStatus, queryDTO.getStatus())
               .orderByDesc(CheckIn::getCreateTime)
               .last("LIMIT " + limit);

        List<CheckIn> checkIns = checkInMapper.selectList(wrapper);
        return checkIns.stream().map(this::convertCheckInToVO).collect(Collectors.toList());
    }

    /**
     * 查询调宿申请
     */
    private List<MyApplyVO> queryTransferApplies(MyApplyQueryDTO queryDTO) {
        long limit = calculateQueryLimit(queryDTO);
        LambdaQueryWrapper<Transfer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transfer::getStudentId, queryDTO.getStudentId())
               .eq(queryDTO.getStatus() != null, Transfer::getStatus, queryDTO.getStatus())
               .orderByDesc(Transfer::getCreateTime)
               .last("LIMIT " + limit);

        List<Transfer> transfers = transferMapper.selectList(wrapper);
        return transfers.stream().map(this::convertTransferToVO).collect(Collectors.toList());
    }

    /**
     * 查询退宿申请
     */
    private List<MyApplyVO> queryCheckOutApplies(MyApplyQueryDTO queryDTO) {
        long limit = calculateQueryLimit(queryDTO);
        LambdaQueryWrapper<CheckOut> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckOut::getStudentId, queryDTO.getStudentId())
               .eq(queryDTO.getStatus() != null, CheckOut::getStatus, queryDTO.getStatus())
               .orderByDesc(CheckOut::getCreateTime)
               .last("LIMIT " + limit);

        List<CheckOut> checkOuts = checkOutMapper.selectList(wrapper);
        return checkOuts.stream().map(this::convertCheckOutToVO).collect(Collectors.toList());
    }

    /**
     * 查询留宿申请
     */
    private List<MyApplyVO> queryStayApplies(MyApplyQueryDTO queryDTO) {
        long limit = calculateQueryLimit(queryDTO);
        LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stay::getStudentId, queryDTO.getStudentId())
               .eq(queryDTO.getStatus() != null, Stay::getStatus, queryDTO.getStatus())
               .orderByDesc(Stay::getCreateTime)
               .last("LIMIT " + limit);

        List<Stay> stays = stayMapper.selectList(wrapper);
        return stays.stream().map(this::convertStayToVO).collect(Collectors.toList());
    }

    /**
     * 转换入住申请为VO
     */
    private MyApplyVO convertCheckInToVO(CheckIn checkIn) {
        MyApplyVO vo = new MyApplyVO();
        vo.setId(checkIn.getId());
        vo.setApplyType("check_in");
        vo.setApplyTypeText(DictUtils.getLabel("approval_business_type", "check_in", "入住申请"));
        vo.setStatus(checkIn.getStatus());
        vo.setStatusText(DictUtils.getLabel("check_in_status", checkIn.getStatus(), "未知"));
        vo.setApplyDate(checkIn.getApplyDate());
        vo.setReason(checkIn.getApplyReason());
        vo.setApprovalInstanceId(checkIn.getApprovalInstanceId());
        vo.setApproveOpinion(checkIn.getApproveOpinion());
        vo.setCreateTime(checkIn.getCreateTime());

        // 入住特有字段
        vo.setCheckInType(checkIn.getCheckInType());
        vo.setCheckInTypeText(DictUtils.getLabel("check_in_type", checkIn.getCheckInType()));
        vo.setExpectedCheckOutDate(checkIn.getExpectedCheckOutDate());
        vo.setCheckInDate(checkIn.getCheckInDate());

        return vo;
    }

    /**
     * 转换调宿申请为VO
     */
    private MyApplyVO convertTransferToVO(Transfer transfer) {
        MyApplyVO vo = new MyApplyVO();
        vo.setId(transfer.getId());
        vo.setApplyType("transfer");
        vo.setApplyTypeText(DictUtils.getLabel("approval_business_type", "transfer", "调宿申请"));
        vo.setStatus(transfer.getStatus());
        vo.setStatusText(DictUtils.getLabel("transfer_status", transfer.getStatus(), "未知"));
        vo.setApplyDate(transfer.getApplyDate());
        vo.setReason(transfer.getTransferReason());
        vo.setApprovalInstanceId(transfer.getApprovalInstanceId());
        vo.setApproveOpinion(transfer.getApproveOpinion());
        vo.setCreateTime(transfer.getCreateTime());

        // 调宿特有字段
        vo.setOriginalDormAddress(buildDormAddress(
            transfer.getOriginalCampusCode(),
            transfer.getOriginalFloorCode(),
            transfer.getOriginalRoomCode(),
            transfer.getOriginalBedCode()
        ));
        vo.setTargetDormAddress(buildDormAddress(
            transfer.getTargetCampusCode(),
            transfer.getTargetFloorCode(),
            transfer.getTargetRoomCode(),
            transfer.getTargetBedCode()
        ));
        vo.setTransferDate(transfer.getTransferDate());

        return vo;
    }

    /**
     * 转换退宿申请为VO
     */
    private MyApplyVO convertCheckOutToVO(CheckOut checkOut) {
        MyApplyVO vo = new MyApplyVO();
        vo.setId(checkOut.getId());
        vo.setApplyType("check_out");
        vo.setApplyTypeText(DictUtils.getLabel("approval_business_type", "check_out", "退宿申请"));
        vo.setStatus(checkOut.getStatus());
        vo.setStatusText(DictUtils.getLabel("check_out_status", checkOut.getStatus(), "未知"));
        vo.setApplyDate(checkOut.getApplyDate());
        vo.setReason(checkOut.getCheckOutReason());
        vo.setApprovalInstanceId(checkOut.getApprovalInstanceId());
        vo.setApproveOpinion(checkOut.getApproveOpinion());
        vo.setCreateTime(checkOut.getCreateTime());

        // 退宿特有字段
        vo.setCheckOutDate(checkOut.getCheckOutDate());

        return vo;
    }

    /**
     * 转换留宿申请为VO
     */
    private MyApplyVO convertStayToVO(Stay stay) {
        MyApplyVO vo = new MyApplyVO();
        vo.setId(stay.getId());
        vo.setApplyType("stay");
        vo.setApplyTypeText(DictUtils.getLabel("approval_business_type", "stay", "留宿申请"));
        vo.setStatus(stay.getStatus());
        vo.setStatusText(DictUtils.getLabel("stay_status", stay.getStatus(), "未知"));
        vo.setApplyDate(stay.getApplyDate());
        vo.setReason(stay.getStayReason());
        vo.setApprovalInstanceId(stay.getApprovalInstanceId());
        vo.setApproveOpinion(stay.getApproveOpinion());
        vo.setCreateTime(stay.getCreateTime());

        // 留宿特有字段
        vo.setStayStartDate(stay.getStayStartDate());
        vo.setStayEndDate(stay.getStayEndDate());
        vo.setParentName(stay.getParentName());
        vo.setParentPhone(stay.getParentPhone());
        vo.setParentAgree(stay.getParentAgree());
        vo.setParentAgreeText("agree".equals(stay.getParentAgree()) ? "同意" :
                             "disagree".equals(stay.getParentAgree()) ? "不同意" : "未知");

        return vo;
    }

    /**
     * 构建宿舍地址（用于调宿申请）
     */
    private String buildDormAddress(String campusCode, String floorCode, String roomCode, String bedCode) {
        if (StrUtil.isBlank(campusCode) && StrUtil.isBlank(floorCode) &&
            StrUtil.isBlank(roomCode) && StrUtil.isBlank(bedCode)) {
            return null;
        }

        // 查询校区名称
        String campusName = null;
        if (StrUtil.isNotBlank(campusCode)) {
            LambdaQueryWrapper<Campus> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Campus::getCampusCode, campusCode);
            Campus campus = campusMapper.selectOne(wrapper);
            if (campus != null) {
                campusName = campus.getCampusName();
            }
        }

        // 查询楼层名称
        String floorName = null;
        if (StrUtil.isNotBlank(floorCode)) {
            LambdaQueryWrapper<Floor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Floor::getFloorCode, floorCode);
            Floor floor = floorMapper.selectOne(wrapper);
            if (floor != null) {
                floorName = floor.getFloorName();
            }
        }

        // 房间号和床位号直接从code提取
        // 注意：这里假设roomCode和bedCode已经是可读的编号
        // 如果不是，需要查询Room和Bed表获取roomNumber和bedNumber

        return String.format("%s-%s-%s-%s",
            campusName != null ? campusName : campusCode,
            floorName != null ? floorName : floorCode,
            roomCode != null ? roomCode : "",
            bedCode != null ? bedCode : "");
    }

    @Override
    @Transactional(readOnly = true)
    public ApplyDetailVO getApplyDetail(Long studentId, Long applyId, String applyType) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        if (applyId == null) {
            throw new BusinessException("申请ID不能为空");
        }
        if (StrUtil.isBlank(applyType)) {
            throw new BusinessException("申请类型不能为空");
        }

        // 根据申请类型查询对应的申请详情
        ApplyDetailVO vo = null;
        switch (applyType) {
            case "check_in":
                vo = getCheckInDetail(studentId, applyId);
                break;
            case "transfer":
                vo = getTransferDetail(studentId, applyId);
                break;
            case "check_out":
                vo = getCheckOutDetail(studentId, applyId);
                break;
            case "stay":
                vo = getStayDetail(studentId, applyId);
                break;
            default:
                throw new BusinessException("不支持的申请类型：" + applyType);
        }

        // 获取审批流程详情
        if (vo != null && vo.getApprovalInstanceId() != null) {
            fillApprovalDetails(vo);
        }

        return vo;
    }

    /**
     * 获取入住申请详情
     */
    private ApplyDetailVO getCheckInDetail(Long studentId, Long applyId) {
        CheckIn checkIn = checkInMapper.selectById(applyId);
        if (checkIn == null) {
            throw new BusinessException("入住申请不存在");
        }
        if (!checkIn.getStudentId().equals(studentId)) {
            throw new BusinessException("无权查看该申请");
        }

        ApplyDetailVO vo = new ApplyDetailVO();
        vo.setId(checkIn.getId());
        vo.setApplyType("check_in");
        vo.setApplyTypeText(DictUtils.getLabel("approval_business_type", "check_in", "入住申请"));
        vo.setStatus(checkIn.getStatus());
        vo.setStatusText(DictUtils.getLabel("check_in_status", checkIn.getStatus(), "未知"));
        vo.setApplyDate(checkIn.getApplyDate());
        vo.setReason(checkIn.getApplyReason());
        vo.setCreateTime(checkIn.getCreateTime());
        vo.setApproveTime(checkIn.getApproveTime());
        vo.setApprovalInstanceId(checkIn.getApprovalInstanceId());
        vo.setApproveOpinion(checkIn.getApproveOpinion());

        // 入住特有字段
        vo.setCheckInType(checkIn.getCheckInType());
        vo.setCheckInTypeText(DictUtils.getLabel("check_in_type", checkIn.getCheckInType()));
        vo.setExpectedCheckOutDate(checkIn.getExpectedCheckOutDate());
        vo.setCheckInDate(checkIn.getCheckInDate());
        vo.setTargetBedId(checkIn.getBedId());

        // 查询目标宿舍信息
        if (checkIn.getBedId() != null) {
            fillTargetDormInfo(vo, checkIn.getCampusCode(), checkIn.getFloorCode(),
                checkIn.getRoomId(), checkIn.getBedId());
        }

        return vo;
    }

    /**
     * 获取调宿申请详情
     */
    private ApplyDetailVO getTransferDetail(Long studentId, Long applyId) {
        Transfer transfer = transferMapper.selectById(applyId);
        if (transfer == null) {
            throw new BusinessException("调宿申请不存在");
        }
        if (!transfer.getStudentId().equals(studentId)) {
            throw new BusinessException("无权查看该申请");
        }

        ApplyDetailVO vo = new ApplyDetailVO();
        vo.setId(transfer.getId());
        vo.setApplyType("transfer");
        vo.setApplyTypeText(DictUtils.getLabel("approval_business_type", "transfer", "调宿申请"));
        vo.setStatus(transfer.getStatus());
        vo.setStatusText(DictUtils.getLabel("transfer_status", transfer.getStatus(), "未知"));
        vo.setApplyDate(transfer.getApplyDate());
        vo.setReason(transfer.getTransferReason());
        vo.setCreateTime(transfer.getCreateTime());
        vo.setApproveTime(transfer.getApproveTime());
        vo.setApprovalInstanceId(transfer.getApprovalInstanceId());
        vo.setApproveOpinion(transfer.getApproveOpinion());

        // 调宿特有字段
        vo.setOriginalDormAddress(buildDormAddress(
            transfer.getOriginalCampusCode(),
            transfer.getOriginalFloorCode(),
            transfer.getOriginalRoomCode(),
            transfer.getOriginalBedCode()
        ));
        vo.setTargetDormAddress(buildDormAddress(
            transfer.getTargetCampusCode(),
            transfer.getTargetFloorCode(),
            transfer.getTargetRoomCode(),
            transfer.getTargetBedCode()
        ));
        vo.setTransferDate(transfer.getTransferDate());

        return vo;
    }

    /**
     * 获取退宿申请详情
     */
    private ApplyDetailVO getCheckOutDetail(Long studentId, Long applyId) {
        CheckOut checkOut = checkOutMapper.selectById(applyId);
        if (checkOut == null) {
            throw new BusinessException("退宿申请不存在");
        }
        if (!checkOut.getStudentId().equals(studentId)) {
            throw new BusinessException("无权查看该申请");
        }

        ApplyDetailVO vo = new ApplyDetailVO();
        vo.setId(checkOut.getId());
        vo.setApplyType("check_out");
        vo.setApplyTypeText(DictUtils.getLabel("approval_business_type", "check_out", "退宿申请"));
        vo.setStatus(checkOut.getStatus());
        vo.setStatusText(DictUtils.getLabel("check_out_status", checkOut.getStatus(), "未知"));
        vo.setApplyDate(checkOut.getApplyDate());
        vo.setReason(checkOut.getCheckOutReason());
        vo.setCreateTime(checkOut.getCreateTime());
        vo.setApproveTime(checkOut.getApproveTime());
        vo.setApprovalInstanceId(checkOut.getApprovalInstanceId());
        vo.setApproveOpinion(checkOut.getApproveOpinion());

        // 退宿特有字段
        vo.setCheckOutDate(checkOut.getCheckOutDate());
        vo.setCheckOutReason(checkOut.getCheckOutReason());

        return vo;
    }

    /**
     * 获取留宿申请详情
     */
    private ApplyDetailVO getStayDetail(Long studentId, Long applyId) {
        Stay stay = stayMapper.selectById(applyId);
        if (stay == null) {
            throw new BusinessException("留宿申请不存在");
        }
        if (!stay.getStudentId().equals(studentId)) {
            throw new BusinessException("无权查看该申请");
        }

        ApplyDetailVO vo = new ApplyDetailVO();
        vo.setId(stay.getId());
        vo.setApplyType("stay");
        vo.setApplyTypeText(DictUtils.getLabel("approval_business_type", "stay", "留宿申请"));
        vo.setStatus(stay.getStatus());
        vo.setStatusText(DictUtils.getLabel("stay_status", stay.getStatus(), "未知"));
        vo.setApplyDate(stay.getApplyDate());
        vo.setReason(stay.getStayReason());
        vo.setCreateTime(stay.getCreateTime());
        vo.setApproveTime(stay.getApproveTime());
        vo.setApprovalInstanceId(stay.getApprovalInstanceId());
        vo.setApproveOpinion(stay.getApproveOpinion());

        // 留宿特有字段
        vo.setStayStartDate(stay.getStayStartDate());
        vo.setStayEndDate(stay.getStayEndDate());
        vo.setParentName(stay.getParentName());
        vo.setParentPhone(stay.getParentPhone());
        vo.setParentAgree(stay.getParentAgree());
        vo.setParentAgreeText("agree".equals(stay.getParentAgree()) ? "同意" :
                             "disagree".equals(stay.getParentAgree()) ? "不同意" : "未知");
        vo.setStayReason(stay.getStayReason());

        return vo;
    }

    /**
     * 填充目标宿舍信息（用于入住申请）
     */
    private void fillTargetDormInfo(ApplyDetailVO vo, String campusCode, String floorCode,
                                     Long roomId, Long bedId) {
        // 查询校区名称
        String campusName = null;
        if (StrUtil.isNotBlank(campusCode)) {
            LambdaQueryWrapper<Campus> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Campus::getCampusCode, campusCode);
            Campus campus = campusMapper.selectOne(wrapper);
            if (campus != null) {
                campusName = campus.getCampusName();
            }
        }

        // 查询楼层名称
        String floorName = null;
        if (StrUtil.isNotBlank(floorCode)) {
            LambdaQueryWrapper<Floor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Floor::getFloorCode, floorCode);
            Floor floor = floorMapper.selectOne(wrapper);
            if (floor != null) {
                floorName = floor.getFloorName();
            }
        }

        // 查询房间号
        String roomNumber = null;
        if (roomId != null) {
            Room room = roomMapper.selectById(roomId);
            if (room != null) {
                roomNumber = room.getRoomNumber();
            }
        }

        // 查询床位号
        String bedNumber = null;
        if (bedId != null) {
            Bed bed = bedMapper.selectById(bedId);
            if (bed != null) {
                bedNumber = bed.getBedNumber();
            }
        }

        vo.setTargetCampusName(campusName);
        vo.setTargetFloorName(floorName);
        vo.setTargetRoomNumber(roomNumber);
        vo.setTargetBedNumber(bedNumber);

        // 构建完整地址
        if (campusName != null && floorName != null && roomNumber != null && bedNumber != null) {
            String fullAddress = String.format("%s-%s-%s-%s", campusName, floorName, roomNumber, bedNumber);
            vo.setTargetDormFullAddress(fullAddress);
        }
    }

    /**
     * 填充审批流程详情
     */
    private void fillApprovalDetails(ApplyDetailVO vo) {
        try {
            ApprovalInstanceVO instanceVO = approvalService.getInstanceDetail(vo.getApprovalInstanceId());
            if (instanceVO != null) {
                vo.setApprovalStatus(instanceVO.getStatus());
                vo.setApprovalStatusText(DictUtils.getLabel("approval_instance_status", instanceVO.getStatus()));
                vo.setFlowName(instanceVO.getFlowName());
                vo.setCurrentNodeName(instanceVO.getCurrentNodeName());

                // 转换审批记录为审批步骤
                if (instanceVO.getRecords() != null && !instanceVO.getRecords().isEmpty()) {
                    List<ApplyDetailVO.ApprovalStepVO> steps = new ArrayList<>();
                    int stepOrder = 1;
                    for (ApprovalRecordVO record : instanceVO.getRecords()) {
                        ApplyDetailVO.ApprovalStepVO step = new ApplyDetailVO.ApprovalStepVO();
                        step.setStepOrder(stepOrder++);
                        step.setNodeId(record.getNodeId());
                        step.setNodeName(record.getNodeName());
                        step.setApproverId(record.getApproverId());
                        step.setApproverName(record.getApproverName());
                        step.setAction(record.getAction());
                        step.setActionText(record.getActionText());
                        step.setOpinion(record.getOpinion());
                        step.setApproveTime(record.getApproveTime());

                        // 根据操作类型设置状态
                        if (record.getAction() != null) {
                            if (record.getAction() == 1) {
                                step.setStatus(2); // 已通过
                                step.setStatusText("已通过");
                            } else if (record.getAction() == 2) {
                                step.setStatus(3); // 已拒绝
                                step.setStatusText("已拒绝");
                            }
                        } else {
                            step.setStatus(1); // 待审批
                            step.setStatusText("待审批");
                        }

                        steps.add(step);
                    }
                    vo.setApprovalSteps(steps);
                }
            }
        } catch (Exception e) {
            log.error("获取审批流程详情失败，审批实例ID：{}", vo.getApprovalInstanceId(), e);
            // 不抛出异常，避免影响主流程
        }
    }
}
