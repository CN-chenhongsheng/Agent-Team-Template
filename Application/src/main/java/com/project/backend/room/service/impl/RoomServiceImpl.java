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
import com.project.backend.room.dto.room.RoomBatchCreateDTO;
import com.project.backend.room.dto.room.RoomQueryDTO;
import com.project.backend.room.dto.room.RoomSaveDTO;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.entity.Room;
import com.project.backend.room.mapper.BedMapper;
import com.project.backend.room.mapper.FloorMapper;
import com.project.backend.room.mapper.RoomMapper;
import com.project.backend.room.service.BedService;
import com.project.backend.room.service.RoomService;
import com.project.backend.room.service.StatisticsService;
import com.project.backend.room.vo.BedVO;
import com.project.backend.room.vo.RoomVO;
import com.project.backend.room.vo.RoomVisualVO;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.common.service.StudentInfoEnricher;
import com.project.backend.util.DictUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 房间Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    private final FloorMapper floorMapper;
    private final BedMapper bedMapper;
    private final CampusMapper campusMapper;
    private final StatisticsService statisticsService;
    private final BedService bedService;
    private final StudentMapper studentMapper;
    private final StudentInfoEnricher studentInfoEnricher;

    @Override
    @Transactional(readOnly = true)
    public PageResult<RoomVO> pageList(RoomQueryDTO queryDTO) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getRoomCode()), Room::getRoomCode, queryDTO.getRoomCode())
               .like(StrUtil.isNotBlank(queryDTO.getRoomNumber()), Room::getRoomNumber, queryDTO.getRoomNumber())
               .eq(queryDTO.getFloorId() != null, Room::getFloorId, queryDTO.getFloorId())
               .eq(StrUtil.isNotBlank(queryDTO.getFloorCode()), Room::getFloorCode, queryDTO.getFloorCode())
               .eq(StrUtil.isNotBlank(queryDTO.getCampusCode()), Room::getCampusCode, queryDTO.getCampusCode())
               .eq(StrUtil.isNotBlank(queryDTO.getRoomType()), Room::getRoomType, queryDTO.getRoomType())
               .eq(queryDTO.getRoomStatus() != null, Room::getRoomStatus, queryDTO.getRoomStatus())
               .eq(queryDTO.getStatus() != null, Room::getStatus, queryDTO.getStatus())
               .orderByAsc(Room::getSort)
               .orderByAsc(Room::getId);

        Page<Room> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<Room> rooms = page.getRecords();
        List<RoomVO> voList = convertToVOList(rooms);

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomVO getDetailById(Long id) {
        Room room = getById(id);
        if (room == null) {
            throw new BusinessException("房间不存在");
        }
        // 使用批量转换方法，即使只有一个元素也能保持一致性
        List<RoomVO> voList = convertToVOList(List.of(room));
        return voList.isEmpty() ? null : voList.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRoom(RoomSaveDTO saveDTO) {
        // 检查编码是否重复
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getRoomCode, saveDTO.getRoomCode())
               .eq(Room::getDeleted, 0);
        if (saveDTO.getId() != null) {
            wrapper.ne(Room::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("房间编码已存在");
        }

        // 查询楼层信息，填充冗余字段
        Floor floor = floorMapper.selectById(saveDTO.getFloorId());
        if (floor == null) {
            throw new BusinessException("所属楼层不存在");
        }

        Room room = new Room();
        BeanUtil.copyProperties(saveDTO, room);

        // 自动填充冗余字段
        room.setFloorCode(floor.getFloorCode());
        room.setCampusCode(floor.getCampusCode());
        // floorNumber 从saveDTO 中获取，如果为空则从楼层信息中获取
        if (room.getFloorNumber() == null) {
            room.setFloorNumber(floor.getFloorNumber());
        }

        if (saveDTO.getId() == null) {
            room.setStatus(room.getStatus() != null ? room.getStatus() : 1);
            room.setSort(room.getSort() != null ? room.getSort() : 0);
            room.setBedCount(room.getBedCount() != null ? room.getBedCount() : 4);
            room.setCurrentOccupancy(0);
            room.setHasAirConditioner(room.getHasAirConditioner() != null ? room.getHasAirConditioner() : 0);
            room.setHasBathroom(room.getHasBathroom() != null ? room.getHasBathroom() : 0);
            room.setHasBalcony(room.getHasBalcony() != null ? room.getHasBalcony() : 0);
            room.setRoomStatus(room.getRoomStatus() != null ? room.getRoomStatus() : 1);

            boolean result = save(room);

            // 更新楼层统计字段
            statisticsService.updateFloorStatistics(room.getFloorId());

            return result;
        } else {
            // 编辑时，如果楼层ID发生变化，需要更新统计字段
            Room oldRoom = getById(room.getId());
            boolean result = updateById(room);
            if (result && oldRoom != null && !oldRoom.getFloorId().equals(room.getFloorId())) {
                // 更新旧楼层的统计字段
                statisticsService.updateFloorStatistics(oldRoom.getFloorId());
                // 更新新楼层的统计字段
                statisticsService.updateFloorStatistics(room.getFloorId());
            } else if (result) {
                // 楼层ID未变化，只更新当前楼层统计字段
                statisticsService.updateFloorStatistics(room.getFloorId());
            }
            return result;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoom(Long id) {
        if (id == null) {
            throw new BusinessException("房间ID不能为空");
        }

        Room room = getById(id);
        if (room == null) {
            throw new BusinessException("房间不存在");
        }

        // ========== 级联删除床位 ==========
        // 查询所有属于该房间的床位
        LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
        bedWrapper.eq(Bed::getRoomId, id);
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

        // 删除当前房间
        boolean result = removeById(id);

        // 更新楼层统计字段
        if (result && room.getFloorId() != null) {
            statisticsService.updateFloorStatistics(room.getFloorId());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("房间ID不能为空");
        }

        // 批量删除时，对每个ID调用单个删除方法，确保级联删除逻辑被执行
        for (Long id : ids) {
            deleteRoom(id);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Room room = getById(id);
        if (room == null) {
            throw new BusinessException("房间不存在");
        }

        // 如果要启用房间，需要检查所属楼层是否启用
        if (status == 1 && room.getFloorId() != null) {
            Floor floor = floorMapper.selectById(room.getFloorId());
            if (floor != null && floor.getStatus() != null && floor.getStatus() == 0) {
                throw new BusinessException("该楼层处于停用状态，不允许启用房间");
            }
        }

        room.setStatus(status);
        boolean result = updateById(room);

        // 如果状态改为停用（0），则级联更新该房间下所有床位状态为停用
        if (status == 0) {
            LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
            bedWrapper.eq(Bed::getRoomId, id);
            Bed bedUpdate = new Bed();
            bedUpdate.setStatus(0);
            bedMapper.update(bedUpdate, bedWrapper);
        }

        return result;
    }

    /**
     * 批量转换实体到VO（优化版本，避免N+1查询问题）
     */
    private List<RoomVO> convertToVOList(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量加载所有关联的楼层
        Set<Long> floorIds = rooms.stream()
                .map(Room::getFloorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Floor> floorMap = floorIds.isEmpty() ? Map.of() :
                floorMapper.selectBatchIds(floorIds).stream()
                        .collect(Collectors.toMap(Floor::getId, f -> f));

        // 批量加载所有关联的校区
        Set<String> campusCodes = rooms.stream()
                .map(Room::getCampusCode)
                .filter(code -> StrUtil.isNotBlank(code))
                .collect(Collectors.toSet());
        Map<String, Campus> campusMap = campusCodes.isEmpty() ? Map.of() :
                campusCodes.stream()
                        .map(campusMapper::selectByCampusCode)
                        .filter(campus -> campus != null)
                        .collect(Collectors.toMap(Campus::getCampusCode, c -> c));

        // 批量统计每个房间的床位数
        Set<Long> roomIds = rooms.stream()
                .map(Room::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Long> bedCountMap = new HashMap<>();
        if (!roomIds.isEmpty()) {
            // 查询所有相关床位
            LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
            bedWrapper.in(Bed::getRoomId, roomIds);
            List<Bed> beds = bedMapper.selectList(bedWrapper);

            // 按房间ID分组统计
            bedCountMap = beds.stream()
                    .collect(Collectors.groupingBy(Bed::getRoomId, Collectors.counting()));
        }

        // 转换为VO列表
        Map<Long, Long> finalBedCountMap = bedCountMap;
        return rooms.stream()
                .map(room -> convertToVO(room, floorMap, campusMap, finalBedCountMap))
                .collect(Collectors.toList());
    }

    /**
     * 实体转VO（优化版本，使用预加载的数据）
     */
    private RoomVO convertToVO(Room room, Map<Long, Floor> floorMap,
                              Map<String, Campus> campusMap, Map<Long, Long> bedCountMap) {
        RoomVO vo = new RoomVO();
        BeanUtil.copyProperties(room, vo);
        vo.setStatusText(DictUtils.getLabel("sys_common_status", room.getStatus(), "未知"));
        vo.setRoomTypeText(DictUtils.getLabel("dormitory_room_type", room.getRoomType(), "未知"));
        vo.setRoomStatusText(DictUtils.getLabel("dormitory_room_status", room.getRoomStatus(), "未知"));

        // 从缓存获取楼层信息
        if (room.getFloorId() != null) {
            Floor floor = floorMap.get(room.getFloorId());
            if (floor != null) {
                vo.setFloorName(floor.getFloorName());
            }
        }

        // 从缓存获取校区信息
        if (StrUtil.isNotBlank(room.getCampusCode())) {
            Campus campus = campusMap.get(room.getCampusCode());
            if (campus != null) {
                vo.setCampusName(campus.getCampusName());
            }
        }

        // 从预先统计的数据获取床位数
        Long bedCount = bedCountMap.getOrDefault(room.getId(), 0L);
        vo.setTotalBeds(bedCount.intValue());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchCreateRooms(RoomBatchCreateDTO dto) {
        // 验证楼层是否存在
        Floor floor = floorMapper.selectById(dto.getFloorId());
        if (floor == null) {
            throw new BusinessException("所属楼层不存在");
        }

        // 验证楼层数是否超出楼层的最大层数
        int maxFloorNumber = floor.getFloorNumber() != null ? floor.getFloorNumber() : 1;
        for (Integer floorNum : dto.getFloorNumbers()) {
            if (floorNum > maxFloorNumber) {
                throw new BusinessException("楼层" + floorNum + " 超出该楼最大层数" + maxFloorNumber);
            }
        }

        // 查询该楼层每个楼层数已有房间的最大序号
        Map<Integer, Integer> maxSeqMap = new HashMap<>();
        for (Integer floorNum : dto.getFloorNumbers()) {
            // 查询该楼层数下所有房间编码，找出最大序号
            LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Room::getFloorId, dto.getFloorId())
                   .eq(Room::getFloorNumber, floorNum);
            List<Room> existingRooms = list(wrapper);

            int maxSeq = 0;
            for (Room existingRoom : existingRooms) {
                String roomCode = existingRoom.getRoomCode();
                if (roomCode != null) {
                    try {
                        // 新格式：{楼栋编码}-{楼层(2位)}{序号(2位)}，如 F1-0101
                        if (roomCode.contains("-")) {
                            String numPart = roomCode.substring(roomCode.lastIndexOf("-") + 1);
                            if (numPart.length() >= 4) {
                                // 后两位为序号
                                int seq = Integer.parseInt(numPart.substring(numPart.length() - 2));
                                maxSeq = Math.max(maxSeq, seq);
                            }
                        } else if (roomCode.length() >= 3) {
                            // 兼容旧格式：楼层数 * 100 + 序号，如 101
                            int seq = Integer.parseInt(roomCode.substring(roomCode.length() - 2));
                            maxSeq = Math.max(maxSeq, seq);
                        }
                    } catch (NumberFormatException e) {
                        // 忽略解析失败的编码
                    }
                }
            }
            maxSeqMap.put(floorNum, maxSeq);
        }

        // 获取当前最大排序号
        LambdaQueryWrapper<Room> sortWrapper = new LambdaQueryWrapper<>();
        sortWrapper.eq(Room::getFloorId, dto.getFloorId())
                   .orderByDesc(Room::getSort)
                   .last("LIMIT 1");
        Room lastRoom = getOne(sortWrapper);
        int currentSort = lastRoom != null && lastRoom.getSort() != null ? lastRoom.getSort() : 0;

        // 批量生成房间
        List<Room> roomsToCreate = new ArrayList<>();
        List<Integer> sortedFloorNumbers = new ArrayList<>(dto.getFloorNumbers());
        Collections.sort(sortedFloorNumbers);

        for (Integer floorNum : sortedFloorNumbers) {
            int startSeq = maxSeqMap.get(floorNum) + 1;

            for (int i = 0; i < dto.getGenerateCount(); i++) {
                int seq = startSeq + i;
                // 生成房间编码：{楼栋编码}-{楼层(2位)}{序号(2位)}，如 F1-0101
                String roomCode = String.format("%s-%02d%02d", floor.getFloorCode(), floorNum, seq);
                // 房间号为纯数字版本：{楼层(2位)}{序号(2位)}
                String roomNumber = String.format("%02d%02d", floorNum, seq);

                // 检查编码是否已存在
                LambdaQueryWrapper<Room> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(Room::getRoomCode, roomCode)
                           .eq(Room::getDeleted, 0);
                if (count(checkWrapper) > 0) {
                    throw new BusinessException("房间编码 " + roomCode + " 已存在");
                }

                Room room = new Room();
                room.setRoomCode(roomCode);
                room.setRoomNumber(roomNumber);
                room.setFloorId(dto.getFloorId());
                room.setFloorCode(floor.getFloorCode());
                room.setCampusCode(floor.getCampusCode());
                room.setFloorNumber(floorNum);
                room.setRoomType(dto.getRoomType());
                room.setRoomStatus(dto.getRoomStatus() != null ? dto.getRoomStatus() : 1);
                room.setBedCount(dto.getBedCount() != null ? dto.getBedCount() : 4);
                room.setArea(dto.getArea());
                room.setMaxOccupancy(dto.getMaxOccupancy());
                room.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
                room.setHasAirConditioner(dto.getHasAirConditioner() != null ? dto.getHasAirConditioner() : 0);
                room.setHasBathroom(dto.getHasBathroom() != null ? dto.getHasBathroom() : 0);
                room.setHasBalcony(dto.getHasBalcony() != null ? dto.getHasBalcony() : 0);
                room.setRemark(dto.getRemark());
                room.setCurrentOccupancy(0);
                room.setSort(++currentSort);

                roomsToCreate.add(room);
            }
        }

        // 批量插入
        boolean result = saveBatch(roomsToCreate);

        // 更新楼层统计字段
        if (result) {
            statisticsService.updateFloorStatistics(dto.getFloorId());
        }

        return roomsToCreate.size();
    }

    /**
     * 检查房间是否被床位关联
     *
     * @param roomId 房间ID
     * @return true-有床位关联，false-无床位关联
     */
    @Override
    @Transactional(readOnly = true)
    public boolean checkRoomHasBeds(Long roomId) {
        if (roomId == null) {
            return false;
        }
        LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
        bedWrapper.eq(Bed::getRoomId, roomId);
        long bedCount = bedMapper.selectCount(bedWrapper);
        return bedCount > 0;
    }

    /**
     * 获取房间可视化列表（含床位和学生信息）
     * 用于可视化平面图展示
     *
     * @param floorId 楼层ID
     * @return 房间列表（含床位信息）
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoomVisualVO> getVisualRoomList(Long floorId) {
        if (floorId == null) {
            return new ArrayList<>();
        }

        // 查询该楼层下所有启用状态的房间
        LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Room::getFloorId, floorId)
                   .eq(Room::getStatus, 1)  // 只查询启用状态的房间
                   .orderByAsc(Room::getFloorNumber)
                   .orderByAsc(Room::getSort)
                   .orderByAsc(Room::getRoomNumber);
        List<Room> rooms = list(roomWrapper);

        if (rooms.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有房间ID
        Set<Long> roomIds = rooms.stream()
                .map(Room::getId)
                .collect(Collectors.toSet());

        // 批量查询所有床位（一次查询所有房间的床位，消除 N+1）
        LambdaQueryWrapper<Bed> allBedsWrapper = new LambdaQueryWrapper<>();
        allBedsWrapper.in(Bed::getRoomId, roomIds).orderByAsc(Bed::getSort).orderByAsc(Bed::getId);
        List<Bed> allBeds = bedMapper.selectList(allBedsWrapper);

        // 批量加载床位关联的房间信息（用于补全 roomNumber、floorId、floorCode）
        Map<Long, Room> bedRoomMap = new HashMap<>();
        for (Room r : rooms) {
            bedRoomMap.put(r.getId(), r);
        }

        // 批量加载床位关联的楼层信息
        Set<Long> bedFloorIds = allBeds.stream()
                .map(Bed::getFloorId).filter(id -> id != null).collect(Collectors.toSet());
        // 也收集来自房间的 floorId（兜底）
        rooms.stream().map(Room::getFloorId).filter(id -> id != null).forEach(bedFloorIds::add);
        Map<Long, Floor> bedFloorMap = bedFloorIds.isEmpty() ? Map.of()
                : floorMapper.selectBatchIds(bedFloorIds).stream()
                        .collect(Collectors.toMap(Floor::getId, f -> f, (a, b) -> a));

        // 批量加载床位关联的校区信息
        Set<String> bedCampusCodes = allBeds.stream()
                .map(Bed::getCampusCode).filter(code -> StrUtil.isNotBlank(code)).collect(Collectors.toSet());
        Map<String, Campus> bedCampusMap = bedCampusCodes.isEmpty() ? Map.of()
                : bedCampusCodes.stream()
                        .map(campusMapper::selectByCampusCode)
                        .filter(campus -> campus != null)
                        .collect(Collectors.toMap(Campus::getCampusCode, c -> c, (a, b) -> a));

        // 批量加载床位关联的学生信息
        Set<Long> bedStudentIds = allBeds.stream()
                .map(Bed::getStudentId).filter(id -> id != null).collect(Collectors.toSet());
        Map<Long, Student> bedStudentMap = bedStudentIds.isEmpty() ? Map.of()
                : studentMapper.selectBatchIds(bedStudentIds).stream()
                        .collect(Collectors.toMap(Student::getId, s -> s, (a, b) -> a));

        // 将床位按房间ID分组，完整转换为 BedVO（含学生信息、楼层、校区等）
        Map<Long, List<BedVO>> bedsByRoomId = new HashMap<>();
        for (Bed bed : allBeds) {
            BedVO bedVO = new BedVO();
            BeanUtil.copyProperties(bed, bedVO);
            bedVO.setStatusText(DictUtils.getLabel("sys_common_status", bed.getStatus(), "未知"));
            bedVO.setBedPositionText(DictUtils.getLabel("dormitory_bed_position", bed.getBedPosition(), "未知"));
            bedVO.setBedStatusText(DictUtils.getLabel("dormitory_bed_status", bed.getBedStatus(), "未知"));

            // 补全房间信息
            Room bedRoom = bedRoomMap.get(bed.getRoomId());
            if (bedRoom != null) {
                bedVO.setRoomNumber(bedRoom.getRoomNumber());
                if (bed.getFloorId() == null && bedRoom.getFloorId() != null) {
                    bedVO.setFloorId(bedRoom.getFloorId());
                }
                if (StrUtil.isBlank(bed.getFloorCode()) && StrUtil.isNotBlank(bedRoom.getFloorCode())) {
                    bedVO.setFloorCode(bedRoom.getFloorCode());
                }
            }

            // 补全楼层信息
            Long floorIdForBed = bedVO.getFloorId() != null ? bedVO.getFloorId()
                    : (bedRoom != null ? bedRoom.getFloorId() : null);
            if (floorIdForBed != null) {
                Floor bedFloor = bedFloorMap.get(floorIdForBed);
                if (bedFloor != null) {
                    bedVO.setFloorName(bedFloor.getFloorName());
                    if (StrUtil.isBlank(bedVO.getFloorCode())) {
                        bedVO.setFloorCode(bedFloor.getFloorCode());
                    }
                }
            }

            // 补全校区信息
            if (StrUtil.isNotBlank(bed.getCampusCode())) {
                Campus bedCampus = bedCampusMap.get(bed.getCampusCode());
                if (bedCampus != null) {
                    bedVO.setCampusName(bedCampus.getCampusName());
                }
            }

            // 填充学生详细信息（关键：含 studentInfo 中的姓名、学号等，供可视化视图展示入住人）
            if (bed.getStudentId() != null) {
                Student student = bedStudentMap.get(bed.getStudentId());
                if (student != null) {
                    studentInfoEnricher.enrichStudentInfo(student, bedVO);
                }
            }

            bedsByRoomId.computeIfAbsent(bed.getRoomId(), k -> new ArrayList<>()).add(bedVO);
        }

        // 批量加载楼层信息
        Set<Long> floorIds = rooms.stream()
                .map(Room::getFloorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Floor> floorMap = floorIds.isEmpty() ? Map.of() :
                floorMapper.selectBatchIds(floorIds).stream()
                        .collect(Collectors.toMap(Floor::getId, f -> f));

        // 批量加载校区信息
        Set<String> campusCodes = rooms.stream()
                .map(Room::getCampusCode)
                .filter(code -> StrUtil.isNotBlank(code))
                .collect(Collectors.toSet());
        Map<String, Campus> campusMap = campusCodes.isEmpty() ? Map.of() :
                campusCodes.stream()
                        .map(campusMapper::selectByCampusCode)
                        .filter(campus -> campus != null)
                        .collect(Collectors.toMap(Campus::getCampusCode, c -> c));

        // 转换为 RoomVisualVO
        return rooms.stream()
                .map(room -> convertToVisualVO(room, floorMap, campusMap, bedsByRoomId))
                .collect(Collectors.toList());
    }

    /**
     * 转换为可视化VO
     */
    private RoomVisualVO convertToVisualVO(Room room, Map<Long, Floor> floorMap,
                                          Map<String, Campus> campusMap,
                                          Map<Long, List<BedVO>> bedsByRoomId) {
        RoomVisualVO vo = new RoomVisualVO();
        BeanUtil.copyProperties(room, vo);
        vo.setStatusText(DictUtils.getLabel("sys_common_status", room.getStatus(), "未知"));
        vo.setRoomTypeText(DictUtils.getLabel("dormitory_room_type", room.getRoomType(), "未知"));
        vo.setRoomStatusText(DictUtils.getLabel("dormitory_room_status", room.getRoomStatus(), "未知"));

        // 从缓存获取楼层信息
        if (room.getFloorId() != null) {
            Floor floor = floorMap.get(room.getFloorId());
            if (floor != null) {
                vo.setFloorName(floor.getFloorName());
                // 设置楼层的性别类型
                vo.setGenderType(floor.getGenderType());
                vo.setGenderTypeText(DictUtils.getLabel("dormitory_gender_type", floor.getGenderType(), "未知"));
            }
        }

        // 从缓存获取校区信息
        if (StrUtil.isNotBlank(room.getCampusCode())) {
            Campus campus = campusMap.get(room.getCampusCode());
            if (campus != null) {
                vo.setCampusName(campus.getCampusName());
            }
        }

        // 设置床位列表
        List<BedVO> beds = bedsByRoomId.getOrDefault(room.getId(), new ArrayList<>());
        vo.setBeds(beds);
        vo.setTotalBeds(beds.size());

        // 动态计算当前入住人数（床位中有学生的数量）
        // 数据库中的 currentOccupancy 字段未被同步更新，所以这里根据床位数据实时计算
        int calculatedOccupancy = (int) beds.stream().filter(bed -> bed.getStudentId() != null).count();
        vo.setCurrentOccupancy(calculatedOccupancy);

        return vo;
    }
}
