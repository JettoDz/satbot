package io.github.jettodz.satbot.services.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import io.github.jettodz.satbot.services.TalkerService;
import io.github.jettodz.satbot.util.SatbotProperties;

/**
 * Implementacion especifica para Chrome. Headless.
 * @author Fernando
 * @since 0.0.1
 */
@Service(value = "chromeTalker")
public class ChromeTalkerService extends TalkerService<ChromeDriver> {

	private final char[] examplePassword = null;

	public ChromeTalkerService(SatbotProperties props) {
		super(props);
	}
    
    @Override
    protected ChromeDriver supplyDriver (String folio) {
        ChromeOptions opts = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("download.default_directory", operationDir(folio).toAbsolutePath().toString());
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.extensions_to_open", "application/xml");
        prefs.put("safebrowsing.enabled", true);
        opts.setAcceptInsecureCerts(true);
        opts.setExperimentalOption("prefs", prefs);
        opts.addArguments("--disable-extensions");
        opts.addArguments("--safebrowsing-disable-download-protection");
        opts.addArguments("safebrowsing-disable-extension-blacklist");
        opts.setHeadless(true);
        ChromeDriver dr = new ChromeDriver(opts);
        dr.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        return dr;
    }

    @Override
    public void oneByOne(UUID operation, String year, String month) {
        oneByOne(operation, examplePassword, year, month);
    }

	@Override
	public void asZip(UUID operation, String year, String month) {
        asZip(operation, examplePassword, year, month);
	}

}
