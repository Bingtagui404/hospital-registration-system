package com.hospital.registration.service;

import com.hospital.registration.entity.Registration;
import com.hospital.registration.vo.PageResult;
import com.hospital.registration.vo.Result;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface RegistrationService {

    Result<Registration> create(Integer patientId, Integer scheduleId);

    Result<Void> cancel(Integer regId);

    Result<List<Registration>> listByPatientId(Integer patientId);

    Result<Registration> getById(Integer regId);

    Result<List<Registration>> listWithFilter(LocalDate startDate, LocalDate endDate, String status);

    Result<PageResult<Registration>> listPageWithFilter(LocalDate startDate, LocalDate endDate, String status, int page, int pageSize);

    Result<Map<String, Object>> statistics();

    Result<Void> finish(Integer regId);
}
