package com.hospital.registration.entity;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Schedule {

    private Integer scheduleId;

    @NotNull(message = "请选择医生")
    private Integer doctorId;

    @NotNull(message = "请选择日期")
    private LocalDate workDate;

    @NotBlank(message = "请选择时段")
    @Pattern(regexp = "^(AM|PM)$", message = "时段只能是AM或PM")
    private String timeSlot;

    @NotNull(message = "请输入总号源")
    @Min(value = 1, message = "总号源至少为1")
    @Max(value = 100, message = "总号源不能超过100")
    private Integer totalQuota;

    private Integer remainingQuota;

    @NotNull(message = "请输入挂号费")
    @DecimalMin(value = "0", message = "挂号费不能为负数")
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
