package io.github.jettodz.satbot.services.impl;

import java.time.Duration;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.springframework.stereotype.Service;

import io.github.jettodz.satbot.services.TalkerService;
import io.github.jettodz.satbot.util.SatbotProperties;

/**
 * Implementacion especifica para Firefox. Headless.
 * @author Fernando
 * @since 0.0.1
 */
@Service(value = "firefoxTalker")
public class FirefoxTalkerService extends TalkerService<FirefoxDriver> {

    public FirefoxTalkerService(SatbotProperties props) {
        super(props);
    }
	
	@Override
	protected FirefoxDriver supplyDriver(String folio) {
		FirefoxOptions opts = new FirefoxOptions();
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("browser.download.folderList", 2);
		profile.setPreference("browser.download.manager.showWhenStarting", false);
		profile.setPreference("browser.download.dir", operationDir(folio).toAbsolutePath().toString());
		profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/xml");
		opts.setProfile(profile);
		opts.setHeadless(true);
		FirefoxDriver driver = new FirefoxDriver(opts);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		return driver;
	}

}
