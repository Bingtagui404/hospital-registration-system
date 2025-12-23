package com.hospital.registration.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Registration {

    private Integer regId;
    private String regNo;
    private Integer patientId;
    private Integer scheduleId;
    private Integer doctorId;
    private Integer deptId;
    private LocalDate workDate;
    private String timeSlot;
    private Integer queueNo;
    private BigDecimal fee;
    private String status;
    private LocalDateTime regTime;
    private LocalDateTime updateTime;

    // 关联字段（非数据库字段）
    private String patientName;
    private String doctorName;
    private String deptName;
    private String title;
}
