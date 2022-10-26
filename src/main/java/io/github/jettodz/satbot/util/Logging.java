package io.github.jettodz.satbot.util;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.openqa.selenium.WebDriverException;

public interface Logging {
	
	default Logger logger() {
		return Logger.getLogger(this.getClass().getName());
	}
	
	default void logDebug(String message) {
		logger().fine(message);
	}
	
	default void logInfo(String message) {
		logger().info(message);
	}
	
	default <E extends Exception> void logWarning(E e) {
		logger().log(Level.WARNING, getOriginRefence(e), e);
	}
	
	default <E extends Exception> void logError(E e) {
		logger().log(Level.SEVERE, getOriginRefence(e), e);
	}
	
	default <E extends Exception> String getOriginRefence(E e) {
		if (!(e instanceof WebDriverException)) return e.getClass().getName();
		Optional<StackTraceElement> satbotCause = Stream.of(e.getStackTrace())
														.filter(trace -> trace.getClassName().startsWith("io.github.jettodz"))
														.findFirst();
		return satbotCause.isPresent() ? satbotCause.get().toString() : e.getClass().getName();
	}
	
}
