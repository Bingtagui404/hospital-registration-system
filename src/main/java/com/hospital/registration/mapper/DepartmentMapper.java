package com.hospital.registration.mapper;

import com.hospital.registration.entity.Department;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    @Select("SELECT * FROM department WHERE status = 1 ORDER BY dept_id")
    List<Department> selectAll();

    @Select("SELECT * FROM department WHERE dept_id = #{deptId}")
    Department selectById(@Param("deptId") Integer deptId);

    @Select("SELECT * FROM department WHERE dept_name = #{deptName}")
    Department selectByName(@Param("deptName") String deptName);

    // 查找已删除的同名科室（用于恢复）
    @Select("SELECT * FROM department WHERE dept_name = #{deptName} AND status = 0")
    Department selectDeletedByName(@Param("deptName") String deptName);

    // 恢复已删除的科室
    @Update("UPDATE department SET status = 1, description = #{description} WHERE dept_id = #{deptId}")
    int restoreById(@Param("deptId") Integer deptId, @Param("description") String description);

    @Insert("INSERT INTO department(dept_name, description, status) VALUES(#{deptName}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "deptId")
    int insert(Department department);

    @Update("UPDATE department SET dept_name = #{deptName}, description = #{description}, status = #{status} WHERE dept_id = #{deptId}")
    int update(Department department);

    @Update("UPDATE department SET status = 0 WHERE dept_id = #{deptId}")
    int deleteById(@Param("deptId") Integer deptId);

    // 搜索科室（按名称模糊匹配）
    @Select("SELECT * FROM department WHERE status = 1 AND dept_name LIKE CONCAT('%', #{keyword}, '%') ORDER BY dept_id")
    List<Department> searchByName(@Param("keyword") String keyword);
}
