package com.hospital.registration.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Schedule {

    private Integer scheduleId;
    private Integer doctorId;
    private LocalDate workDate;
    private String timeSlot;
    private Integer totalQuota;
    private Integer remainingQuota;
    private BigDecimal fee;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联字段（非数据库字段）
    private String doctorName;
    private String deptName;
    private Integer deptId;
    private String title;
}
