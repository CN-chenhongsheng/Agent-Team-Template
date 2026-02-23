package com.project.backend.room.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.mapper.CampusMapper;
import com.project.backend.room.dto.floor.FloorQueryDTO;
import com.project.backend.room.dto.floor.FloorSaveDTO;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.entity.Room;
import com.project.backend.room.mapper.BedMapper;
import com.project.backend.room.mapper.FloorMapper;
import com.project.backend.room.mapper.RoomMapper;
import com.project.backend.room.service.FloorService;
import com.project.backend.room.vo.FloorVO;
import com.project.backend.util.DictUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 楼层Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FloorServiceImpl extends ServiceImpl<FloorMapper, Floor> implements FloorService {

    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;
    private final CampusMapper campusMapper;

    @Override
    public PageResult<FloorVO> pageList(FloorQueryDTO queryDTO) {
        LambdaQueryWrapper<Floor> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getFloorCode()), Floor::getFloorCode, queryDTO.getFloorCode())
               .like(StrUtil.isNotBlank(queryDTO.getFloorName()), Floor::getFloorName, queryDTO.getFloorName())
               .eq(StrUtil.isNotBlank(queryDTO.getCampusCode()), Floor::getCampusCode, queryDTO.getCampusCode())
               .eq(queryDTO.getGenderType() != null, Floor::getGenderType, queryDTO.getGenderType())
               .eq(queryDTO.getStatus() != null, Floor::getStatus, queryDTO.getStatus())
               .orderByAsc(Floor::getSort)
               .orderByAsc(Floor::getId);

        Page<Floor> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<FloorVO> voList = convertToVOList(page.getRecords());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public FloorVO getDetailById(Long id) {
        Floor floor = getById(id);
        if (floor == null) {
            throw new BusinessException("楼层不存在");
        }
        return convertToVO(floor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveFloor(FloorSaveDTO saveDTO) {
        // 检查编码是否重复
        LambdaQueryWrapper<Floor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Floor::getFloorCode, saveDTO.getFloorCode())
               .eq(Floor::getDeleted, 0);
        if (saveDTO.getId() != null) {
            wrapper.ne(Floor::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("楼层编码已存在");
        }


        Floor floor = new Floor();
        BeanUtil.copyProperties(saveDTO, floor);

        if (saveDTO.getId() == null) {
            floor.setStatus(floor.getStatus() != null ? floor.getStatus() : 1);
            floor.setSort(floor.getSort() != null ? floor.getSort() : 0);
            floor.setTotalRooms(0);
            floor.setTotalBeds(0);
            floor.setCurrentOccupancy(0);
            return save(floor);
        } else {
            return updateById(floor);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFloor(Long id) {
        if (id == null) {
            throw new BusinessException("楼层ID不能为空");
        }

        Floor floor = getById(id);
        if (floor == null) {
            throw new BusinessException("楼层不存在");
        }

        // ========== 级联删除房间、床位==========
        // 查询所有属于该楼层的房间
        LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Room::getFloorId, id);
        List<Room> rooms = roomMapper.selectList(roomWrapper);

        // 获取所有房间ID
        List<Long> roomIds = rooms.stream()
                .map(Room::getId)
                .collect(Collectors.toList());

        if (!roomIds.isEmpty()) {
            // 查询所有属于这些房间的床位
            LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
            bedWrapper.in(Bed::getRoomId, roomIds);
            List<Bed> beds = bedMapper.selectList(bedWrapper);

            // 处理床位的学生关联关系：清空学生关联字段，但不删除学生
            for (Bed bed : beds) {
                if (bed.getStudentId() != null) {
                    bed.setStudentId(null);
                    bed.setStudentName(null);
                    bed.setCheckInDate(null);
                    bed.setCheckOutDate(null);
                    bed.setBedStatus(1); // 设为空闲状态
                    bedMapper.updateById(bed);
                }
            }

            // 删除所有床位
            bedMapper.delete(bedWrapper);
        }

        // 删除所有房间
        roomMapper.delete(roomWrapper);

        // 删除当前楼层
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("楼层ID不能为空");
        }

        // 批量删除时，对每个ID调用单个删除方法，确保级联删除逻辑被执行
        for (Long id : ids) {
            deleteFloor(id);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Floor floor = getById(id);
        if (floor == null) {
            throw new BusinessException("楼层不存在");
        }

        // 如果要启用楼层，需要检查所属校区是否启用
        if (status == 1 && StrUtil.isNotBlank(floor.getCampusCode())) {
            LambdaQueryWrapper<Campus> campusWrapper = new LambdaQueryWrapper<>();
            campusWrapper.eq(Campus::getCampusCode, floor.getCampusCode());
            Campus campus = campusMapper.selectOne(campusWrapper);
            if (campus != null && campus.getStatus() != null && campus.getStatus() == 0) {
                throw new BusinessException("该校区处于停用状态，不允许启用楼层");
            }
        }

        floor.setStatus(status);
        boolean result = updateById(floor);

        // 如果状态改为停用（0），则级联停用该楼层下的所有房间和床位
        if (status == 0) {
            // 查询所有属于该楼层的房间ID
            LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
            roomWrapper.eq(Room::getFloorId, id);
            List<Room> rooms = roomMapper.selectList(roomWrapper);
            List<Long> roomIds = rooms.stream()
                    .map(Room::getId)
                    .collect(Collectors.toList());

            // 级联停用所有房间
            if (!roomIds.isEmpty()) {
                Room roomUpdate = new Room();
                roomUpdate.setStatus(0);
                roomMapper.update(roomUpdate, roomWrapper);

                // 级联停用所有床位
                LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
                bedWrapper.in(Bed::getRoomId, roomIds);
                Bed bedUpdate = new Bed();
                bedUpdate.setStatus(0);
                bedMapper.update(bedUpdate, bedWrapper);
            }
        }

        return result;
    }

    /**
     * 批量转换实体到VO（避免N+1查询）
     */
    private List<FloorVO> convertToVOList(List<Floor> floors) {
        if (floors == null || floors.isEmpty()) {
            return List.of();
        }

        // 批量加载所有关联的校区
        Set<String> campusCodes = floors.stream()
                .map(Floor::getCampusCode)
                .filter(code -> StrUtil.isNotBlank(code))
                .collect(Collectors.toSet());
        Map<String, Campus> campusMap = campusCodes.isEmpty() ? Map.of() :
                campusMapper.selectList(
                        new LambdaQueryWrapper<Campus>().in(Campus::getCampusCode, campusCodes)
                ).stream().collect(Collectors.toMap(Campus::getCampusCode, c -> c));

        return floors.stream().map(floor -> {
            FloorVO vo = new FloorVO();
            BeanUtil.copyProperties(floor, vo);
            vo.setStatusText(DictUtils.getLabel("sys_common_status", floor.getStatus(), "未知"));
            vo.setGenderTypeText(DictUtils.getLabel("dormitory_gender_type", floor.getGenderType(), "未知"));
            if (StrUtil.isNotBlank(floor.getCampusCode())) {
                Campus campus = campusMap.get(floor.getCampusCode());
                if (campus != null) {
                    vo.setCampusName(campus.getCampusName());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 实体转VO
     */
    private FloorVO convertToVO(Floor floor) {
        FloorVO vo = new FloorVO();
        BeanUtil.copyProperties(floor, vo);
        vo.setStatusText(DictUtils.getLabel("sys_common_status", floor.getStatus(), "未知"));

        // 查询校区名称
        if (floor.getCampusCode() != null) {
            Campus campus = campusMapper.selectByCampusCode(floor.getCampusCode());
            if (campus != null) {
                vo.setCampusName(campus.getCampusName());
            }
        }

        // 性别类型文本映射（使用字典）
        vo.setGenderTypeText(DictUtils.getLabel("dormitory_gender_type", floor.getGenderType(), "未知"));

        return vo;
    }

    /**
     * 检查楼层是否被房间关联
     * 
     * @param floorId 楼层ID
     * @return true-有房间关联，false-无房间关联
     */
    public boolean checkFloorHasRooms(Long floorId) {
        if (floorId == null) {
            return false;
        }
        LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Room::getFloorId, floorId);
        long roomCount = roomMapper.selectCount(roomWrapper);
        return roomCount > 0;
    }
}
