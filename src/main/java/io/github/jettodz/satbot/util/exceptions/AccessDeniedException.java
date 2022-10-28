package io.github.jettodz.satbot.util.exceptions;

/**
 * Excepcion en caso de que la combinacion de llave, contraseña y key no resulten en logeo exitoso
 * @author Fernando
 * @since 0.0.1
 */
public class AccessDeniedException extends SatbotException {
	
	private static final String CAUSE = "Combinacion de archivo .cer, .key y contraseña no válidos.";
	
	private static final long serialVersionUID = -506862923029789114L;
	
	public AccessDeniedException() {
		super(CAUSE);
	}

}
