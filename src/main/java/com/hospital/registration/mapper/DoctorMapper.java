package com.hospital.registration.mapper;

import com.hospital.registration.entity.Doctor;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DoctorMapper {

    @Select("SELECT d.*, dept.dept_name FROM doctor d " +
            "LEFT JOIN department dept ON d.dept_id = dept.dept_id " +
            "WHERE d.status = 1 ORDER BY d.doctor_id")
    List<Doctor> selectAll();

    @Select("SELECT d.*, dept.dept_name FROM doctor d " +
            "LEFT JOIN department dept ON d.dept_id = dept.dept_id " +
            "WHERE d.dept_id = #{deptId} AND d.status = 1 ORDER BY d.doctor_id")
    List<Doctor> selectByDeptId(@Param("deptId") Integer deptId);

    @Select("SELECT d.*, dept.dept_name FROM doctor d " +
            "LEFT JOIN department dept ON d.dept_id = dept.dept_id " +
            "WHERE d.doctor_id = #{doctorId}")
    Doctor selectById(@Param("doctorId") Integer doctorId);

    @Insert("INSERT INTO doctor(dept_id, doctor_name, gender, title, specialty, phone, status) " +
            "VALUES(#{deptId}, #{doctorName}, #{gender}, #{title}, #{specialty}, #{phone}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "doctorId")
    int insert(Doctor doctor);

    @Update("UPDATE doctor SET dept_id = #{deptId}, doctor_name = #{doctorName}, gender = #{gender}, " +
            "title = #{title}, specialty = #{specialty}, phone = #{phone}, status = #{status} " +
            "WHERE doctor_id = #{doctorId}")
    int update(Doctor doctor);

    @Update("UPDATE doctor SET status = 0 WHERE doctor_id = #{doctorId}")
    int deleteById(@Param("doctorId") Integer doctorId);

    // 搜索医生（同时匹配医生姓名和科室名称）
    @Select("SELECT d.*, dept.dept_name FROM doctor d " +
            "LEFT JOIN department dept ON d.dept_id = dept.dept_id " +
            "WHERE d.status = 1 AND (d.doctor_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR dept.dept_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY d.doctor_id")
    List<Doctor> searchByName(@Param("keyword") String keyword);
}
