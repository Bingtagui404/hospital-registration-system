package com.hospital.registration.service;

import com.hospital.registration.entity.Admin;
import com.hospital.registration.vo.Result;

public interface AdminService {

    Result<Admin> login(String username, String password);
}
