package com.hospital.registration.controller;

import com.hospital.registration.entity.Admin;
import com.hospital.registration.service.AdminService;
import com.hospital.registration.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result<Admin> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        return adminService.login(username, password);
    }
}
