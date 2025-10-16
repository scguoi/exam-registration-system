package com.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 在线考试报名系统启动类
 * 
 * @author system
 * @since 2024-10-16
 */
@SpringBootApplication
@MapperScan("com.exam.mapper")
public class ExamRegistrationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamRegistrationSystemApplication.class, args);
        System.out.println("=================================");
        System.out.println("在线考试报名系统启动成功！");
        System.out.println("访问地址: http://localhost:8080/api");
        System.out.println("=================================");
    }
}
