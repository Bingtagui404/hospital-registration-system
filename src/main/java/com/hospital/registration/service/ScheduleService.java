package com.hospital.registration.service;

import com.hospital.registration.entity.Schedule;
import com.hospital.registration.vo.Result;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    Result<List<Schedule>> list();

    Result<List<Schedule>> listWithFilter(Integer deptId, LocalDate workDate);

    Result<List<Schedule>> listAvailable(Integer deptId, LocalDate workDate);

    Result<Schedule> getById(Integer scheduleId);

    Result<Schedule> add(Schedule schedule);

    Result<Schedule> update(Schedule schedule);

    Result<Void> delete(Integer scheduleId);

    Result<List<Schedule>> listByDoctorAndDateRange(Integer doctorId, LocalDate startDate, LocalDate endDate);
}
