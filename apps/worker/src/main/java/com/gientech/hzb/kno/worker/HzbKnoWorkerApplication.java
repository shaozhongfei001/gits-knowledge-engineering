package com.gientech.hzb.kno.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gientech.hzb.kno")
public class HzbKnoWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HzbKnoWorkerApplication.class, args);
    }
}
