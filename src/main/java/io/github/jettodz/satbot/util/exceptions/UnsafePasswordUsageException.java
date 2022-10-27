package io.github.jettodz.satbot.util.exceptions;

/**
 * Excepcion indicante de que se esta leyendo estaticamente una contraseña de clave FIEL.
 * Esto es mala práctica, y se permite únicamente como manera de usar la API mientras se trabaja
 * en una formalización de acceso a los datos de un contribuyente (.cer, .key y contraseña) segura y
 * adecuadamente pensada. 
 * @author Fernando
 * @since 0.0.1
 *
 */
public class UnsafePasswordUsageException extends SatbotException {

	private static final long serialVersionUID = -7192229588154066233L;
	
	private static final String CAUSE = "Usando contraseña directamente desde satbot.properties. En conjunto con los archivos .cer y .key, su identidad puede ser vulnerable!";
	
	public UnsafePasswordUsageException() {
		super(CAUSE);
	}

}
