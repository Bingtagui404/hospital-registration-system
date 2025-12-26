package com.hospital.registration.service.impl;

import com.hospital.registration.entity.Department;
import com.hospital.registration.mapper.DepartmentMapper;
import com.hospital.registration.service.DepartmentService;
import com.hospital.registration.vo.PageResult;
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
    public Result<PageResult<Department>> listPage(int page, int pageSize) {
        // 分页参数边界校验
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        } else if (pageSize > 100) {
            pageSize = 100;
        }
        int offset = (page - 1) * pageSize;
        List<Department> list = departmentMapper.selectPage(offset, pageSize);
        long total = departmentMapper.countAll();
        return Result.success(PageResult.of(list, total, page, pageSize));
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
        // 检查科室名称是否已存在（包括有效科室）
        Department existing = departmentMapper.selectByName(department.getDeptName());
        if (existing != null && existing.getStatus() == 1) {
            return Result.error("科室名称已存在");
        }

        // 检查是否有已删除的同名科室，如有则恢复
        Department deleted = departmentMapper.selectDeletedByName(department.getDeptName());
        if (deleted != null) {
            departmentMapper.restoreById(deleted.getDeptId(), department.getDescription());
            deleted.setStatus(1);
            deleted.setDescription(department.getDescription());
            return Result.success("科室已恢复", deleted);
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

    @Override
    public Result<List<Department>> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return list();
        }
        List<Department> list = departmentMapper.searchByName(keyword.trim());
        return Result.success(list);
    }
}
