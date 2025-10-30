package com.smartHomeApp.SmartHomeApp;

import com.smartHomeApp.SmartHomeApp.infrastructure.jwt.RSAKeyRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(RSAKeyRecord.class)
@ConfigurationPropertiesScan
@EnableScheduling
public class SmartHomeAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartHomeAppApplication.class, args);
	}

}
