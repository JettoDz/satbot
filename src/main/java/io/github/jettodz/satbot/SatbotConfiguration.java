package io.github.jettodz.satbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.jettodz.satbot.util.SatbotProperties;

/**
 * Esta clase permite registrar SatBot al contexto de Spring, de manera que sus componentes
 * puedan ser inyectables mediante {@link Autowired} y que la clase {@link SatbotProperties}
 * pueda leer las propiedaes del archivo {@code satbot.properties}.
 * <br>
 * El modo de uso pensado es mediante la importacion de esta clase a cualquier clase anotada
 * con {@link Configuration} o {@link SpringBootApplication} con la anotacion {@link Import}, 
 * como se muestra en el siguiente snippet:
 * 
 * <pre>
 * &#64;Configuration
 * &#64;Import(SatbotConfiguration.class)
 * public class MisConfiguraciones {
 * 
 *     // Otros beans aqui
 * 
 * }
 * </pre>
 * 
 * @author Fernando
 *
 */
@Configuration
@ComponentScan
public class SatbotConfiguration {
	
	public SatbotConfiguration() {
		WebDriverManager.chromedriver().setup();
		WebDriverManager.firefoxdriver().setup();
	}

}

