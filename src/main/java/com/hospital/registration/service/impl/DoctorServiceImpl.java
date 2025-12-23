package com.hospital.registration.service.impl;

import com.hospital.registration.entity.Doctor;
import com.hospital.registration.mapper.DoctorMapper;
import com.hospital.registration.service.DoctorService;
import com.hospital.registration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public Result<List<Doctor>> list() {
        List<Doctor> list = doctorMapper.selectAll();
        return Result.success(list);
    }

    @Override
    public Result<List<Doctor>> listByDeptId(Integer deptId) {
        List<Doctor> list = doctorMapper.selectByDeptId(deptId);
        return Result.success(list);
    }

    @Override
    public Result<Doctor> getById(Integer doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            return Result.error("医生不存在");
        }
        return Result.success(doctor);
    }

    @Override
    @Transactional
    public Result<Doctor> add(Doctor doctor) {
        doctor.setStatus(1);
        doctorMapper.insert(doctor);
        return Result.success("添加成功", doctor);
    }

    @Override
    @Transactional
    public Result<Doctor> update(Doctor doctor) {
        Doctor existing = doctorMapper.selectById(doctor.getDoctorId());
        if (existing == null) {
            return Result.error("医生不存在");
        }
        doctorMapper.update(doctor);
        return Result.success("更新成功", doctor);
    }

    @Override
    @Transactional
    public Result<Void> delete(Integer doctorId) {
        Doctor existing = doctorMapper.selectById(doctorId);
        if (existing == null) {
            return Result.error("医生不存在");
        }
        doctorMapper.deleteById(doctorId);
        return Result.success("删除成功", null);
    }
}
