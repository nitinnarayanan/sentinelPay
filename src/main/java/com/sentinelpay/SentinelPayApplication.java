package com.sentinelpay;

import com.sentinelpay.common.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class SentinelPayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SentinelPayApplication.class, args);
	}
}