package com.hospital.registration.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Doctor {

    private Integer doctorId;
    private Integer deptId;
    private String doctorName;
    private String gender;
    private String title;
    private String specialty;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联字段（非数据库字段）
    private String deptName;
}
