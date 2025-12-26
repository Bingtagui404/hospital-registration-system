package com.hospital.registration.mapper;

import com.hospital.registration.entity.Registration;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface RegistrationMapper {

    @Select("SELECT r.*, p.patient_name, d.doctor_name, d.title, dept.dept_name " +
            "FROM registration r " +
            "LEFT JOIN patient p ON r.patient_id = p.patient_id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.doctor_id " +
            "LEFT JOIN department dept ON r.dept_id = dept.dept_id " +
            "WHERE r.patient_id = #{patientId} ORDER BY r.reg_time DESC")
    List<Registration> selectByPatientId(@Param("patientId") Integer patientId);

    @Select("SELECT r.*, p.patient_name, d.doctor_name, d.title, dept.dept_name " +
            "FROM registration r " +
            "LEFT JOIN patient p ON r.patient_id = p.patient_id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.doctor_id " +
            "LEFT JOIN department dept ON r.dept_id = dept.dept_id " +
            "WHERE r.reg_id = #{regId}")
    Registration selectById(@Param("regId") Integer regId);

    @Select("SELECT * FROM registration WHERE patient_id = #{patientId} " +
            "AND schedule_id = #{scheduleId} AND status = 'BOOKED'")
    Registration selectByPatientAndSchedule(@Param("patientId") Integer patientId,
                                            @Param("scheduleId") Integer scheduleId);

    @Select("SELECT MAX(CAST(SUBSTRING(reg_no, 11) AS UNSIGNED)) FROM registration " +
            "WHERE reg_no LIKE CONCAT('GH', #{dateStr}, '%')")
    Integer selectMaxSeqByDate(@Param("dateStr") String dateStr);

    @Insert("INSERT INTO registration(reg_no, patient_id, schedule_id, doctor_id, dept_id, " +
            "work_date, time_slot, queue_no, fee, status) " +
            "VALUES(#{regNo}, #{patientId}, #{scheduleId}, #{doctorId}, #{deptId}, " +
            "#{workDate}, #{timeSlot}, #{queueNo}, #{fee}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "regId")
    int insert(Registration registration);

    // 条件更新：只有当前状态为 BOOKED 时才能取消，防止并发重复退号
    @Update("UPDATE registration SET status = #{newStatus} WHERE reg_id = #{regId} AND status = #{oldStatus}")
    int updateStatusWithCondition(@Param("regId") Integer regId,
                                   @Param("oldStatus") String oldStatus,
                                   @Param("newStatus") String newStatus);

    @Select("SELECT COUNT(*) FROM registration WHERE status = 'BOOKED'")
    int countBooked();

    @Select("SELECT COUNT(*) FROM registration WHERE status = 'CANCELLED'")
    int countCancelled();

    @Select("SELECT COUNT(*) FROM registration WHERE status = 'FINISHED'")
    int countFinished();

    @Select("SELECT COALESCE(SUM(fee), 0) FROM registration WHERE status != 'CANCELLED'")
    Double sumFee();

    // 查询指定号源的最大排队号（用于并发安全生成 queueNo）
    @Select("SELECT MAX(queue_no) FROM registration WHERE schedule_id = #{scheduleId}")
    Integer selectMaxQueueNo(@Param("scheduleId") Integer scheduleId);

    // 统计指定号源的已占用数量（非取消记录：BOOKED + FINISHED）
    @Select("SELECT COUNT(*) FROM registration WHERE schedule_id = #{scheduleId} AND status != 'CANCELLED'")
    int countOccupiedBySchedule(@Param("scheduleId") Integer scheduleId);

    @Select("<script>" +
            "SELECT r.*, p.patient_name, d.doctor_name, d.title, dept.dept_name " +
            "FROM registration r " +
            "LEFT JOIN patient p ON r.patient_id = p.patient_id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.doctor_id " +
            "LEFT JOIN department dept ON r.dept_id = dept.dept_id " +
            "WHERE 1=1 " +
            "<if test='startDate != null'> AND r.work_date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND r.work_date &lt;= #{endDate} </if>" +
            "<if test='status != null and status != \"\"'> AND r.status = #{status} </if>" +
            "ORDER BY r.reg_time DESC" +
            "</script>")
    List<Registration> selectWithFilter(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("status") String status);

    // 分页查询挂号记录
    @Select("<script>" +
            "SELECT r.*, p.patient_name, d.doctor_name, d.title, dept.dept_name " +
            "FROM registration r " +
            "LEFT JOIN patient p ON r.patient_id = p.patient_id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.doctor_id " +
            "LEFT JOIN department dept ON r.dept_id = dept.dept_id " +
            "WHERE 1=1 " +
            "<if test='startDate != null'> AND r.work_date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND r.work_date &lt;= #{endDate} </if>" +
            "<if test='status != null and status != \"\"'> AND r.status = #{status} </if>" +
            "ORDER BY r.reg_time DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<Registration> selectPageWithFilter(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            @Param("status") String status,
                                            @Param("offset") int offset,
                                            @Param("pageSize") int pageSize);

    // 统计挂号记录总数
    @Select("<script>" +
            "SELECT COUNT(*) FROM registration r " +
            "WHERE 1=1 " +
            "<if test='startDate != null'> AND r.work_date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND r.work_date &lt;= #{endDate} </if>" +
            "<if test='status != null and status != \"\"'> AND r.status = #{status} </if>" +
            "</script>")
    long countWithFilter(@Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate,
                         @Param("status") String status);

    // 按科室统计挂号数量（全量，排除已取消）
    @Select("SELECT dept.dept_name AS name, COUNT(*) AS count " +
            "FROM registration r " +
            "LEFT JOIN department dept ON r.dept_id = dept.dept_id " +
            "WHERE r.status != 'CANCELLED' " +
            "GROUP BY r.dept_id, dept.dept_name " +
            "ORDER BY count DESC")
    List<Map<String, Object>> countByDept();
}
