package io.github.jettodz.satbot.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:satbot.properties")
public class SatbotProperties {

    private String key;
    private String dlFolder;

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

    /**
     * @param dlFolder - Ruta donde se descargaran las facturas.
     */
    @Value("${downloads.folder}")
    public void setDlFolder(String dlFolder) {
        this.dlFolder = dlFolder;
    }

    public String getDlFolder() {
        return dlFolder;
    }

}