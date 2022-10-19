package mx.com.bmf.satbot.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import mx.com.bmf.satbot.services.TalkerService;
import mx.com.bmf.satbot.util.FileDeleter;
import mx.com.bmf.satbot.util.Props;

@Service(value = "chromeTalker")
public class ChromeTalkerService extends TalkerService {

    private final Props props;

    private final char[] examplePassword = "".toCharArray();

    public ChromeTalkerService(Props props) {
        this.props = props;
    }
    
    private ChromeDriver supplyDriver (String folio) {
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

    /**
     * todo implementar salida en caso de maximo de descargas, maximo de sesiones o fallo al loggear
     * @param operation - UUID de generado por el cliente para identificar esta peticion
     * @param year - Anio solicitado para descarga de facturas
     * @param month - Mes solicitado para la descarga de facutras
     */
    @Override
    public void oneByOne(UUID operation, String year, String month) {
        ChromeDriver driver = supplyDriver(operation.toString());
        try {
        	String mainWindow = login(driver, examplePassword);
            logInfo("Acceso exitoso");
            requestForDate(driver, year, month);
            multipleDownloads(driver, mainWindow);
        } catch (WebDriverException | InterruptedException e) { // No importa que error sea, siempre hay que cerrar sesion y driver
            logError(e);
        } finally {
        	try {
        		logInfo("Cerrando sesion");
                driver.executeScript("document.getElementById('anchorClose').click()");
            } catch (WebDriverException e2) { // En caso de que el error sea interno o no haya llegado a ingresar
                logError(e2);
            }
            driver.quit();
            logInfo("Operacion exitosa");
        }
    }

	@Override
	public void asZip(UUID operation, String year, String month) {
        ChromeDriver driver = supplyDriver(operation.toString());
        try {
            login(driver, examplePassword);
            logInfo("Acceso exitoso");
            requestForDate(driver, year, month);
            singleZipDownload(driver);
        } catch (WebDriverException | InterruptedException e) { // No importa que error sea, siempre hay que cerrar sesion y driver
        	logInfo(e.getClass().getName());
            logError(e);
        } finally {
            try {
            	logInfo("Cerrando sesion");
                driver.executeScript("document.getElementById('anchorClose').click()");
            } catch (WebDriverException e2) { // En caso de que el error sea interno o no haya llegado a ingresar
                logError(e2);
            }
            driver.quit();
            logInfo("Operacion exitosa");
        }
	}

    @Override
    public void clear(String folio) throws IOException {
        logInfo("Limpiando registros de @"+ folio);
        Files.walkFileTree(Paths.get(props.getDlFolder() + folio), new FileDeleter());
    }

}
