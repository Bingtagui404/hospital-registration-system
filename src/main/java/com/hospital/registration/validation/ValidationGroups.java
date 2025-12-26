package com.hospital.registration.validation;

/**
 * 验证分组接口
 */
public interface ValidationGroups {

    /**
     * 创建时的验证分组（如注册）
     */
    interface OnCreate {}

    /**
     * 更新时的验证分组（如修改个人信息）
     */
    interface OnUpdate {}
}
