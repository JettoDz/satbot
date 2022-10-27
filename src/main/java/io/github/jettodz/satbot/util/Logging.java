package io.github.jettodz.satbot.util;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.openqa.selenium.WebDriverException;

import io.github.jettodz.satbot.util.exceptions.SatbotException;

/**
 * Permite volver a una clase "autologgeable" al brincar metodos para log.
 * @author Fernando
 * @since 0.0.1
 */
public interface Logging {
	
	/**
	 * @return Una instancia de Logger nativo de java para la clase implementante de la interfaz {@link Logging}
	 */
	default Logger logger() {
		return Logger.getLogger(this.getClass().getName());
	}
	
	/**
	 * Lanza un mensaje a STDOUT a nivel Debug/Fine
	 * @param message - Texto a imprimir
	 */
	default void logDebug(String message) {
		logger().fine(message);
	}
	
	/**
	 * Lanza un mensaje a STDOUT a nivel Info
	 * @param message - Texto a imprimir
	 */
	default void logInfo(String message) {
		logger().info(message);
	}
	
	/**
	 * Lanza un mensaje a STDOUT a nivel Warning
	 * @param message - Texto a imprimir
	 */
	default void logWarning(String message) {
		logger().warning(message);
	}
	
	/**
	 * Lanza un mensaje a STDOUT/STDERR a nivel Warning
	 * @param <E> - Subclase de {@link Exception}
	 * @param exception - Instancia de la excepcion capturada
	 */
	default <E extends Exception> void logWarning(E exception) {
		logger().log(Level.WARNING, getOriginRefence(exception), exception);
	}
	
	/**
	 * Lanza el origen del error y la causa como mensaje a STDOUT/STDERR a nivel Error/Severe
	 * @param <E> - Subclase de {@link Exception}
	 * @param exception - Instancia de la excepcion capturada
	 */
	default <E extends Exception> void logError(E exception) {
		logger().log(Level.SEVERE, getOriginRefence(exception), exception);
	}
	
	/**
	 * Obtiene la linea de la clase donde se genero el error si la misma se origino en algun metodo propio de
	 * Satbot. En caso de que sea de otro origen, obtiene y retorna el tipo de la excepcion
	 * @param <E> - Subclase de {@link Exception}
	 * @param exception - Instancia de la excepcion
	 * @return Clase y numero de linea donde surgio el problema o la clase de <b>E</b>
	 */
	default <E extends Exception> String getOriginRefence(E exception) {
		if (!(exception instanceof WebDriverException || exception instanceof SatbotException)) return exception.getClass().getName();
		Optional<StackTraceElement> satbotCause = Stream.of(exception.getStackTrace())
														.filter(trace -> trace.getClassName().startsWith("io.github.jettodz"))
														.findFirst();
		return satbotCause.isPresent() ? satbotCause.get().toString() : exception.getClass().getName();
	}
	
}
