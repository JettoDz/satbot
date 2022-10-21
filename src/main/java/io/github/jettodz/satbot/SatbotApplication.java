package io.github.jettodz.satbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.bonigarcia.wdm.WebDriverManager;

@SpringBootApplication
public class SatbotApplication {

	public static void main(String[] args) {
		WebDriverManager.chromedriver().setup();
		WebDriverManager.firefoxdriver().setup();
		SpringApplication.run(SatbotApplication.class, args);
	}

}
