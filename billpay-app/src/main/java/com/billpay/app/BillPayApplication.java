package com.billpay.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.billpay")
public class BillPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillPayApplication.class, args);
    }
}