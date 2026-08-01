package com.gien.gits.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gien.gits")
public class HzbKnoApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(HzbKnoApiApplication.class, args);
    }
}
