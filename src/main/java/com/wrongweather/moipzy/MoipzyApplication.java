package com.wrongweather.moipzy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude={SecurityAutoConfiguration.class})
@EnableScheduling //스케줄링을 위해 필요한 annotation
public class MoipzyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoipzyApplication.class, args);
	}

}
