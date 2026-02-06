package com.project.backend.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.mapper.CampusMapper;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.entity.Room;
import com.project.backend.room.mapper.BedMapper;
import com.project.backend.room.mapper.FloorMapper;
import com.project.backend.room.mapper.RoomMapper;
import com.project.backend.room.service.DormHierarchyService;
import com.project.backend.room.vo.DormHierarchyNodeVO;
import com.project.backend.room.vo.DormHierarchyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 宿舍层级服务实现
 * 
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormHierarchyServiceImpl implements DormHierarchyService {

    private final CampusMapper campusMapper;
    private final FloorMapper floorMapper;
    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;

    @Override
    public DormHierarchyVO getFullHierarchy() {
        // 1. 查询所有校区（只查询启用的）
        LambdaQueryWrapper<Campus> campusWrapper = new LambdaQueryWrapper<>();
        campusWrapper.eq(Campus::getStatus, 1);
        campusWrapper.orderByAsc(Campus::getSort);
        campusWrapper.orderByAsc(Campus::getId);
        List<Campus> allCampuses = campusMapper.selectList(campusWrapper);

        // 2. 查询所有楼层（只查询启用的）
        LambdaQueryWrapper<Floor> floorWrapper = new LambdaQueryWrapper<>();
        floorWrapper.eq(Floor::getStatus, 1);
        floorWrapper.orderByAsc(Floor::getSort);
        floorWrapper.orderByAsc(Floor::getId);
        List<Floor> allFloors = floorMapper.selectList(floorWrapper);

        // 3. 查询所有房间（只查询启用的）
        LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Room::getStatus, 1);
        roomWrapper.orderByAsc(Room::getSort);
        roomWrapper.orderByAsc(Room::getId);
        List<Room> allRooms = roomMapper.selectList(roomWrapper);

        // 4. 查询所有床位（只查询启用的）
        LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
        bedWrapper.eq(Bed::getStatus, 1);
        bedWrapper.orderByAsc(Bed::getSort);
        bedWrapper.orderByAsc(Bed::getId);
        List<Bed> allBeds = bedMapper.selectList(bedWrapper);

        // 5. 按关联字段分组
        Map<String, List<Floor>> floorMapByCampus = allFloors.stream()
                .filter(floor -> floor.getCampusCode() != null && !floor.getCampusCode().isEmpty())
                .collect(Collectors.groupingBy(Floor::getCampusCode));

        Map<Long, List<Room>> roomMapByFloor = allRooms.stream()
                .filter(room -> room.getFloorId() != null)
                .collect(Collectors.groupingBy(Room::getFloorId));

        Map<Long, List<Bed>> bedMapByRoom = allBeds.stream()
                .filter(bed -> bed.getRoomId() != null)
                .collect(Collectors.groupingBy(Bed::getRoomId));

        // 6. 构建校区列表
        List<DormHierarchyNodeVO> campusNodes = allCampuses.stream()
                .map(campus -> buildCampusNode(campus, floorMapByCampus, roomMapByFloor, bedMapByRoom))
                .collect(Collectors.toList());

        DormHierarchyVO result = new DormHierarchyVO();
        result.setCampuses(campusNodes);
        return result;
    }

    /**
     * 构建校区节点
     */
    private DormHierarchyNodeVO buildCampusNode(
            Campus campus,
            Map<String, List<Floor>> floorMapByCampus,
            Map<Long, List<Room>> roomMapByFloor,
            Map<Long, List<Bed>> bedMapByRoom) {
        DormHierarchyNodeVO node = new DormHierarchyNodeVO();
        node.setId(campus.getId());
        node.setCode(campus.getCampusCode());
        node.setName(campus.getCampusName());
        node.setType("campus");
        node.setStatus(campus.getStatus());

        // 构建楼层节点
        String campusCode = campus.getCampusCode();
        List<Floor> floors = (campusCode != null && !campusCode.isEmpty())
                ? floorMapByCampus.getOrDefault(campusCode, new ArrayList<>())
                : new ArrayList<>();
        List<DormHierarchyNodeVO> floorNodes = floors.stream()
                .map(floor -> buildFloorNode(floor, roomMapByFloor, bedMapByRoom))
                .collect(Collectors.toList());

        if (!floorNodes.isEmpty()) {
            node.setChildren(floorNodes);
        }

        return node;
    }

    /**
     * 构建楼层节点
     */
    private DormHierarchyNodeVO buildFloorNode(
            Floor floor,
            Map<Long, List<Room>> roomMapByFloor,
            Map<Long, List<Bed>> bedMapByRoom) {
        DormHierarchyNodeVO node = new DormHierarchyNodeVO();
        node.setId(floor.getId());
        node.setCode(floor.getFloorCode());
        node.setName(floor.getFloorName());
        node.setType("floor");
        node.setParentCode(floor.getCampusCode());
        node.setStatus(floor.getStatus());

        // 构建房间节点
        Long floorId = floor.getId();
        List<Room> rooms = (floorId != null)
                ? roomMapByFloor.getOrDefault(floorId, new ArrayList<>())
                : new ArrayList<>();
        List<DormHierarchyNodeVO> roomNodes = rooms.stream()
                .map(room -> buildRoomNode(room, bedMapByRoom))
                .collect(Collectors.toList());

        if (!roomNodes.isEmpty()) {
            node.setChildren(roomNodes);
        }

        return node;
    }

    /**
     * 构建房间节点
     */
    private DormHierarchyNodeVO buildRoomNode(
            Room room,
            Map<Long, List<Bed>> bedMapByRoom) {
        DormHierarchyNodeVO node = new DormHierarchyNodeVO();
        node.setId(room.getId());
        node.setCode(room.getRoomCode());
        node.setName(room.getRoomNumber());
        node.setType("room");
        node.setParentCode(String.valueOf(room.getFloorId()));
        node.setStatus(room.getStatus());

        // 构建床位节点
        Long roomId = room.getId();
        List<Bed> beds = (roomId != null)
                ? bedMapByRoom.getOrDefault(roomId, new ArrayList<>())
                : new ArrayList<>();
        List<DormHierarchyNodeVO> bedNodes = beds.stream()
                .map(this::buildBedNode)
                .collect(Collectors.toList());

        if (!bedNodes.isEmpty()) {
            node.setChildren(bedNodes);
        }

        return node;
    }

    /**
     * 构建床位节点
     */
    private DormHierarchyNodeVO buildBedNode(Bed bed) {
        DormHierarchyNodeVO node = new DormHierarchyNodeVO();
        node.setId(bed.getId());
        node.setCode(bed.getBedCode());
        node.setName(bed.getBedNumber());
        node.setType("bed");
        node.setParentCode(String.valueOf(bed.getRoomId()));
        node.setStatus(bed.getStatus());
        node.setBedStatus(bed.getBedStatus());
        // 床位是叶子节点，没有children
        return node;
    }
}
