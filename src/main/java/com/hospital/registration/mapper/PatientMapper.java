package com.hospital.registration.mapper;

import com.hospital.registration.entity.Patient;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PatientMapper {

    @Select("SELECT * FROM patient WHERE patient_id = #{patientId}")
    Patient selectById(@Param("patientId") Integer patientId);

    @Select("SELECT * FROM patient WHERE id_card = #{idCard}")
    Patient selectByIdCard(@Param("idCard") String idCard);

    @Select("SELECT * FROM patient WHERE phone = #{phone}")
    Patient selectByPhone(@Param("phone") String phone);

    @Select("SELECT * FROM patient WHERE phone = #{phone} AND password = #{password}")
    Patient selectByPhoneAndPassword(@Param("phone") String phone, @Param("password") String password);

    @Insert("INSERT INTO patient(patient_name, id_card, phone, gender, age, address, medical_history, password) " +
            "VALUES(#{patientName}, #{idCard}, #{phone}, #{gender}, #{age}, #{address}, #{medicalHistory}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "patientId")
    int insert(Patient patient);

    @Update("UPDATE patient SET patient_name = #{patientName}, phone = #{phone}, gender = #{gender}, " +
            "age = #{age}, address = #{address}, medical_history = #{medicalHistory} WHERE patient_id = #{patientId}")
    int update(Patient patient);
}
