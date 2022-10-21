package io.github.jettodz.satbot.util;

import java.util.logging.Logger;

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
		logger().warning(e.getMessage() + "\n" + e.getCause().toString());
	}
	
	default <E extends Exception> void logError(E e) {
		logger().warning(e.getMessage() + "\n" + e.getCause().toString());
	}
	
}
