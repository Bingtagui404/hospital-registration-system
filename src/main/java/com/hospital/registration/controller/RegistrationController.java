package com.hospital.registration.controller;

import com.hospital.registration.entity.Registration;
import com.hospital.registration.service.RegistrationService;
import com.hospital.registration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @PostMapping
    public Result<Registration> create(@RequestBody Map<String, Integer> params) {
        Integer patientId = params.get("patientId");
        Integer scheduleId = params.get("scheduleId");
        if (patientId == null || scheduleId == null) {
            return Result.error("参数不完整：patientId 和 scheduleId 不能为空");
        }
        return registrationService.create(patientId, scheduleId);
    }

    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable("id") Integer id) {
        return registrationService.cancel(id);
    }

    @PutMapping("/finish/{id}")
    public Result<Void> finish(@PathVariable("id") Integer id) {
        return registrationService.finish(id);
    }

    @GetMapping("/my")
    public Result<List<Registration>> listByPatientId(@RequestParam Integer patientId) {
        return registrationService.listByPatientId(patientId);
    }

    @GetMapping("/{id}")
    public Result<Registration> getById(@PathVariable("id") Integer id) {
        return registrationService.getById(id);
    }

    @GetMapping("/list")
    public Result<List<Registration>> list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String status) {
        return registrationService.listWithFilter(startDate, endDate, status);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return registrationService.statistics();
    }
}
