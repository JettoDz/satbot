package mx.com.bmf.services.impl;

import java.nio.CharBuffer;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
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
    public ChromeDriver test() {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("download.default_directory", Paths.get("/temp/download").toAbsolutePath().toString());
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.extensions_to_open", "application/xml");
        prefs.put("safebrowsing.enabled", true);
        options.setAcceptInsecureCerts(true);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--disable-extensions");
        options.addArguments("--safebrowsing-disable-download-protection");
        options.addArguments("safebrowsing-disable-extension-blacklist");
        options.setHeadless(true);
        ChromeDriver driver = new ChromeDriver(options);
        
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        driver.get("https://portalcfdi.facturaelectronica.sat.gob.mx");

        String mainWindow = driver.getWindowHandle();

        driver.findElement(By.id("buttonFiel")).click();

        driver.findElement(By.id("fileCertificate")).sendKeys(Paths.get("/temp/.cer").toAbsolutePath().toString());
        
        driver.findElement(By.id("filePrivateKey")).sendKeys(Paths.get("/temp/.key").toAbsolutePath().toString());

        driver.findElement(By.id("privateKeyPassword")).sendKeys(CharBuffer.wrap(p));

        driver.findElement(By.id("submit")).click();

        driver.findElement(By.linkText("Consultar Facturas Recibidas")).click();

        driver.findElement(By.id("ctl00_MainContent_RdoFechas")).click();
        
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(By.id("DdlAnio")));
        new Select(driver.findElement(By.id("DdlAnio"))).selectByValue(String.valueOf(date.getYear()));

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_CldFecha_DdlMes")));
        new Select(driver.findElement(By.id("ctl00_MainContent_CldFecha_DdlMes"))).selectByValue(String.valueOf(date.getMonthValue()));

        driver.findElement(By.id("ctl00_MainContent_BtnBusqueda")).click();

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(By.id("seleccionador")));
        
        driver.findElements(By.id("BtnDescarga")).forEach(elem -> {
            elem.click();
            List<String> windows = driver.getWindowHandles().stream().distinct().collect(Collectors.toList());
            if (windows.remove(mainWindow) && windows.size() == 1) {
                driver.switchTo().window(windows.get(0));
                driver.close();
                driver.switchTo().window(mainWindow);
            }
        });
        System.out.println("done!");

        return driver;
    }

}
