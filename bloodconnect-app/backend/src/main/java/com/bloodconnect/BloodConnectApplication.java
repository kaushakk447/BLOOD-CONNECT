package com.bloodconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = {"com.bloodconnect"})
public class BloodConnectApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BloodConnectApplication.class, args);
    }
}
