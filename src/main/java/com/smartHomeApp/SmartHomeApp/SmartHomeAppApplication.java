package com.smartHomeApp.SmartHomeApp;

import com.smartHomeApp.SmartHomeApp.config.jwt.RSAKeyRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RSAKeyRecord.class)
@ConfigurationPropertiesScan
public class SmartHomeAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartHomeAppApplication.class, args);
	}

}
