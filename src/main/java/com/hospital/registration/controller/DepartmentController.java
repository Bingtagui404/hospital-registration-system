package com.hospital.registration.controller;

import com.hospital.registration.entity.Department;
import com.hospital.registration.service.DepartmentService;
import com.hospital.registration.vo.PageResult;
import com.hospital.registration.vo.Result;
import jakarta.validation.Valid;
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

    @GetMapping("/page")
    public Result<PageResult<Department>> listPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return departmentService.listPage(page, pageSize);
    }

    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable("id") Integer id) {
        return departmentService.getById(id);
    }

    @PostMapping
    public Result<Department> add(@Valid @RequestBody Department department) {
        return departmentService.add(department);
    }

    @PutMapping
    public Result<Department> update(@Valid @RequestBody Department department) {
        return departmentService.update(department);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Integer id) {
        return departmentService.delete(id);
    }

    @GetMapping("/search")
    public Result<List<Department>> search(@RequestParam(required = false) String keyword) {
        return departmentService.search(keyword);
    }
}
