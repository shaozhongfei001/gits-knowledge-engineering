package com.gien.gits.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gien.gits")
public class GitsKnoApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(GitsKnoApiApplication.class, args);
    }
}
