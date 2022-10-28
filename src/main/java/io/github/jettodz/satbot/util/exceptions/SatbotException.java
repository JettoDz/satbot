package io.github.jettodz.satbot.util.exceptions;

/**
 * Clase abstracta para controlar bien cual excepcion es parte de un error entre el uso de Satbot
 * y el codigo de Satbot mismo, ajeno a lo que ocurra directamente con Java o Selenium.
 * @author Fernando
 * @since 0.0.1
 */
public abstract class SatbotException extends Exception {

	private static final long serialVersionUID = -632064775613484240L;
	
	public SatbotException(String message) {
		super(message);
	}

}
