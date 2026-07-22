package com.example.seckillzc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.seckillzc.mapper")
@SpringBootApplication
public class SeckillZcApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillZcApplication.class, args);
    }
}
