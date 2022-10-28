package io.github.jettodz.satbot.util;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.github.jettodz.satbot.services.TalkerService;
import io.github.jettodz.satbot.util.exceptions.SatbotException;

/**
 * Envuelve los errores esperados durante el proceso de autentificacion y descarga de facturas
 * en el portal del SAT.
 * @author Fernando
 *
 * @param <T> - Tipo del RemoteWebDriver usado en la implementacion. Por defecto, se intuye automaticamente para subclases
 * de {@link TalkerService}, por lo que puede definir un SatbotConsumer con tipo <b>T</b>
 * @since 0.0.1
 */
@FunctionalInterface
public interface SatbotExecution<T extends RemoteWebDriver> {
	
	/**
	 * Ejecuta los metodos necesarios para llevar a cabo la funcion de acceso, autentificacion y descarga.
	 * @param driver - RemoteWebDriver de la implementacion
	 * @param password - De la FIEL del contribuyente
	 * @param cer - Archivo .cer del contribuyente
	 * @param key - Archivo .key del contribuyente
	 * @param year - Año a filtrar
	 * @param month - Mes a filtrar
	 * @throws SatbotException - Para fallos en autenticacion o uso de contraseña obtenida del arhcivo satbor.properties
	 * @throws WebDriverException - En caso de que alguna ejecucion de Selenium haya fallado
	 * @throws InterruptedException - Para atender a los Sleeps necesarios para dar tiempo al portal del SAT de cargar
	 */
	public void accept(T driver, char[] password, byte[] cer, byte[] key, String year, String month)
			throws SatbotException, WebDriverException, InterruptedException;
	
}
