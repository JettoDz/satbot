package mx.com.bmf.satbot.services.impl;

import mx.com.bmf.satbot.services.TalkerService;
import mx.com.bmf.satbot.util.FileDeleter;
import mx.com.bmf.satbot.util.Props;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service(value = "chromeTalker")
public class ChromeTalkerService extends TalkerService {

    private final Props props;

    private final char[] examplePassword = "".toCharArray();
    
    private final Function<Path, ChromeOptions> options = downloadsFolder -> {
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
        return opts;
    };

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
//        opts.setHeadless(true);
        ChromeDriver dr = new ChromeDriver(opts);
        dr.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        return dr;
    }

    public ChromeTalkerService(Props props) {
        this.props = props;
    }

    @Override
    public String testProp(){
        return props.getKey();
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
        } catch (Exception e) { // No importa que error sea, siempre hay que cerrar sesion y driver
            logError(e);
        } finally {
        	try {
                driver.executeScript("document.getElementById('anchorClose').click()");
            } catch (Exception e2) { // En caso de que el error sea interno o no haya llegado a ingresar
                logError(e2);
            }
            driver.quit();
        }
    }

	@Override
	public void asZip(UUID operation) {
        ChromeDriver driver = supplyDriver(operation.toString());
		/*
		 * Para descarga de paquetes completos. 
		 * 
		 * Esto usa el elemento "seleccionador" del Sat, un Checkbox. Este elemento y un boton debajo generan un uuid
		 * de descarga similar al que usa el metodo oneByOne, pero lo despliega en un div/span con fondo verde. Probablemente
		 * hay que capturarlo directamente con webElement.getText() con ayuda de REGEX o algo parecido. 
		 * 
		 * Para descargar el zip, hay que regresar a inicio, dar click en la tercera liga y alli esta una lista de solicitudes
		 * de descarga identificadas por uuid. Habra que filtrar los tr segun el uuid objetivo y dar click al BtnDescarga de ese
		 * tr. Esto quiere decir que todos los botones de descarga tienen el mismo ID. No entiendo como eso es posible, pero de 
		 * alli que hay que obtener el tr completo, para poder acceder al td con el boton y el del uuid.
		 */
        try {
            String mainWindow = login(driver, examplePassword);
            logInfo("Acceso exitoso");
            requestForDate(driver, "2021", "6");
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(By.id("seleccionador"))).click();
            driver.findElement(By.id("ctl00_MainContent_BtnDescargar")).click();
            logInfo(new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.presenceOfElementLocated(By.id("dvAlert")))
                    .findElement(By.className("alert-success"))
                    .getText());
        } catch (Exception e) { // No importa que error sea, siempre hay que cerrar sesion y driver
            logError(e);
        } finally {
            try {
                driver.executeScript("document.getElementById('anchorClose').click()");
            } catch (Exception e2) { // En caso de que el error sea interno o no haya llegado a ingresar
                logError(e2);
            }
            driver.quit();
        }

        String btnDescargarSeleccionados = "ctl00_MainContent_BtnDescargar";
        String divAlertaVerde = "dvAlert";
        // Hay un hijo, class alert-success, con un boton, un strong, texto, br, b y texto. Nos interesa el primer texto,
        // o sea, el tercer elemento/innertext del div alert-success
        String mensajeExito = "La descarga de los CFDI se encuentra en proceso y podrás obtener el resultado con el folio de " +
                "descarga:D56854AE-7C56-4A5F-A4DD-3E30ACBE216F, en la opción: Recuperar descargas de CFDI."; // substring entre ':' y ','?
        String uuidDescarga = mensajeExito.substring(mensajeExito.indexOf(":") + 1, mensajeExito.indexOf(","));
        String inicio; // findElement(By.linktext("Inicio").click(), doucment.href='../Consulta.aspx'...?
        String textoConsultaDescargas = "Recuperar Descargas de CFDI";
        String tablaHistorial = "ctl00_MainContent_GridViewReporte";
        // Aqui, (esperar) tomar los TDs, quitar el primero, generar Map<String, WebElement> con el UUID (xpath: .//td[2]
        // Asi se puede buscar relativamente rapido el botonDescarga del uuidDescarga.
        // Procedimiento estandar de descarga
        // Cerrar sesion y matar driver
	}

    @Override
    public void clear(String folio) throws IOException {
        logInfo("Limpiando registros de @"+ folio);
        Files.walkFileTree(Paths.get(props.getDlFolder() + folio), new FileDeleter());
    }

}
