package mx.com.bmf.satbot.util;

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
	
	default void logWarning(Exception e) {
		logger().warning(e.getMessage() + "\n" + e.getCause().toString());
	}
	
	default void logError(Exception e) {
		logger().warning(e.getMessage() + "\n" + e.getCause().toString());
	}
	
}
