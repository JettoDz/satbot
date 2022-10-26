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
import io.github.jettodz.satbot.util.SatbotProperties;

/**
 * Cambiando de opinion, ajustare el codigo para que sea importado como una libreria. 
 * Dicho eso, es necesario para las pruebas que exista un certificado y una llave para que se 
 * ejecuten las consultas al SAT. Por lo tanto, si los test fallan, es porque efectivamente
 * este proyecto requiere informacion confidencial para trabajar. Recomeidno por lo tanto 
 * definir estaticamente llaves para los testing, pero jamas deben dejarse en el codigo mismo
 * ni en un directorio especifico. Recomiendo usar {@link Files#createTempDirectory(String, java.nio.file.attribute.FileAttribute...)}
 * o {@link Files#createTempFile(String, String, java.nio.file.attribute.FileAttribute...)} para
 * cuando se almacenen estos datos.
 * 
 * En conclusion: Que los test fallen es el resultado esperado.
 * @author Fernando
 *
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
class SatbotApplicationTests {
	
	@LocalServerPort
	private int port;
	
	@Autowired 
	private TalkerController controller;
	@Autowired
	private SatbotProperties props;
	
	private static final String CHROME = "chrome";
	private static final String FIREFOX = "firefox";
	private static final String YEAR = "2021";
	private static final String MONTH = "6";
	
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
		assertThat(controller.oneByOne(CHROME, YEAR, MONTH)).matches(res -> res.getBody().startsWith("ok!"));
		assertThat(controller.oneByOne(FIREFOX, YEAR, MONTH)).matches(res -> res.getBody().startsWith("ok!"));
		assertThat(controller.oneByOne(CHROME, null, MONTH)).isEqualTo(ResponseEntity.badRequest().build());
		assertThat(controller.oneByOne(FIREFOX, YEAR, null)).isEqualTo(ResponseEntity.badRequest().build());
	}
	
	@Test
	@Order(4)
	void testZip() {
		assertThat(controller.asZip(CHROME, YEAR ,MONTH)).matches(res -> res.getBody().startsWith("ok!"));
		assertThat(controller.asZip(FIREFOX, YEAR, MONTH)).matches(res -> res.getBody().startsWith("ok!"));
		assertThat(controller.asZip(CHROME, null, MONTH)).isEqualTo(ResponseEntity.badRequest().build());
		assertThat(controller.asZip(FIREFOX, YEAR, null)).isEqualTo(ResponseEntity.badRequest().build());
	}
	
	@Test
	@Order(2)
	void testClear() {
		Path temp = null;
		try {
			temp = Files.createTempDirectory(Paths.get(props.getDlFolder()), null);
			assertThat(controller.clear(temp.getFileName().toString())).matches(res -> res.getBody().startsWith("Recursos limpiados"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertThat(controller.clear("unexistant-dir")).isEqualTo(ResponseEntity.internalServerError().body("Error al limpiar recursos"));		
		assertThat(controller.clear(null)).isEqualTo(ResponseEntity.badRequest().build());
	}

}
