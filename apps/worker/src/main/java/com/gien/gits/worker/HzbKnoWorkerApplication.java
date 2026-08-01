package com.gien.gits.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gien.gits")
public class HzbKnoWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HzbKnoWorkerApplication.class, args);
    }
}
