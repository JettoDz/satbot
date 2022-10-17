package mx.com.bmf.satbot.services.impl;

import java.nio.CharBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import mx.com.bmf.satbot.services.TalkerService;
import mx.com.bmf.satbot.util.Logging;
import mx.com.bmf.satbot.util.Props;

@Service(value = "chromeTalker")
public class ChromeTalkerService implements TalkerService, Logging {

    private Props props;

    private final char[] examplePassword = "".toCharArray();
    private final String ddlRoot = "/tmp/download/";
    
    private Function<Path, ChromeOptions> options = downloadsFolder -> {
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

    public ChromeTalkerService(Props props) {
        this.props = props;
    }

    @Override
    public String testProp(){
        return props.getKey();
    }
    
    private String login(WebDriver driver) {
    	logInfo("Accediento a portalcfdi.facturaelectronica.sat.gob.mx");
        driver.get("https://portalcfdi.facturaelectronica.sat.gob.mx");
        
        String mainWindow = driver.getWindowHandle();

        driver.findElement(By.id("buttonFiel")).click();

        logDebug("Accediendo. Se usan el certificado y la llave privada");
        //estos paths son para efectos de pruebas. NUNCA ALMACENAR ARCHIVOS NI CONTRASEÑAS DE CLAVE FIEL
        driver.findElement(By.id("fileCertificate")).sendKeys(Paths.get("/tmp/.cer").toAbsolutePath().toString());
        
        driver.findElement(By.id("filePrivateKey")).sendKeys(Paths.get("/tmp/.key").toAbsolutePath().toString());

        driver.findElement(By.id("privateKeyPassword")).sendKeys(CharBuffer.wrap(examplePassword));

        driver.findElement(By.id("submit")).click();
        
        return mainWindow;
    }
    
    private void requestForDate(WebDriver driver, String year, String month) {
    	driver.findElement(By.linkText("Consultar Facturas Recibidas")).click();

        driver.findElement(By.id("ctl00_MainContent_RdoFechas")).click();
        
        new WebDriverWait(driver, Duration.ofSeconds(10)).ignoring(StaleElementReferenceException.class)
                                                         .until(ExpectedConditions.elementToBeClickable(By.id("DdlAnio")));
        new Select(driver.findElement(By.id("DdlAnio"))).selectByVisibleText(year);

        new WebDriverWait(driver, Duration.ofSeconds(10)).ignoring(StaleElementReferenceException.class)
                                                         .until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_CldFecha_DdlMes")));
        new Select(driver.findElement(By.id("ctl00_MainContent_CldFecha_DdlMes"))).selectByValue(month);

        driver.findElement(By.id("ctl00_MainContent_BtnBusqueda")).click();
    }
    
    private void multipleDownloads(WebDriver driver, String mainWindow) {
    	new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(By.id("seleccionador")));
        logInfo("Descargando facturas");
        driver.findElements(By.id("BtnDescarga")).forEach(elem -> {
            try {
                new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(elem)).click();
                Thread.sleep(500); // No es lo mas limpio ni mucho menos, pero es sorprendentemente robusto
                driver.getWindowHandles().stream()
                						 .distinct()
                						 .filter(wh -> !wh.equals(mainWindow))
                						 .forEach(wh -> closeHandleAndSwitchTo(driver, wh, mainWindow));
            } catch (InterruptedException e) {
            	logError(e);
            }
        });
    }
    
    private void closeHandleAndSwitchTo(WebDriver driver, String webHandle, String mainWindow) {
    	driver.switchTo().window(webHandle);
        driver.close();
        driver.switchTo().window(mainWindow);
    }

    @Override
    public void oneByOne(String year, String month) {
    	UUID operation = UUID.randomUUID();
        String dlFolder = ddlRoot + operation.toString();
        ChromeDriver driver = new ChromeDriver(options.apply(Paths.get(dlFolder)));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        try {
        	String mainWindow = login(driver);
            logInfo("Acceso exitoso");
            requestForDate(driver, year, month);
            multipleDownloads(driver, mainWindow);
            System.out.println("Idea: Montar sobre la misma maquina o compartir una carpeta. Este sistema regresaria el UUID operation.");
        } catch (Exception e) { // No importa que error sea, siempre hay que cerrar sesion y driver
            logError(e);
        } finally {
        	driver.executeScript("document.getElementById('anchorClose').click()");
            driver.quit();
        }
    }

	@Override
	public void asZip() {
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
	}

}
