package mx.com.bmf.satbot.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
// @PropertySource(value = "classpath:satbot.properties")
public class Props {

    private String key;

    @Value("${satbot.security.key}")
    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
    
}
