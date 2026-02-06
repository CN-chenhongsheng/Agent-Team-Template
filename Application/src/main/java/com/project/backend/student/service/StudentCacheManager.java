package com.project.backend.student.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.entity.Class;
import com.project.backend.organization.entity.Department;
import com.project.backend.organization.entity.Major;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.entity.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentCacheManager {

    private final Cache<String, Campus> campusCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    private final Cache<String, Department> deptCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    private final Cache<String, Major> majorCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(5000)
            .build();

    private final Cache<Long, Class> classCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(5000)
            .build();

    private final Cache<Long, Floor> floorCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(5000)
            .build();

    private final Cache<Long, Room> roomCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    private final Cache<Long, Bed> bedCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(50000)
            .build();

    public void putCampus(String key, Campus value) {
        campusCache.put(key, value);
    }

    public Campus getCampus(String key) {
        return campusCache.getIfPresent(key);
    }

    public void putDept(String key, Department value) {
        deptCache.put(key, value);
    }

    public Department getDept(String key) {
        return deptCache.getIfPresent(key);
    }

    public void putMajor(String key, Major value) {
        majorCache.put(key, value);
    }

    public Major getMajor(String key) {
        return majorCache.getIfPresent(key);
    }

    public void putClass(Long key, Class value) {
        classCache.put(key, value);
    }

    public Class getClass(Long key) {
        return classCache.getIfPresent(key);
    }

    public void putFloor(Long key, Floor value) {
        floorCache.put(key, value);
    }

    public Floor getFloor(Long key) {
        return floorCache.getIfPresent(key);
    }

    public void putRoom(Long key, Room value) {
        roomCache.put(key, value);
    }

    public Room getRoom(Long key) {
        return roomCache.getIfPresent(key);
    }

    public void putBed(Long key, Bed value) {
        bedCache.put(key, value);
    }

    public Bed getBed(Long key) {
        return bedCache.getIfPresent(key);
    }

    public void clearAll() {
        campusCache.invalidateAll();
        deptCache.invalidateAll();
        majorCache.invalidateAll();
        classCache.invalidateAll();
        floorCache.invalidateAll();
        roomCache.invalidateAll();
        bedCache.invalidateAll();
    }
}
