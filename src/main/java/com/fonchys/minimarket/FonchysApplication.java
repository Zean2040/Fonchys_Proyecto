package com.fonchys.minimarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FonchysApplication {

    public static void main(String[] args) {
        SpringApplication.run(FonchysApplication.class, args);
    }
}
