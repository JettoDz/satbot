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
    public void test() {

        ChromeOptions opts = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("download.default_directory", Paths.get("/tmp/download").toAbsolutePath().toString());
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

        List<Path> downloads = new ArrayList<>(0);
        try{
            downloads = Files.walk(Paths.get("/tmp/download")).map(Path::toAbsolutePath).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        System.out.println("done! ");
        if (downloads.isEmpty()) {
            System.out.println("Error fetching paths");
        } else {
            System.out.println("Paths for download: ");
            downloads.forEach(p -> System.out.println(p.toString()));
        }
        driver.quit();;
    }

}
