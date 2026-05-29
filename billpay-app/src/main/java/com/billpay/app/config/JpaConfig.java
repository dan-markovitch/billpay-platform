package com.billpay.app.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.billpay")
@EnableJpaRepositories(basePackages = "com.billpay")
public class JpaConfig {
}
