package com.hospital.registration.service.impl;

import com.hospital.registration.entity.Department;
import com.hospital.registration.mapper.DepartmentMapper;
import com.hospital.registration.service.DepartmentService;
import com.hospital.registration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public Result<List<Department>> list() {
        List<Department> list = departmentMapper.selectAll();
        return Result.success(list);
    }

    @Override
    public Result<Department> getById(Integer deptId) {
        Department department = departmentMapper.selectById(deptId);
        if (department == null) {
            return Result.error("科室不存在");
        }
        return Result.success(department);
    }

    @Override
    @Transactional
    public Result<Department> add(Department department) {
        // 检查科室名称是否已存在
        Department existing = departmentMapper.selectByName(department.getDeptName());
        if (existing != null) {
            return Result.error("科室名称已存在");
        }

        department.setStatus(1);
        departmentMapper.insert(department);
        return Result.success("添加成功", department);
    }

    @Override
    @Transactional
    public Result<Department> update(Department department) {
        Department existing = departmentMapper.selectById(department.getDeptId());
        if (existing == null) {
            return Result.error("科室不存在");
        }

        // 检查名称是否与其他科室重复
        Department byName = departmentMapper.selectByName(department.getDeptName());
        if (byName != null && !byName.getDeptId().equals(department.getDeptId())) {
            return Result.error("科室名称已存在");
        }

        departmentMapper.update(department);
        return Result.success("更新成功", department);
    }

    @Override
    @Transactional
    public Result<Void> delete(Integer deptId) {
        Department existing = departmentMapper.selectById(deptId);
        if (existing == null) {
            return Result.error("科室不存在");
        }

        departmentMapper.deleteById(deptId);
        return Result.success("删除成功", null);
    }
}
