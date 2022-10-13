package mx.com.bmf.satbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import io.github.bonigarcia.wdm.WebDriverManager;
import mx.com.bmf.satbot.controllers.TalkerController;
import mx.com.bmf.satbot.util.Props;
import mx.com.bmf.services.TalkerService;
import mx.com.bmf.services.impl.DefaultTalkerService;

@SpringBootApplication
@ComponentScan(basePackageClasses = TalkerController.class)
public class SatbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(SatbotApplication.class, args);
		WebDriverManager.chromedriver().setup();
	}

	@Bean
	public Props props() {
		return new Props();
	}

	@Bean
	public TalkerService talkerService() {
		return new DefaultTalkerService(props());
	}

}

