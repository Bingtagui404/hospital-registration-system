package com.hospital.registration.controller;

import com.hospital.registration.entity.Patient;
import com.hospital.registration.service.PatientService;
import com.hospital.registration.validation.ValidationGroups.OnCreate;
import com.hospital.registration.validation.ValidationGroups.OnUpdate;
import com.hospital.registration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping("/register")
    public Result<Patient> register(@Validated(OnCreate.class) @RequestBody Patient patient) {
        return patientService.register(patient);
    }

    @PostMapping("/login")
    public Result<Patient> login(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");
        return patientService.login(phone, password);
    }

    @GetMapping("/info")
    public Result<Patient> getInfo(@RequestParam Integer patientId) {
        return patientService.getInfo(patientId);
    }

    @PutMapping("/info")
    public Result<Patient> updateInfo(@Validated(OnUpdate.class) @RequestBody Patient patient) {
        return patientService.updateInfo(patient);
    }
}
