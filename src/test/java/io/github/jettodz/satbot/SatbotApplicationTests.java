package io.github.jettodz.satbot;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.jettodz.satbot.controllers.TalkerController;
import io.github.jettodz.satbot.util.Props;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
class SatbotApplicationTests {
	
	@LocalServerPort
	private int port;
	
	@Autowired 
	private TalkerController controller;
	@Autowired
	private Props props;
	
	@BeforeAll
	public static void init() {
		WebDriverManager.chromedriver().setup();
		WebDriverManager.firefoxdriver().setup();
	}

	@Test
	@Order(1)
	void contextLoads() {
		assertThat(controller).isNotNull();
		assertThat(props).isNotNull();
	}
	
	@Test
	@Order(3)
	void testOneByOne() {
		assertThat(controller.oneByOne("chrome", "2021", "6")).matches(res -> res.getBody().startsWith("ok!"));
		assertThat(controller.oneByOne("firefox", "2021", "6")).matches(res -> res.getBody().startsWith("ok!"));
		assertThat(controller.oneByOne("chrome", null, "6")).isEqualTo(ResponseEntity.badRequest().build());
		assertThat(controller.oneByOne("firefox", "2021", null)).isEqualTo(ResponseEntity.badRequest().build());
	}
	
	@Test
	@Order(4)
	void testZip() {
		assertThat(controller.asZip("chrome", "2021", "6")).matches(res -> res.getBody().startsWith("ok!"));
		assertThat(controller.asZip("firefox", "2021", "6")).matches(res -> res.getBody().startsWith("ok!"));
		assertThat(controller.asZip("chrome", null, "6")).isEqualTo(ResponseEntity.badRequest().build());
		assertThat(controller.asZip("firefox", "2021", null)).isEqualTo(ResponseEntity.badRequest().build());
	}
	
	@Test
	@Order(2)
	void testClear() {
		Path temp = null;
		try {
			temp = Files.createTempDirectory(Paths.get(props.getDlFolder()), null);
			assertThat(controller.clear("chrome", temp.getFileName().toString())).matches(res -> res.getBody().startsWith("Recursos limpiados"));
			temp = Files.createTempDirectory(Paths.get(props.getDlFolder()), null);
			assertThat(controller.clear("firefox", temp.getFileName().toString())).matches(res -> res.getBody().startsWith("Recursos limpiados"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertThat(controller.clear("chrome", "unexistant-dir")).isEqualTo(ResponseEntity.internalServerError().body("Error al limpiar recursos"));		
		assertThat(controller.clear("chrome", null)).isEqualTo(ResponseEntity.badRequest().build());
	}

}
