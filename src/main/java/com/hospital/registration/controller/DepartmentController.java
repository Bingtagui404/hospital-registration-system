package com.hospital.registration.controller;

import com.hospital.registration.entity.Department;
import com.hospital.registration.service.DepartmentService;
import com.hospital.registration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dept")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/list")
    public Result<List<Department>> list() {
        return departmentService.list();
    }

    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable("id") Integer id) {
        return departmentService.getById(id);
    }

    @PostMapping
    public Result<Department> add(@RequestBody Department department) {
        return departmentService.add(department);
    }

    @PutMapping
    public Result<Department> update(@RequestBody Department department) {
        return departmentService.update(department);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Integer id) {
        return departmentService.delete(id);
    }
}
