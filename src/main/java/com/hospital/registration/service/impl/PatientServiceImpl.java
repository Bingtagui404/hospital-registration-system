package com.hospital.registration.service.impl;

import com.hospital.registration.entity.Patient;
import com.hospital.registration.mapper.PatientMapper;
import com.hospital.registration.service.PatientService;
import com.hospital.registration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientMapper patientMapper;

    @Override
    @Transactional
    public Result<Patient> register(Patient patient) {
        // 检查身份证是否已注册
        Patient byIdCard = patientMapper.selectByIdCard(patient.getIdCard());
        if (byIdCard != null) {
            return Result.error("该身份证号已注册");
        }

        // 检查手机号是否已注册
        Patient byPhone = patientMapper.selectByPhone(patient.getPhone());
        if (byPhone != null) {
            return Result.error("该手机号已注册");
        }

        patientMapper.insert(patient);
        patient.setPassword(null); // 不返回密码
        return Result.success("注册成功", patient);
    }

    @Override
    public Result<Patient> login(String phone, String password) {
        Patient patient = patientMapper.selectByPhoneAndPassword(phone, password);
        if (patient == null) {
            return Result.error("手机号或密码错误");
        }
        patient.setPassword(null); // 不返回密码
        return Result.success("登录成功", patient);
    }

    @Override
    public Result<Patient> getInfo(Integer patientId) {
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            return Result.error("患者不存在");
        }
        patient.setPassword(null);
        return Result.success(patient);
    }

    @Override
    @Transactional
    public Result<Patient> updateInfo(Patient patient) {
        Patient existing = patientMapper.selectById(patient.getPatientId());
        if (existing == null) {
            return Result.error("患者不存在");
        }

        patientMapper.update(patient);
        patient.setPassword(null);
        return Result.success("更新成功", patient);
    }
}
