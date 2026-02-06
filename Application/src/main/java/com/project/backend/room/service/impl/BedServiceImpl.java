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
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.common.service.StudentInfoEnricher;
import com.project.backend.util.DictUtils;
import com.project.backend.room.dto.bed.BedBatchCreateDTO;
import com.project.backend.room.dto.bed.BedQueryDTO;
import com.project.backend.room.dto.bed.BedSaveDTO;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.entity.Room;
import com.project.backend.room.mapper.BedMapper;
import com.project.backend.room.mapper.FloorMapper;
import com.project.backend.room.mapper.RoomMapper;
import com.project.backend.room.service.BedService;
import com.project.backend.room.service.StatisticsService;
import com.project.backend.room.vo.BedVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 床位Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BedServiceImpl extends ServiceImpl<BedMapper, Bed> implements BedService {

    private final RoomMapper roomMapper;
    private final FloorMapper floorMapper;
    private final CampusMapper campusMapper;
    private final StudentMapper studentMapper;
    private final StudentInfoEnricher studentInfoEnricher;
    private final StatisticsService statisticsService;

    @Override
    @Transactional(readOnly = true)
    public PageResult<BedVO> pageList(BedQueryDTO queryDTO) {
        LambdaQueryWrapper<Bed> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getBedCode()), Bed::getBedCode, queryDTO.getBedCode())
               .like(StrUtil.isNotBlank(queryDTO.getBedNumber()), Bed::getBedNumber, queryDTO.getBedNumber())
               .eq(queryDTO.getRoomId() != null, Bed::getRoomId, queryDTO.getRoomId())
               .eq(StrUtil.isNotBlank(queryDTO.getRoomCode()), Bed::getRoomCode, queryDTO.getRoomCode())
               .eq(queryDTO.getFloorId() != null, Bed::getFloorId, queryDTO.getFloorId())
               .eq(StrUtil.isNotBlank(queryDTO.getFloorCode()), Bed::getFloorCode, queryDTO.getFloorCode())
               .eq(StrUtil.isNotBlank(queryDTO.getCampusCode()), Bed::getCampusCode, queryDTO.getCampusCode())
               .eq(StrUtil.isNotBlank(queryDTO.getBedPosition()), Bed::getBedPosition, queryDTO.getBedPosition())
               .eq(queryDTO.getBedStatus() != null, Bed::getBedStatus, queryDTO.getBedStatus())
               .eq(queryDTO.getStatus() != null, Bed::getStatus, queryDTO.getStatus())
               .orderByAsc(Bed::getSort)
               .orderByAsc(Bed::getId);

        Page<Bed> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<Bed> beds = page.getRecords();
        List<BedVO> voList = convertToVOList(beds);

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public BedVO getDetailById(Long id) {
        Bed bed = getById(id);
        if (bed == null) {
            throw new BusinessException("床位不存在");
        }
        // 使用批量转换方法，即使只有一个元素也能保持一致性
        List<BedVO> voList = convertToVOList(List.of(bed));
        return voList.isEmpty() ? null : voList.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBed(BedSaveDTO saveDTO) {
        // 检查编码是否重复
        LambdaQueryWrapper<Bed> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bed::getBedCode, saveDTO.getBedCode())
               .eq(Bed::getDeleted, 0);
        if (saveDTO.getId() != null) {
            wrapper.ne(Bed::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("床位编码已存在");
        }

        // 查询房间信息，填充冗余字段
        Room room = roomMapper.selectById(saveDTO.getRoomId());
        if (room == null) {
            throw new BusinessException("所属房间不存在");
        }

        Bed bed = new Bed();
        BeanUtil.copyProperties(saveDTO, bed);

        // 自动填充冗余字段
        bed.setRoomCode(room.getRoomCode());
        bed.setFloorId(room.getFloorId());
        bed.setFloorCode(room.getFloorCode());
        bed.setCampusCode(room.getCampusCode());

        if (saveDTO.getId() == null) {
            bed.setStatus(bed.getStatus() != null ? bed.getStatus() : 1);
            bed.setSort(bed.getSort() != null ? bed.getSort() : 0);
            bed.setBedStatus(bed.getBedStatus() != null ? bed.getBedStatus() : 1);

            boolean result = save(bed);

            // 更新房间和楼层统计字段
            if (result) {
                statisticsService.updateRoomStatistics(bed.getRoomId());
                if (bed.getFloorId() != null) {
                    statisticsService.updateFloorStatistics(bed.getFloorId());
                }
            }

            return result;
        } else {
            // 编辑时，如果房间ID发生变化，需要更新统计字段
            Bed oldBed = getById(bed.getId());
            boolean result = updateById(bed);
            if (result && oldBed != null) {
                // 如果房间ID变化，更新旧房间和新房间的统计字段
                if (!oldBed.getRoomId().equals(bed.getRoomId())) {
                    statisticsService.updateRoomStatistics(oldBed.getRoomId());
                    statisticsService.updateRoomStatistics(bed.getRoomId());
                    // 如果楼层ID也变化，更新楼层统计字段
                    if (oldBed.getFloorId() != null && bed.getFloorId() != null &&
                        !oldBed.getFloorId().equals(bed.getFloorId())) {
                        statisticsService.updateFloorStatistics(oldBed.getFloorId());
                        statisticsService.updateFloorStatistics(bed.getFloorId());
                    } else if (bed.getFloorId() != null) {
                        statisticsService.updateFloorStatistics(bed.getFloorId());
                    }
                } else {
                    // 房间ID未变化，只更新当前房间和楼层统计字段
                    statisticsService.updateRoomStatistics(bed.getRoomId());
                    if (bed.getFloorId() != null) {
                        statisticsService.updateFloorStatistics(bed.getFloorId());
                    }
                }
            }
            return result;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBed(Long id) {
        if (id == null) {
            throw new BusinessException("床位ID不能为空");
        }

        Bed bed = getById(id);
        if (bed == null) {
            throw new BusinessException("床位不存在");
        }

        boolean result = removeById(id);

        // 更新房间和楼层统计字段
        if (result) {
            if (bed.getRoomId() != null) {
                statisticsService.updateRoomStatistics(bed.getRoomId());
            }
            if (bed.getFloorId() != null) {
                statisticsService.updateFloorStatistics(bed.getFloorId());
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("床位ID不能为空");
        }
        return removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Bed bed = getById(id);
        if (bed == null) {
            throw new BusinessException("床位不存在");
        }

        // 如果要启用床位，需要检查所属房间是否启用
        if (status == 1 && bed.getRoomId() != null) {
            Room room = roomMapper.selectById(bed.getRoomId());
            if (room != null && room.getStatus() != null && room.getStatus() == 0) {
                throw new BusinessException("该房间处于停用状态，不允许启用床位");
            }
        }

        bed.setStatus(status);
        boolean result = updateById(bed);

        // 如果床位状态变更，更新统计字段（使用try-catch确保不影响主操作）
        if (result) {
            try {
                if (bed.getRoomId() != null) {
                    statisticsService.updateRoomStatistics(bed.getRoomId());
                }
                if (bed.getFloorId() != null) {
                    statisticsService.updateFloorStatistics(bed.getFloorId());
                }
            } catch (Exception e) {
                log.error("更新床位统计字段失败，床位ID：{}，房间ID：{}，楼层ID：{}",
                         id, bed.getRoomId(), bed.getFloorId(), e);
                // 不抛出异常，允许状态更新成功
            }
        }

        return result;
    }

    /**
     * 批量转换实体到VO（优化版本，避免N+1查询问题）
     */
    private List<BedVO> convertToVOList(List<Bed> beds) {
        if (beds == null || beds.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量加载所有关联的房间
        Set<Long> roomIds = beds.stream()
                .map(Bed::getRoomId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Room> roomMap = roomIds.isEmpty() ? Map.of() :
                roomMapper.selectBatchIds(roomIds).stream()
                        .collect(Collectors.toMap(Room::getId, r -> r));

        // 批量加载所有关联的楼层
        Set<Long> floorIds = beds.stream()
                .map(Bed::getFloorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        // 同时添加房间中的楼层ID
        roomMap.values().stream()
                .map(Room::getFloorId)
                .filter(id -> id != null)
                .forEach(floorIds::add);
        Map<Long, Floor> floorMap = floorIds.isEmpty() ? Map.of() :
                floorMapper.selectBatchIds(floorIds).stream()
                        .collect(Collectors.toMap(Floor::getId, f -> f));

        // 批量加载所有关联的校区
        Set<String> campusCodes = beds.stream()
                .map(Bed::getCampusCode)
                .filter(code -> StrUtil.isNotBlank(code))
                .collect(Collectors.toSet());
        Map<String, Campus> campusMap = campusCodes.isEmpty() ? Map.of() :
                campusCodes.stream()
                        .map(campusMapper::selectByCampusCode)
                        .filter(campus -> campus != null)
                        .collect(Collectors.toMap(Campus::getCampusCode, c -> c));

        // 批量加载所有关联的学生
        Set<Long> studentIds = beds.stream()
                .map(Bed::getStudentId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Student> studentMap = studentIds.isEmpty() ? Map.of() :
                studentMapper.selectBatchIds(studentIds).stream()
                        .collect(Collectors.toMap(Student::getId, s -> s));

        // 转换为VO列表
        return beds.stream()
                .map(bed -> convertToVO(bed, roomMap, floorMap, campusMap, studentMap))
                .collect(Collectors.toList());
    }

    /**
     * 实体转VO（优化版本，使用预加载的数据）
     */
    private BedVO convertToVO(Bed bed, Map<Long, Room> roomMap, Map<Long, Floor> floorMap,
                             Map<String, Campus> campusMap, Map<Long, Student> studentMap) {
        BedVO vo = new BedVO();
        BeanUtil.copyProperties(bed, vo);
        vo.setStatusText(DictUtils.getLabel("sys_common_status", bed.getStatus(), "未知"));
        vo.setBedPositionText(DictUtils.getLabel("dormitory_bed_position", bed.getBedPosition(), "未知"));
        vo.setBedStatusText(DictUtils.getLabel("dormitory_bed_status", bed.getBedStatus(), "未知"));

        // 从缓存获取房间信息
        Room room = null;
        if (bed.getRoomId() != null) {
            room = roomMap.get(bed.getRoomId());
            if (room != null) {
                vo.setRoomNumber(room.getRoomNumber());
                // 如果床位的floorId为空或不一致，从房间获取
                if (bed.getFloorId() == null || !bed.getFloorId().equals(room.getFloorId())) {
                    if (room.getFloorId() != null) {
                        vo.setFloorId(room.getFloorId());
                    }
                }
                // 如果床位的floorCode为空或不一致，从房间获取
                if (StrUtil.isBlank(bed.getFloorCode()) || !bed.getFloorCode().equals(room.getFloorCode())) {
                    if (StrUtil.isNotBlank(room.getFloorCode())) {
                        vo.setFloorCode(room.getFloorCode());
                    }
                }
            }
        }

        // 从缓存获取楼层信息
        Long floorIdToQuery = bed.getFloorId();
        if (floorIdToQuery == null && room != null && room.getFloorId() != null) {
            floorIdToQuery = room.getFloorId();
        }
        if (floorIdToQuery != null) {
            Floor floor = floorMap.get(floorIdToQuery);
            if (floor != null) {
                vo.setFloorName(floor.getFloorName());
                if (StrUtil.isBlank(vo.getFloorCode()) || !vo.getFloorCode().equals(floor.getFloorCode())) {
                    vo.setFloorCode(floor.getFloorCode());
                }
            }
        }

        // 从缓存获取校区信息
        if (StrUtil.isNotBlank(bed.getCampusCode())) {
            Campus campus = campusMap.get(bed.getCampusCode());
            if (campus != null) {
                vo.setCampusName(campus.getCampusName());
            }
        }

        // 从缓存获取学生信息
        if (bed.getStudentId() != null) {
            Student student = studentMap.get(bed.getStudentId());
            if (student != null) {
                studentInfoEnricher.enrichStudentInfo(student, vo);
            }
        }

        return vo;
    }

    /**
     * 实体转VO（保留原方法用于单个对象转换）
     */
    private BedVO convertToVO(Bed bed) {
        BedVO vo = new BedVO();
        BeanUtil.copyProperties(bed, vo);
        vo.setStatusText(DictUtils.getLabel("sys_common_status", bed.getStatus(), "未知"));
        vo.setBedPositionText(DictUtils.getLabel("dormitory_bed_position", bed.getBedPosition(), "未知"));
        vo.setBedStatusText(DictUtils.getLabel("dormitory_bed_status", bed.getBedStatus(), "未知"));

        // 查询房间信息填充房间号，并确保楼层信息正确
        Room room = null;
        if (bed.getRoomId() != null) {
            room = roomMapper.selectById(bed.getRoomId());
            if (room != null) {
                // 填充房间号
                vo.setRoomNumber(room.getRoomNumber());
                // 如果床位的floorId为空或不一致，从房间获取并更新
                if (bed.getFloorId() == null || !bed.getFloorId().equals(room.getFloorId())) {
                    if (room.getFloorId() != null) {
                        bed.setFloorId(room.getFloorId());
                        vo.setFloorId(room.getFloorId());
                    }
                }
                // 如果床位的floorCode为空或不一致，从房间获取并更新
                if (StrUtil.isBlank(bed.getFloorCode()) || !bed.getFloorCode().equals(room.getFloorCode())) {
                    if (StrUtil.isNotBlank(room.getFloorCode())) {
                        bed.setFloorCode(room.getFloorCode());
                        vo.setFloorCode(room.getFloorCode());
                    }
                }
            }
        }

        // 查询楼层信息填充楼层名称
        Long floorIdToQuery = bed.getFloorId();
        if (floorIdToQuery == null && room != null && room.getFloorId() != null) {
            floorIdToQuery = room.getFloorId();
        }
        if (floorIdToQuery != null) {
            Floor floor = floorMapper.selectById(floorIdToQuery);
            if (floor != null) {
                vo.setFloorName(floor.getFloorName());
                // 确保楼层编码正确
                if (StrUtil.isBlank(vo.getFloorCode()) || !vo.getFloorCode().equals(floor.getFloorCode())) {
                    vo.setFloorCode(floor.getFloorCode());
                }
            }
        }

        // 查询校区信息填充校区名称
        if (StrUtil.isNotBlank(bed.getCampusCode())) {
            Campus campus = campusMapper.selectByCampusCode(bed.getCampusCode());
            if (campus != null) {
                vo.setCampusName(campus.getCampusName());
            }
        }

        // 填充学生详细信息
        if (bed.getStudentId() != null) {
            Student student = studentMapper.selectById(bed.getStudentId());
            if (student != null) {
                studentInfoEnricher.enrichStudentInfo(student, vo);
            }
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchCreateBeds(BedBatchCreateDTO dto) {
        // 验证房间是否存在
        Room room = roomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException("所属房间不存在");
        }

        // 确保房间的关联字段不为空
        if (room.getFloorId() == null) {
            throw new BusinessException("房间的楼层信息不完整，无法创建床位");
        }
        if (StrUtil.isBlank(room.getRoomCode())) {
            throw new BusinessException("房间编码不能为空");
        }

        // 查询该房间已有床位的最大序号
        LambdaQueryWrapper<Bed> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bed::getRoomId, dto.getRoomId());
        List<Bed> existingBeds = list(wrapper);

        int maxSeq = 0;
        for (Bed existingBed : existingBeds) {
            String bedCode = existingBed.getBedCode();
            if (bedCode != null) {
                try {
                    // 新格式：{房间编号}-{床位序号}，如 F1-0101-1
                    if (bedCode.contains("-")) {
                        String lastPart = bedCode.substring(bedCode.lastIndexOf("-") + 1);
                        int seq = Integer.parseInt(lastPart);
                        maxSeq = Math.max(maxSeq, seq);
                    } else {
                        // 兼容旧格式：纯数字
                        int seq = Integer.parseInt(bedCode);
                        maxSeq = Math.max(maxSeq, seq);
                    }
                } catch (NumberFormatException e) {
                    // 忽略解析失败的编码
                }
            }
        }

        // 获取当前最大排序号
        LambdaQueryWrapper<Bed> sortWrapper = new LambdaQueryWrapper<>();
        sortWrapper.eq(Bed::getRoomId, dto.getRoomId())
                   .orderByDesc(Bed::getSort)
                   .last("LIMIT 1");
        Bed lastBed = getOne(sortWrapper);
        int currentSort = lastBed != null && lastBed.getSort() != null ? lastBed.getSort() : 0;

        // 批量生成床位
        List<Bed> bedsToCreate = new ArrayList<>();
        for (int i = 0; i < dto.getGenerateCount(); i++) {
            int seq = maxSeq + i + 1;
            // 生成床位编码：{房间编号}-{床位序号}，如 F1-0101-1
            String bedCode = String.format("%s-%d", room.getRoomCode(), seq);
            // 床位号为纯数字
            String bedNumber = String.valueOf(seq);

            // 检查编码是否已存在（在同一房间内）
            LambdaQueryWrapper<Bed> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(Bed::getRoomId, dto.getRoomId())
                       .eq(Bed::getBedCode, bedCode)
                       .eq(Bed::getDeleted, 0);
            if (count(checkWrapper) > 0) {
                throw new BusinessException("床位编码 " + bedCode + " 已存在");
            }

            Bed bed = new Bed();
            bed.setBedCode(bedCode);
            bed.setBedNumber(bedNumber);
            bed.setRoomId(dto.getRoomId());
            bed.setRoomCode(room.getRoomCode());
            bed.setFloorId(room.getFloorId());
            bed.setFloorCode(room.getFloorCode());
            bed.setCampusCode(room.getCampusCode());
            bed.setBedPosition(dto.getBedPosition());
            bed.setBedStatus(dto.getBedStatus() != null ? dto.getBedStatus() : 1);
            bed.setCheckInDate(dto.getCheckInDate());
            bed.setCheckOutDate(dto.getCheckOutDate());
            bed.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
            bed.setRemark(dto.getRemark());
            bed.setSort(++currentSort);

            bedsToCreate.add(bed);
        }

        // 批量插入
        boolean result = saveBatch(bedsToCreate);

        // 更新房间和楼层统计字段
        if (result) {
            statisticsService.updateRoomStatistics(dto.getRoomId());
            if (room.getFloorId() != null) {
                statisticsService.updateFloorStatistics(room.getFloorId());
            }
        }

        return bedsToCreate.size();
    }
}
