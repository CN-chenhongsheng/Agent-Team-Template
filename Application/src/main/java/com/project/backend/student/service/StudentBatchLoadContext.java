package com.project.backend.student.service;

import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.entity.Class;
import com.project.backend.organization.entity.Department;
import com.project.backend.organization.entity.Major;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.entity.Room;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class StudentBatchLoadContext {
    private Map<String, Campus> campusByCode = new HashMap<>();
    private Map<String, Department> deptByCode = new HashMap<>();
    private Map<String, Major> majorByCode = new HashMap<>();
    private Map<Long, Class> classById = new HashMap<>();
    private Map<Long, Floor> floorById = new HashMap<>();
    private Map<String, Floor> floorByCode = new HashMap<>();
    private Map<Long, Room> roomById = new HashMap<>();
    private Map<String, Room> roomByCode = new HashMap<>();
    private Map<Long, Bed> bedById = new HashMap<>();
    private Map<String, Bed> bedByCode = new HashMap<>();
}
