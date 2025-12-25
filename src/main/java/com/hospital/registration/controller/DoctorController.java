package com.hospital.registration.controller;

import com.hospital.registration.entity.Doctor;
import com.hospital.registration.service.DoctorService;
import com.hospital.registration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/list")
    public Result<List<Doctor>> list() {
        return doctorService.list();
    }

    @GetMapping("/listByDept")
    public Result<List<Doctor>> listByDeptId(@RequestParam Integer deptId) {
        return doctorService.listByDeptId(deptId);
    }

    @GetMapping("/{id}")
    public Result<Doctor> getById(@PathVariable("id") Integer id) {
        return doctorService.getById(id);
    }

    @PostMapping
    public Result<Doctor> add(@RequestBody Doctor doctor) {
        return doctorService.add(doctor);
    }

    @PutMapping
    public Result<Doctor> update(@RequestBody Doctor doctor) {
        return doctorService.update(doctor);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Integer id) {
        return doctorService.delete(id);
    }

    @GetMapping("/search")
    public Result<List<Doctor>> search(@RequestParam(required = false) String keyword) {
        return doctorService.search(keyword);
    }
}
