package com.hospital.registration.service;

import com.hospital.registration.entity.Department;
import com.hospital.registration.vo.Result;

import java.util.List;

public interface DepartmentService {

    Result<List<Department>> list();

    Result<Department> getById(Integer deptId);

    Result<Department> add(Department department);

    Result<Department> update(Department department);

    Result<Void> delete(Integer deptId);

    Result<List<Department>> search(String keyword);
}
