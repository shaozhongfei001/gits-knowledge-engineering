package com.gientech.hzb.kno.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gientech.hzb.kno")
public class HzbKnoApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(HzbKnoApiApplication.class, args);
    }
}
