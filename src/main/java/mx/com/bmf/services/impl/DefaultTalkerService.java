package mx.com.bmf.services.impl;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import mx.com.bmf.satbot.util.Props;
import mx.com.bmf.services.TalkerService;

@Service
public class DefaultTalkerService implements TalkerService {

    private Props props;

    private final String ddlRoot = "/tmp/download/";

    public DefaultTalkerService(Props props) {
        this.props = props;
    }

    char[] p = "".toCharArray();
    LocalDate date = LocalDate.of(2021, 8, 1);

    @Override
    public String testProp(){
        return props.getKey();
    }

    @Override
    public void test() {
        UUID operation = UUID.randomUUID();
        String ddlFolder = ddlRoot + operation.toString();
        ChromeOptions opts = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("download.default_directory", Paths.get(ddlFolder).toAbsolutePath().toString());
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.extensions_to_open", "application/xml");
        prefs.put("safebrowsing.enabled", true);
        opts.setAcceptInsecureCerts(true);
        opts.setExperimentalOption("prefs", prefs);
        opts.addArguments("--disable-extensions");
        opts.addArguments("--safebrowsing-disable-download-protection");
        opts.addArguments("safebrowsing-disable-extension-blacklist");
        opts.setHeadless(true);
        
        ChromeDriver driver = new ChromeDriver(opts);
        
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
            driver.get("https://portalcfdi.facturaelectronica.sat.gob.mx");

            String mainWindow = driver.getWindowHandle();

            driver.findElement(By.id("buttonFiel")).click();

            //estos paths son para efectos de pruebas. NUNCA ALMACENAR ARCHIVOS NI CONTRASEÑAS DE CLAVE FIEL
            driver.findElement(By.id("fileCertificate")).sendKeys(Paths.get("/tmp/.cer").toAbsolutePath().toString());
            
            driver.findElement(By.id("filePrivateKey")).sendKeys(Paths.get("/tmp/.key").toAbsolutePath().toString());

            driver.findElement(By.id("privateKeyPassword")).sendKeys(CharBuffer.wrap(p));

            driver.findElement(By.id("submit")).click();

            driver.findElement(By.linkText("Consultar Facturas Recibidas")).click();

            driver.findElement(By.id("ctl00_MainContent_RdoFechas")).click();
            
            new WebDriverWait(driver, Duration.ofSeconds(10)).ignoring(StaleElementReferenceException.class)
                                                                      .until(ExpectedConditions.elementToBeClickable(By.id("DdlAnio")));
            new Select(driver.findElement(By.id("DdlAnio"))).selectByVisibleText(String.valueOf(date.getYear()));

            new WebDriverWait(driver, Duration.ofSeconds(10)).ignoring(StaleElementReferenceException.class)
                                                                      .until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_CldFecha_DdlMes")));
            new Select(driver.findElement(By.id("ctl00_MainContent_CldFecha_DdlMes"))).selectByValue(String.valueOf(date.getMonthValue()));

            driver.findElement(By.id("ctl00_MainContent_BtnBusqueda")).click();

            new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(By.id("seleccionador")));
            driver.findElements(By.id("BtnDescarga")).forEach(elem -> {
                try {
                    new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(elem)).click();
                    Thread.sleep(500);
                    driver.getWindowHandles().stream().distinct().filter(wh -> !wh.equals(mainWindow)).forEach(wh -> {
                        driver.switchTo().window(wh);
                        driver.close();
                        driver.switchTo().window(mainWindow);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
            });
            System.out.println("Idea: Montar sobre la misma maquina y compartir una carpeta. Este sistema regresaria el UUID operation.");
        } catch (Exception e) { // No importa que error sea, siempre hay que cerrar el driver
            e.printStackTrace(System.err);
        } finally {
            driver.findElement(By.id("anchorClose")).click();
            driver.quit();
        }

    }

}
