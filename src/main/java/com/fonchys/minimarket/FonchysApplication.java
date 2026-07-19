package com.fonchys.minimarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// v1.0.0 - Sistema de gestión Fonchys Minimarket
@SpringBootApplication
@EnableScheduling
public class FonchysApplication {
    public static void main(String[] args) {
        SpringApplication.run(FonchysApplication.class, args);
    }
}
