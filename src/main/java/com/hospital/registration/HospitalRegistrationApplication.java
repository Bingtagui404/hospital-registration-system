package com.hospital.registration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication //标记是spring boot的应用
@MapperScan("com.hospital.registration.mapper") //告诉spring去哪里找mapper
public class HospitalRegistrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalRegistrationApplication.class, args);
    }
}
//启动应用
