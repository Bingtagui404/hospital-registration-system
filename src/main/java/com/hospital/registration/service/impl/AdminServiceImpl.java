package com.hospital.registration.service.impl;

import com.hospital.registration.entity.Admin;
import com.hospital.registration.mapper.AdminMapper;
import com.hospital.registration.service.AdminService;
import com.hospital.registration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Result<Admin> login(String username, String password) {
        if (username == null || username.isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.error("密码不能为空");
        }

        Admin admin = adminMapper.selectByUsernameAndPassword(username, password);
        if (admin == null) {
            return Result.error("用户名或密码错误");
        }

        admin.setPassword(null);
        return Result.success("登录成功", admin);
    }
}
