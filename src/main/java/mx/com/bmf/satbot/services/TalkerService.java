package mx.com.bmf.satbot.services;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import mx.com.bmf.satbot.util.Logging;

public abstract class TalkerService implements Logging {

    public abstract void oneByOne(UUID operation, String year, String month);

    public abstract void asZip(UUID operation, String year, String month);

    public abstract void clear(String folio) throws IOException;

    /**
     * Accede al portal del SAT
     * @param driver WebDriver usado para la sesion
     * @param password Contrasena de la clave FIEL
     * @return webHandler de la ventana principal, para posterior utilidad.
     * @throws InterruptedException 
     */
    protected String login(RemoteWebDriver driver, @Nonnull char[] password) throws InterruptedException {
        logInfo("Accediento a portalcfdi.facturaelectronica.sat.gob.mx");
        driver.get("https://portalcfdi.facturaelectronica.sat.gob.mx");
        String mainWindow = driver.getWindowHandle();
        Thread.sleep(100);
        driver.findElement(By.id("buttonFiel")).click();
        logDebug("Accediendo. Se usan el certificado y la llave privada");
        Thread.sleep(100);
        //estos paths son para efectos de pruebas. NUNCA ALMACENAR ARCHIVOS NI CONTRASEÑAS DE CLAVE FIEL
        driver.findElement(By.id("fileCertificate")).sendKeys(Paths.get("/tmp/.cer").toAbsolutePath().toString());
        driver.findElement(By.id("filePrivateKey")).sendKeys(Paths.get("/tmp/.key").toAbsolutePath().toString());
        driver.findElement(By.id("privateKeyPassword")).sendKeys(CharBuffer.wrap(password));

        driver.findElement(By.id("submit")).click();
        return mainWindow;
    }

    /**
     * Llena los selectores para la fecha dada
     * @param driver - WebDriver para la sesion
     * @param year - Cadena de texto con el anio, en formato yyyy
     * @param month - Cadena de texto con el numero del mes sin padding (Para Enero, pasar "1", en lugar de "01")
     */
    protected void requestForDate(RemoteWebDriver driver, String year, String month) {
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

    /**
     * Descarga una a una las facturas encontradas por la peticion segun la fecha
     * @param driver - WebDriver de la sesion
     * @param mainWindow - WebHandler de la ventana principal
     */
    protected void multipleDownloads(RemoteWebDriver driver, String mainWindow) {
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

    /**
     * Al intentar descarga una factura de forma individual, el portal lanza una ventana emergente por cada archivo.
     * Este metodo accese/usa la ventana emergente, la cierra y regresa a la principal.
     * @param driver - WebDriver de la sesion
     * @param webHandle - WebHandle de la ventana emergente
     * @param mainWindow - WebHandle de la ventana principal
     */
    protected void closeHandleAndSwitchTo(RemoteWebDriver driver, String webHandle, String mainWindow) {
        driver.switchTo().window(webHandle);
        driver.close();
        driver.switchTo().window(mainWindow);
    }
    
    protected void singleZipDownload(RemoteWebDriver driver) throws InterruptedException {
    	new WebDriverWait(driver, Duration.ofSeconds(10))
		        .until(ExpectedConditions.elementToBeClickable(By.id("seleccionador"))).click();
		new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.id("ctl00_MainContent_BtnDescargar"))).click();
		logInfo("Solicitud para descarga multiple correcta");
		Thread.sleep(200);
		String successMsg = new WebDriverWait(driver, Duration.ofSeconds(60))
		        .until(ExpectedConditions.presenceOfElementLocated(By.id("dvAlert")))
		        .findElement(By.className("alert-success"))
		        .getText();
		String requestUuid = successMsg.substring(successMsg.indexOf(":") + 1, successMsg.indexOf(",")).trim();
		logInfo("Se recupero UUID de solicitud correctamente");
		driver.executeScript("document.location.href='../Consulta.aspx'");
		Thread.sleep(500);
		new WebDriverWait(driver, Duration.ofSeconds(20))
				.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Recuperar Descargas de CFDI"))).click();
		List<WebElement> rows = new WebDriverWait(driver, Duration.ofSeconds(20))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_GridViewReporte")))
				.findElements(By.tagName("tr"));
		logInfo("Buscando UUID de solicitud para descaragar");
		rows.subList(1, rows.size()).stream()
									.collect(Collectors.toMap(we -> we.findElement(By.xpath(".//td[2]")).getText(), Function.identity()))
									.get(requestUuid)
									.findElement(By.id("BtnDescarga"))
									.click();
		Thread.sleep(200);
		logInfo("Descarga correcta");
    }

}
