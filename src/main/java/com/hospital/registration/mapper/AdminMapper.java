package com.hospital.registration.mapper;

import com.hospital.registration.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper {

    @Select("SELECT * FROM admin WHERE username = #{username} AND password = #{password} AND status = 1")
    Admin selectByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    @Select("SELECT * FROM admin WHERE admin_id = #{adminId}")
    Admin selectById(@Param("adminId") Integer adminId);
}
