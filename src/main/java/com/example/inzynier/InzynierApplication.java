package com.example.inzynier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class InzynierApplication {

    public static void main(String[] args) {
        SpringApplication.run(InzynierApplication.class, args);
    }

}
