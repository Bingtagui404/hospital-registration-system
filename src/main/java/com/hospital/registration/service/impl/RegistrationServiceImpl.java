package com.hospital.registration.service.impl;

import com.hospital.registration.entity.Registration;
import com.hospital.registration.entity.Schedule;
import com.hospital.registration.mapper.RegistrationMapper;
import com.hospital.registration.mapper.ScheduleMapper;
import com.hospital.registration.service.RegistrationService;
import com.hospital.registration.vo.PageResult;
import com.hospital.registration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    @Transactional
    public Result<Registration> create(Integer patientId, Integer scheduleId) {
        // 1. 查询号源
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            return Result.error("号源不存在");
        }
        if (schedule.getRemainingQuota() <= 0) {
            return Result.error("号源已满");
        }
        if (schedule.getStatus() != 1) {
            return Result.error("该号源已停诊");
        }

        // 2. 检查是否已预约过
        Registration existing = registrationMapper.selectByPatientAndSchedule(patientId, scheduleId);
        if (existing != null) {
            return Result.error("您已预约过该号源");
        }

        // 3. 原子扣减号源（乐观锁）
        int affected = scheduleMapper.decreaseQuota(scheduleId);
        if (affected == 0) {
            return Result.error("号源已被抢完，请重新选择");
        }

        // 4. 生成挂号单号（GH + yyyyMMdd + 6位序号）
        String regNo = generateRegNo(registrationMapper);

        // 5. 计算排队号（使用 MAX+1，避免并发冲突）
        Integer maxQueueNo = registrationMapper.selectMaxQueueNo(scheduleId);
        int queueNo = (maxQueueNo == null) ? 1 : maxQueueNo + 1;

        // 6. 创建挂号记录
        Registration registration = new Registration();
        registration.setRegNo(regNo);
        registration.setPatientId(patientId);
        registration.setScheduleId(scheduleId);
        registration.setDoctorId(schedule.getDoctorId());
        registration.setDeptId(schedule.getDeptId());
        registration.setWorkDate(schedule.getWorkDate());
        registration.setTimeSlot(schedule.getTimeSlot());
        registration.setQueueNo(queueNo);
        registration.setFee(schedule.getFee());
        registration.setStatus("BOOKED");

        // 7. 插入记录（带重试机制处理并发冲突）
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            try {
                registrationMapper.insert(registration);
                break; // 插入成功，跳出循环
            } catch (DuplicateKeyException e) {
                String msg = e.getMessage();
                // 判断冲突类型：queueNo冲突、reg_no冲突、还是真正的重复挂号
                if (msg != null && (msg.contains("uk_schedule_queue") || msg.contains("reg_no"))) {
                    // queueNo 或 reg_no 冲突，重新计算并重试
                    maxQueueNo = registrationMapper.selectMaxQueueNo(scheduleId);
                    queueNo = (maxQueueNo == null) ? 1 : maxQueueNo + 1;
                    registration.setQueueNo(queueNo);
                    registration.setRegNo(generateRegNo(registrationMapper));
                    if (i == maxRetry - 1) {
                        scheduleMapper.increaseQuota(scheduleId);
                        return Result.error("系统繁忙，请稍后重试");
                    }
                } else {
                    // 真正的重复挂号（uk_patient_schedule_active），回滚号源
                    scheduleMapper.increaseQuota(scheduleId);
                    return Result.error("您已预约过该号源，请勿重复挂号");
                }
            }
        }

        // 8. 补充返回信息
        registration.setDoctorName(schedule.getDoctorName());
        registration.setDeptName(schedule.getDeptName());
        registration.setTitle(schedule.getTitle());

        return Result.success("挂号成功", registration);
    }

    @Override
    @Transactional
    public Result<Void> cancel(Integer regId) {
        // 1. 查询挂号记录
        Registration registration = registrationMapper.selectById(regId);
        if (registration == null) {
            return Result.error("挂号记录不存在");
        }
        if (!"BOOKED".equals(registration.getStatus())) {
            return Result.error("该挂号记录无法取消");
        }

        // 2. 检查是否在允许退号时间内（就诊时间前1小时）
        LocalDateTime now = LocalDateTime.now();
        LocalDate workDate = registration.getWorkDate();
        int hour = "AM".equals(registration.getTimeSlot()) ? 8 : 14;
        LocalDateTime visitTime = workDate.atTime(hour, 0);

        if (now.isAfter(visitTime.minusHours(1))) {
            return Result.error("已超过退号时间（就诊前1小时）");
        }

        // 3. 原子更新状态（条件更新 + 检查影响行数，防止并发重复退号）
        int affected = registrationMapper.updateStatusWithCondition(regId, "BOOKED", "CANCELLED");
        if (affected == 0) {
            return Result.error("退号失败，该记录可能已被取消");
        }

        // 4. 恢复号源
        scheduleMapper.increaseQuota(registration.getScheduleId());

        return Result.success("退号成功", null);
    }

    @Override
    public Result<List<Registration>> listByPatientId(Integer patientId) {
        List<Registration> list = registrationMapper.selectByPatientId(patientId);
        return Result.success(list);
    }

    @Override
    public Result<Registration> getById(Integer regId) {
        Registration registration = registrationMapper.selectById(regId);
        if (registration == null) {
            return Result.error("挂号记录不存在");
        }
        return Result.success(registration);
    }

    @Override
    public Result<List<Registration>> listWithFilter(LocalDate startDate, LocalDate endDate, String status) {
        List<Registration> list = registrationMapper.selectWithFilter(startDate, endDate, status);
        return Result.success(list);
    }

    @Override
    public Result<PageResult<Registration>> listPageWithFilter(LocalDate startDate, LocalDate endDate, String status, int page, int pageSize) {
        // 分页参数边界校验
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        else if (pageSize > 100) pageSize = 100;

        int offset = (page - 1) * pageSize;
        List<Registration> list = registrationMapper.selectPageWithFilter(startDate, endDate, status, offset, pageSize);
        long total = registrationMapper.countWithFilter(startDate, endDate, status);
        return Result.success(PageResult.of(list, total, page, pageSize));
    }

    @Override
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("bookedCount", registrationMapper.countBooked());
        stats.put("cancelledCount", registrationMapper.countCancelled());
        stats.put("finishedCount", registrationMapper.countFinished());
        stats.put("totalFee", registrationMapper.sumFee());
        stats.put("deptStats", registrationMapper.countByDept());
        return Result.success(stats);
    }

    @Override
    @Transactional
    public Result<Void> finish(Integer regId) {
        // 1. 查询挂号记录
        Registration registration = registrationMapper.selectById(regId);
        if (registration == null) {
            return Result.error("挂号记录不存在");
        }
        if (!"BOOKED".equals(registration.getStatus())) {
            return Result.error("只有待就诊状态的记录才能标记为已就诊");
        }

        // 2. 原子更新状态
        int affected = registrationMapper.updateStatusWithCondition(regId, "BOOKED", "FINISHED");
        if (affected == 0) {
            return Result.error("标记失败，该记录可能已被修改");
        }

        return Result.success("标记成功", null);
    }

    /**
     * 生成挂号单号：GH + yyyyMMdd + 6位序号
     * 使用 MAX+1 查询当天最大序号，配合重试机制处理并发冲突
     */
    private String generateRegNo(RegistrationMapper mapper) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Integer maxSeq = mapper.selectMaxSeqByDate(dateStr);
        int seq = (maxSeq == null) ? 1 : maxSeq + 1;
        return String.format("GH%s%06d", dateStr, seq);
    }
}
