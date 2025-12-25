package com.hospital.registration.controller;

import com.hospital.registration.entity.Schedule;
import com.hospital.registration.service.ScheduleService;
import com.hospital.registration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/list")
    public Result<List<Schedule>> list(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate workDate) {
        return scheduleService.listWithFilter(deptId, workDate);
    }

    @GetMapping("/available")
    public Result<List<Schedule>> listAvailable(
            @RequestParam Integer deptId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate workDate) {
        return scheduleService.listAvailable(deptId, workDate);
    }

    @GetMapping("/{id}")
    public Result<Schedule> getById(@PathVariable("id") Integer id) {
        return scheduleService.getById(id);
    }

    @PostMapping
    public Result<Schedule> add(@RequestBody Schedule schedule) {
        return scheduleService.add(schedule);
    }

    @PutMapping
    public Result<Schedule> update(@RequestBody Schedule schedule) {
        return scheduleService.update(schedule);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Integer id) {
        return scheduleService.delete(id);
    }

    @GetMapping("/doctor/{doctorId}")
    public Result<List<Schedule>> listByDoctor(
            @PathVariable Integer doctorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return scheduleService.listByDoctorAndDateRange(doctorId, startDate, endDate);
    }
}
