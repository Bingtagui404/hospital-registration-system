package com.hospital.registration.service.impl;

import com.hospital.registration.entity.Schedule;
import com.hospital.registration.mapper.ScheduleMapper;
import com.hospital.registration.service.ScheduleService;
import com.hospital.registration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public Result<List<Schedule>> list() {
        List<Schedule> list = scheduleMapper.selectAll();
        return Result.success(list);
    }

    @Override
    public Result<List<Schedule>> listWithFilter(Integer deptId, LocalDate workDate) {
        List<Schedule> list = scheduleMapper.selectWithFilter(deptId, workDate);
        return Result.success(list);
    }

    @Override
    public Result<List<Schedule>> listAvailable(Integer deptId, LocalDate workDate) {
        List<Schedule> list = scheduleMapper.selectAvailable(deptId, workDate);
        return Result.success(list);
    }

    @Override
    public Result<Schedule> getById(Integer scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            return Result.error("排班不存在");
        }
        return Result.success(schedule);
    }

    @Override
    @Transactional
    public Result<Schedule> add(Schedule schedule) {
        schedule.setStatus(1);
        if (schedule.getRemainingQuota() == null) {
            schedule.setRemainingQuota(schedule.getTotalQuota());
        }
        try {
            scheduleMapper.insert(schedule);
        } catch (DuplicateKeyException e) {
            return Result.error("该医生在此日期和时段已有排班，请勿重复添加");
        }
        return Result.success("添加成功", schedule);
    }

    @Override
    @Transactional
    public Result<Schedule> update(Schedule schedule) {
        Schedule existing = scheduleMapper.selectById(schedule.getScheduleId());
        if (existing == null) {
            return Result.error("排班不存在");
        }
        try {
            scheduleMapper.update(schedule);
        } catch (DuplicateKeyException e) {
            return Result.error("该医生在此日期和时段已有其他排班");
        }
        return Result.success("更新成功", schedule);
    }

    @Override
    @Transactional
    public Result<Void> delete(Integer scheduleId) {
        Schedule existing = scheduleMapper.selectById(scheduleId);
        if (existing == null) {
            return Result.error("排班不存在");
        }
        scheduleMapper.deleteById(scheduleId);
        return Result.success("删除成功", null);
    }
}
