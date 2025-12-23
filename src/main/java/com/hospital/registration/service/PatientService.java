package com.hospital.registration.service;

import com.hospital.registration.entity.Patient;
import com.hospital.registration.vo.Result;

public interface PatientService {

    Result<Patient> register(Patient patient);

    Result<Patient> login(String phone, String password);

    Result<Patient> getInfo(Integer patientId);

    Result<Patient> updateInfo(Patient patient);
}
