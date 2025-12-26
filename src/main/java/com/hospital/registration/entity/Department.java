package com.hospital.registration.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Department {

    private Integer deptId;

    @NotBlank(message = "科室名称不能为空")
    @Size(min = 2, max = 20, message = "科室名称长度为2-20个字符")
    private String deptName;

    @Size(max = 200, message = "描述不能超过200个字符")
    private String description;

    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
