package mx.com.bmf.satbot.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:satbot.properties")
public class Props {

    private String key;

    @Value("${security.key}")
    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
    
}
