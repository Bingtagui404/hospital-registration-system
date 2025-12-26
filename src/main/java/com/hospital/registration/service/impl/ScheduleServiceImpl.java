package com.hospital.registration.service.impl;

import com.hospital.registration.entity.Schedule;
import com.hospital.registration.mapper.RegistrationMapper;
import com.hospital.registration.mapper.ScheduleMapper;
import com.hospital.registration.service.ScheduleService;
import com.hospital.registration.vo.PageResult;
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

    @Autowired
    private RegistrationMapper registrationMapper;

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
    public Result<PageResult<Schedule>> listPageWithFilter(Integer deptId, LocalDate workDate, int page, int pageSize) {
        // 分页参数边界校验
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        else if (pageSize > 100) pageSize = 100;

        int offset = (page - 1) * pageSize;
        List<Schedule> list = scheduleMapper.selectPageWithFilter(deptId, workDate, offset, pageSize);
        long total = scheduleMapper.countWithFilter(deptId, workDate);
        return Result.success(PageResult.of(list, total, page, pageSize));
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
        // 检查是否有已删除的同医生+日期+时段排班，如有则恢复
        Schedule deleted = scheduleMapper.selectDeletedByDoctorDateSlot(
                schedule.getDoctorId(), schedule.getWorkDate(), schedule.getTimeSlot());
        if (deleted != null) {
            // 校验必填字段
            if (schedule.getTotalQuota() == null) {
                return Result.error("总号源不能为空");
            }

            // 统计已有的非取消挂号数量，防止超卖
            int occupied = registrationMapper.countOccupiedBySchedule(deleted.getScheduleId());

            // 检查新的总号源是否小于已占用数
            if (schedule.getTotalQuota() < occupied) {
                return Result.error("总号源不能小于已占用数量（" + occupied + "）");
            }

            // 计算正确的剩余号源
            int remainingQuota = schedule.getTotalQuota() - occupied;

            scheduleMapper.restoreById(deleted.getScheduleId(),
                    schedule.getTotalQuota(), remainingQuota, schedule.getFee());
            deleted.setStatus(1);
            deleted.setTotalQuota(schedule.getTotalQuota());
            deleted.setRemainingQuota(remainingQuota);
            deleted.setFee(schedule.getFee());
            return Result.success("排班已恢复", deleted);
        }

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

        // 从挂号表统计真实已占用数量（BOOKED + FINISHED，避免脏数据和遗漏已就诊记录）
        int occupied = registrationMapper.countOccupiedBySchedule(schedule.getScheduleId());

        // 检查新的总号源是否小于已占用数
        if (schedule.getTotalQuota() != null && schedule.getTotalQuota() < occupied) {
            return Result.error("总号源不能小于已占用数量（" + occupied + "）");
        }

        // 同步调整剩余号源：remaining = newTotal - occupied
        if (schedule.getTotalQuota() != null) {
            schedule.setRemainingQuota(schedule.getTotalQuota() - occupied);
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

    @Override
    public Result<List<Schedule>> listByDoctorAndDateRange(Integer doctorId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> list = scheduleMapper.selectByDoctorAndDateRange(doctorId, startDate, endDate);
        return Result.success(list);
    }
}
