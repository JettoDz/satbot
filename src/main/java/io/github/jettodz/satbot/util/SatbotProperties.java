package io.github.jettodz.satbot.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import io.github.jettodz.satbot.util.exceptions.UnsafePasswordUsageException;

/**
 * Lee las propiedades de {@code satbot.properties}. No podra leer si la libreria no es importada, inyectada como anotacion
 * o declarada autoconfigurable. 
 *<br>
 *Para esos casos, se ofrece un constructor vacio y setters o un constructor con los argumentos para setear las propiedades.
 * @author Fernando
 * @since 0.0.1
 */
@Component
@PropertySource("classpath:satbot.properties")
public class SatbotProperties implements Logging {

    private String key;
    private String dlFolder;
    private String fallbackPassword;
    
    public SatbotProperties() {
		// Constructor vacio para casos especiales.
	}
    
    /**
     * Constructor extraordinario para casos en los que se prefiera setear manualmente
     * las propiedades para Satbot.
     * @param key - Llave para desencriptar la contraseña de la clave FIEL
     * @param dlFolder - Directorio donde los TalkerService dejaran los XML descargados
     * @param fallbackPassword - Contraseña de un contribuyente. Unicamente usar en desarrollo, JAMAS en productivo
     */
    public SatbotProperties(String key, String dlFolder, String fallbackPassword) {
    	this.key = key;
    	this.dlFolder = dlFolder;
    	this.fallbackPassword = fallbackPassword;
    }

    @Value("${security.key}")
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return - Llave para desencriptar la contrasenia, idealmente.
     */
    public String getKey() {
        return key;
    }

    @Value("${downloads.folder}")
    public void setDlFolder(String dlFolder) {
        this.dlFolder = dlFolder;
    }
    
    /**
     * @return ruta donde se descargaran las facturas.
     */
    public String getDlFolder() {
        return dlFolder;
    }
    
    @Value("${security.temp.password:#{null}}")
    public void setFallbackPassword(String fallbackPassword) {
    	this.fallbackPassword = fallbackPassword;
    }
    
    /**
     * Obtiene la llave para un unico .cer y .key. Solo para pruebas. 
     * @return contraseña de un contribuyente
     */
    public String getFallbackPassword() {
    	logWarning(new UnsafePasswordUsageException().getMessage());
    	return fallbackPassword;
    }

}
