package mx.com.bmf.satbot.services;

import mx.com.bmf.satbot.util.Logging;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;

public abstract class TalkerService implements Logging {

    public abstract String testProp();

    public abstract void oneByOne(UUID operation, String year, String month);

    public abstract void asZip(UUID operation);

    public abstract void clear(String folio) throws IOException;

    /**
     * Accede al portal del SAT
     * @param driver WebDriver usado para la sesion
     * @param password Contrasena de la clave FIEL
     * @return webHandler de la ventana principal, para posterior utilidad.
     */
    protected String login(WebDriver driver, @Nonnull char[] password) {
        logInfo("Accediento a portalcfdi.facturaelectronica.sat.gob.mx");
        driver.get("https://portalcfdi.facturaelectronica.sat.gob.mx");
        String mainWindow = driver.getWindowHandle();

        driver.findElement(By.id("buttonFiel")).click();
        logDebug("Accediendo. Se usan el certificado y la llave privada");

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
    protected void requestForDate(WebDriver driver, String year, String month) {
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
    protected void multipleDownloads(WebDriver driver, String mainWindow) {
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
    protected void closeHandleAndSwitchTo(WebDriver driver, String webHandle, String mainWindow) {
        driver.switchTo().window(webHandle);
        driver.close();
        driver.switchTo().window(mainWindow);
    }

}
