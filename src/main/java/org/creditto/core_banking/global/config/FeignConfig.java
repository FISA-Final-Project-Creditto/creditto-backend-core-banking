package org.creditto.core_banking.global.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "org.creditto.core_banking.global.feign")
public class FeignConfig {

}

