package com.hospital.registration.service;

import com.hospital.registration.entity.Doctor;
import com.hospital.registration.vo.Result;

import java.util.List;

public interface DoctorService {

    Result<List<Doctor>> list();

    Result<List<Doctor>> listByDeptId(Integer deptId);

    Result<Doctor> getById(Integer doctorId);

    Result<Doctor> add(Doctor doctor);

    Result<Doctor> update(Doctor doctor);

    Result<Void> delete(Integer doctorId);

    Result<List<Doctor>> search(String keyword);
}
