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

    // 查找已删除的同医生+日期+时段排班（用于恢复）
    @Select("SELECT * FROM schedule WHERE doctor_id = #{doctorId} AND work_date = #{workDate} " +
            "AND time_slot = #{timeSlot} AND status = 0")
    Schedule selectDeletedByDoctorDateSlot(@Param("doctorId") Integer doctorId,
                                           @Param("workDate") LocalDate workDate,
                                           @Param("timeSlot") String timeSlot);

    // 恢复已删除的排班
    @Update("UPDATE schedule SET status = 1, total_quota = #{totalQuota}, " +
            "remaining_quota = #{remainingQuota}, fee = #{fee} WHERE schedule_id = #{scheduleId}")
    int restoreById(@Param("scheduleId") Integer scheduleId, @Param("totalQuota") Integer totalQuota,
                    @Param("remainingQuota") Integer remainingQuota, @Param("fee") java.math.BigDecimal fee);

    // 查询医生指定日期范围的排班（用于周历视图）
    @Select("SELECT s.*, d.doctor_name, d.title, d.dept_id, dept.dept_name " +
            "FROM schedule s " +
            "LEFT JOIN doctor d ON s.doctor_id = d.doctor_id " +
            "LEFT JOIN department dept ON d.dept_id = dept.dept_id " +
            "WHERE s.doctor_id = #{doctorId} " +
            "AND s.work_date BETWEEN #{startDate} AND #{endDate} " +
            "AND s.status = 1 ORDER BY s.work_date, s.time_slot")
    List<Schedule> selectByDoctorAndDateRange(@Param("doctorId") Integer doctorId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
}
