package com.hospital.registration.entity;

import com.hospital.registration.validation.ValidationGroups.OnCreate;
import com.hospital.registration.validation.ValidationGroups.OnUpdate;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Patient {

    private Integer patientId;

    @NotBlank(message = "姓名不能为空", groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 2, max = 20, message = "姓名长度为2-20个字符", groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]+$", message = "姓名只能包含中文", groups = {OnCreate.class, OnUpdate.class})
    private String patientName;

    @NotBlank(message = "身份证号不能为空", groups = {OnCreate.class})
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$",
            message = "身份证号格式不正确（需18位）", groups = {OnCreate.class})
    private String idCard;

    @NotBlank(message = "手机号不能为空", groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确（需11位）", groups = {OnCreate.class, OnUpdate.class})
    private String phone;

    private String gender;

    @Min(value = 0, message = "年龄不能为负数", groups = {OnCreate.class, OnUpdate.class})
    @Max(value = 150, message = "年龄不能超过150岁", groups = {OnCreate.class, OnUpdate.class})
    private Integer age;

    private String address;
    private String medicalHistory;

    @NotBlank(message = "密码不能为空", groups = {OnCreate.class})
    @Size(min = 6, max = 20, message = "密码长度为6-20位", groups = {OnCreate.class})
    private String password;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
