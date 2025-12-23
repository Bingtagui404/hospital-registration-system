package com.hospital.registration.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Patient {

    private Integer patientId;
    private String patientName;
    private String idCard;
    private String phone;
    private String gender;
    private Integer age;
    private String address;
    private String medicalHistory;
    private String password;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
