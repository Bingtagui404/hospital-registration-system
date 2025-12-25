package com.hospital.registration.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data //Lombok注解
public class Admin {

    private Integer adminId;
    private String username;
    private String password;
    private String realName;
    private Integer status;
    private LocalDateTime createTime;
}
