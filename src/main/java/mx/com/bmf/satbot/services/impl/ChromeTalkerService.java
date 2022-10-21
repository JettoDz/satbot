package mx.com.bmf.satbot.services.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import mx.com.bmf.satbot.services.TalkerService;
import mx.com.bmf.satbot.util.Props;

@Service(value = "chromeTalker")
public class ChromeTalkerService extends TalkerService<ChromeDriver> {

    private final Props props;

    private final char[] examplePassword = "".toCharArray();

    public ChromeTalkerService(Props props) {
        this.props = props;
    }
    
    @Override
    protected ChromeDriver supplyDriver (String folio) {
        Path downloadsFolder = Paths.get(props.getDlFolder() + folio);
        ChromeOptions opts = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("download.default_directory", downloadsFolder.toAbsolutePath().toString());
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

	@Override
    public void clear(String folio) throws IOException {
        clear(Paths.get(props.getDlFolder(), folio).toAbsolutePath());
    }

}
