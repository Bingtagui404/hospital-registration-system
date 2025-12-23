package com.hospital.registration.mapper;

import com.hospital.registration.entity.Schedule;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper {

    @Select("SELECT s.*, d.doctor_name, d.title, d.dept_id, dept.dept_name " +
            "FROM schedule s " +
            "LEFT JOIN doctor d ON s.doctor_id = d.doctor_id " +
            "LEFT JOIN department dept ON d.dept_id = dept.dept_id " +
            "WHERE s.status = 1 ORDER BY s.work_date, s.time_slot")
    List<Schedule> selectAll();

    @Select("<script>" +
            "SELECT s.*, d.doctor_name, d.title, d.dept_id, dept.dept_name " +
            "FROM schedule s " +
            "LEFT JOIN doctor d ON s.doctor_id = d.doctor_id " +
            "LEFT JOIN department dept ON d.dept_id = dept.dept_id " +
            "WHERE s.status = 1 " +
            "<if test='deptId != null'> AND d.dept_id = #{deptId} </if>" +
            "<if test='workDate != null'> AND s.work_date = #{workDate} </if>" +
            "ORDER BY s.work_date, s.time_slot" +
            "</script>")
    List<Schedule> selectWithFilter(@Param("deptId") Integer deptId, @Param("workDate") LocalDate workDate);

    @Select("SELECT s.*, d.doctor_name, d.title, d.dept_id, dept.dept_name " +
            "FROM schedule s " +
            "LEFT JOIN doctor d ON s.doctor_id = d.doctor_id " +
            "LEFT JOIN department dept ON d.dept_id = dept.dept_id " +
            "WHERE d.dept_id = #{deptId} AND s.work_date = #{workDate} " +
            "AND s.remaining_quota > 0 AND s.status = 1 " +
            "ORDER BY s.time_slot")
    List<Schedule> selectAvailable(@Param("deptId") Integer deptId, @Param("workDate") LocalDate workDate);

    @Select("SELECT s.*, d.doctor_name, d.title, d.dept_id, dept.dept_name " +
            "FROM schedule s " +
            "LEFT JOIN doctor d ON s.doctor_id = d.doctor_id " +
            "LEFT JOIN department dept ON d.dept_id = dept.dept_id " +
            "WHERE s.schedule_id = #{scheduleId}")
    Schedule selectById(@Param("scheduleId") Integer scheduleId);

    @Insert("INSERT INTO schedule(doctor_id, work_date, time_slot, total_quota, remaining_quota, fee, status) " +
            "VALUES(#{doctorId}, #{workDate}, #{timeSlot}, #{totalQuota}, #{remainingQuota}, #{fee}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "scheduleId")
    int insert(Schedule schedule);

    @Update("UPDATE schedule SET doctor_id = #{doctorId}, work_date = #{workDate}, time_slot = #{timeSlot}, " +
            "total_quota = #{totalQuota}, remaining_quota = #{remainingQuota}, fee = #{fee}, status = #{status} " +
            "WHERE schedule_id = #{scheduleId}")
    int update(Schedule schedule);

    @Update("UPDATE schedule SET remaining_quota = remaining_quota - 1 " +
            "WHERE schedule_id = #{scheduleId} AND remaining_quota > 0")
    int decreaseQuota(@Param("scheduleId") Integer scheduleId);

    @Update("UPDATE schedule SET remaining_quota = remaining_quota + 1 " +
            "WHERE schedule_id = #{scheduleId}")
    int increaseQuota(@Param("scheduleId") Integer scheduleId);

    @Update("UPDATE schedule SET status = 0 WHERE schedule_id = #{scheduleId}")
    int deleteById(@Param("scheduleId") Integer scheduleId);
}
