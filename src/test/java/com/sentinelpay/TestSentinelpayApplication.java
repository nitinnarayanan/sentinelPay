package com.sentinelpay;

import org.springframework.boot.SpringApplication;

public class TestSentinelpayApplication {

	public static void main(String[] args) {
		SpringApplication.from(SentinelPayApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
