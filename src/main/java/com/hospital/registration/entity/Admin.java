package com.hospital.registration.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Admin {

    private Integer adminId;
    private String username;
    private String password;
    private String realName;
    private Integer status;
    private LocalDateTime createTime;
}
