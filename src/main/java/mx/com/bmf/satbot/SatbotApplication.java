package mx.com.bmf.satbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.bonigarcia.wdm.WebDriverManager;
import mx.com.bmf.satbot.controllers.Talker;

@SpringBootApplication
public class SatbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(SatbotApplication.class, args);
		WebDriverManager.chromedriver().setup();
		Talker test = new Talker();
		test.test();
	}

}

