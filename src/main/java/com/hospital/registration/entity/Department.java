package com.hospital.registration.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Department {

    private Integer deptId;
    private String deptName;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
