package io.github.jettodz.satbot.services.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.springframework.stereotype.Service;

import io.github.jettodz.satbot.services.TalkerService;
import io.github.jettodz.satbot.util.Props;

@Service
public class FirefoxTalkerService extends TalkerService<FirefoxDriver> {

	private final Props props;
	
	private char[] examplePassword = "".toCharArray();

    public FirefoxTalkerService(Props props) {
        this.props = props;
    }
	
	@Override
	protected FirefoxDriver supplyDriver(String folio) {
		Path downloadsFolder = Paths.get(props.getDlFolder() + folio);
		FirefoxOptions opts = new FirefoxOptions();
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("browser.download.folderList", 2);
		profile.setPreference("browser.download.manager.showWhenStarting", false);
		profile.setPreference("browser.download.dir", downloadsFolder.toAbsolutePath().toString());
		profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/xml");
		opts.setProfile(profile);
		opts.setHeadless(true);
		FirefoxDriver driver = new FirefoxDriver(opts);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		return driver;
	}
	
	@Override
	public void oneByOne(UUID operation, String year, String month) {
		oneByOne(operation, examplePassword, year, month);
	}

	@Override
	public void asZip(UUID operation, String year, String month) {
		asZip(operation, examplePassword, year, month);
	}

	@Override
	public void clear(String folio) throws IOException {
		clear(Paths.get(props.getDlFolder() + folio).toAbsolutePath());
	}

	

}
