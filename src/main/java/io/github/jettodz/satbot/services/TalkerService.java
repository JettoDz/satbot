package io.github.jettodz.satbot.services;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.jettodz.satbot.util.FileDeleter;
import io.github.jettodz.satbot.util.Logging;
import io.github.jettodz.satbot.util.SatbotProperties;

/**
 * Clase base para los automatas que controlaran un navegador.
 * Incluye los metodos para descargar una a una las facturas y para bajar un paquete zip. y
 * incluye la mayoria de la logica para generar un nuevo TalkerService usando otro
 * driver.
 * 
 * @author Fernando
 *
 * @param <T> - Subclase de RemoteWebDriver, como ChromeWebDriver, ChromiuimWebDriver, FirefoxWebDriver, etc...
 */
public abstract class TalkerService<T extends RemoteWebDriver> implements Logging {
	
	protected SatbotProperties props;
	
	public TalkerService(SatbotProperties props) {
		this.props = props;
	}
	
	/**
	 * Retorna un nuevo T configurado para trabajar segun lo requerido con las opciones
	 * deseadas. Dichas opciones son propias de Selenium.
	 * @param folio - Sufijo para identificar cada llamada a Satbot, que se usara como
	 * nombre para una carpeta nueva dentro de {@link SatbotProperties#getDlFolder()}. Opcional, pero
	 * altamente recomendado para control tanto en Cliente como en Servidor.
	 * @return u RemoteWebDriver especializado y adecuado a la necesidad.
	 * 
	 * @see #operationDir(String)
	 */
	protected abstract T supplyDriver (String folio);
	
	/**
	 * Concatena folio y el directorio de descargas obtenido del {@code satbot.properties#downloads.foler}
	 * @param folio - UUID en string de la operacion. Puede ser opcional.
	 * @return Path del directorio de descargas para una llamada a Satbot
	 */
	protected Path operationDir(String folio) {
		return Paths.get(props.getDlFolder() + (Objects.isNull(folio) ? "" : folio));
	}
	
	/**
	 * Para todas las facturas disponibles tras filtrar por mes y año, se hace click en el boton
	 * de descarga y se almacenan las mismas en {@link #operationDir(String)} 
	 * @param operation - UUID, preferentemente {@link UUID#randomUUID()} para esta operacion
	 * @param year - Año a buscar
	 * @param month - Mes a buscar
	 * @since 0.0.1
	 */
	public abstract void oneByOne(UUID operation, String year, String month);

	/**
	 * Implementacion real del metodo {@link #oneByOne(UUID, String, String)}.
	 * El flujo es: Login, Filtro y Descargada, definidos por los metodos {@link #fielLogin(RemoteWebDriver, char[])},
	 * {@link #requestForDate(RemoteWebDriver, String, String)} y {@link #multipleDownloads(RemoteWebDriver, String)}, 
	 * respectivamente. Al finalizar esto, cierra sesion y mata el proceso del WebDriver.
	 * @param operation - UUID, preferentemente {@link UUID#randomUUID()} para esta operacion
	 * @param examplePassword - Contraseña para el certificado y llave privada
	 * @param year - Año a buscar
	 * @param month - Mes a buscar
	 * @since 0.0.1
	 */
    protected void oneByOne(UUID operation, char[] examplePassword, String year, String month) {
    	T driver = supplyDriver(operation.toString());
        try {
        	String mainWindow = fielLogin(driver, Objects.isNull(examplePassword) ? props.getFallbackPassword().toCharArray() : examplePassword);
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

    /**
     * Descarga las facturas una a una. Esto resulta en multiples XML en el directorio de descargas en la carpeta del UUID
     * @param operation - UUID de generado por el cliente para identificar esta peticion
     * @param year - Año solicitado para descarga de facturas
     * @param month - Mes solicitado para la descarga de facutras
     */
    public abstract void asZip(UUID operation, String year, String month);
    
    /**
     * Implementacion real de {@link #asZip(UUID, String, String)}
     * @param operation - UUID de generado por el cliente para identificar esta peticion
     * @param examplePassword - Contraseña para el certificado y llave privada
     * @param year - Año solicitado para descarga de facturas
     * @param month - Mes solicitado para la descarga de facutras
     */
    protected void asZip(UUID operation, char[] examplePassword, String year, String month) {
    	T driver = supplyDriver(operation.toString());
        try {
            fielLogin(driver, Objects.isNull(examplePassword) ? props.getFallbackPassword().toCharArray() : examplePassword);
            logInfo("Acceso exitoso");
            requestForDate(driver, year, month);
            singleZipDownload(driver);
        } catch (WebDriverException | InterruptedException e) { // No importa que error sea, siempre hay que cerrar sesion y driver
        	logInfo(e.getClass().getName());
            logError(e);
        } finally {
            try {
            	logInfo("Intentando cerrar sesion...");
                driver.executeScript("document.getElementById('anchorClose').click()");
            } catch (WebDriverException e2) { // En caso de que el error sea interno o no haya llegado a ingresar
                logError(e2);
            }
            driver.quit();
            logInfo("Operacion finalizada");
        }
    }

    /**
     * Busca y limpia el directorio identificado por el UUID proporcionado
     * @param folio - UUID, como String, identificador de la operacion de descarga
     * @throws IOException - En caso de que no encuentre el directorio o no haya sido posible limpiar.
     */
    public void clear(String folio) throws IOException {
    	logInfo("Limpiando registros de @" + folio);
    	Files.walkFileTree(Paths.get(props.getDlFolder(), folio).toAbsolutePath(), new FileDeleter());
    }

    /**
     * Accede al portal del SAT
     * @param driver WebDriver usado para la sesion
     * @param password Contrasena de la clave FIEL
     * @return webHandler de la ventana principal, para posterior utilidad.
     * @throws InterruptedException - En caso de que algun hilo muera en los delays
     */
    protected String fielLogin(T driver, @Nonnull char[] password) throws InterruptedException {
        logInfo("Accediento a portalcfdi.facturaelectronica.sat.gob.mx");
        driver.get("https://portalcfdi.facturaelectronica.sat.gob.mx");
        String mainWindow = driver.getWindowHandle();
        Thread.sleep(100);
        driver.findElement(By.id("buttonFiel")).click();
        logDebug("Accediendo. Se usan el certificado y la llave privada");
        Thread.sleep(100);
        //estos paths son para efectos de pruebas. NUNCA ALMACENAR ARCHIVOS NI CONTRASEÑAS DE CLAVE FIEL
        driver.findElement(By.id("fileCertificate")).sendKeys(Paths.get("/tmp/cer.cer").toAbsolutePath().toString());
        driver.findElement(By.id("filePrivateKey")).sendKeys(Paths.get("/tmp/private.key").toAbsolutePath().toString());
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
    protected void requestForDate(T driver, String year, String month) {
        driver.findElement(By.linkText("Consultar Facturas Recibidas")).click();

        driver.findElement(By.id("ctl00_MainContent_RdoFechas")).click();

        new WebDriverWait(driver, Duration.ofSeconds(20)).ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.elementToBeClickable(By.id("DdlAnio")));
        new Select(driver.findElement(By.id("DdlAnio"))).selectByVisibleText(year);

        new WebDriverWait(driver, Duration.ofSeconds(20)).ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_CldFecha_DdlMes")));
        new Select(driver.findElement(By.id("ctl00_MainContent_CldFecha_DdlMes"))).selectByValue(month);

        driver.findElement(By.id("ctl00_MainContent_BtnBusqueda")).click();
    }

    /**
     * Descarga una a una las facturas encontradas por la peticion segun la fecha
     * @param driver - WebDriver de la sesion
     * @param mainWindow - WebHandler de la ventana principal
     * @throws InterruptedException - En caso de que algun hilo muera en los delays
     */
    protected void multipleDownloads(T driver, String mainWindow) throws InterruptedException {
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.presenceOfElementLocated(By.id("seleccionador")));
        logInfo("Descargando facturas individualmente");
        Thread.sleep(200);
        driver.findElements(By.id("BtnDescarga")).forEach(elem -> {
            try {
                new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.elementToBeClickable(elem)).click();
                Thread.sleep(500); // No es lo mas limpio ni mucho menos, pero es sorprendentemente robusto
                driver.getWindowHandles().stream()
                        .distinct()
                        .filter(wh -> !wh.equals(mainWindow))
                        .forEach(wh -> closeHandleAndSwitchTo(driver, wh, mainWindow));
            } catch (InterruptedException e) {
                logError(e);
            }
        });
        Thread.sleep(200);
		logInfo("Proceso de descarga finalizado");
    }

    /**
     * Al intentar descarga una factura de forma individual, el portal lanza una ventana emergente por cada archivo.
     * Este metodo accese/usa la ventana emergente, la cierra y regresa a la principal.
     * @param driver - WebDriver de la sesion
     * @param webHandle - WebHandle de la ventana emergente
     * @param mainWindow - WebHandle de la ventana principal
     */
    protected void closeHandleAndSwitchTo(T driver, String webHandle, String mainWindow) {
        driver.switchTo().window(webHandle);
        driver.close();
        driver.switchTo().window(mainWindow);
    }
    
    /**
     * Selecciona el checkbox principal de ls tabla y descarga los seleccionados, resultando en un zip del mes 
     * seleccionado con UUID generado por el SAT 
     * @param driver - WebDriver de la sesion
     * @throws InterruptedException - En caso de que algun hilo muera en los delays
     */
    protected void singleZipDownload(T driver) throws InterruptedException {
    	new WebDriverWait(driver, Duration.ofSeconds(20))
		        .until(ExpectedConditions.elementToBeClickable(By.id("seleccionador"))).click();
    	Thread.sleep(500);
		new WebDriverWait(driver, Duration.ofSeconds(20))
				.until(ExpectedConditions.elementToBeClickable(By.id("ctl00_MainContent_BtnDescargar"))).click();
		logInfo("Solicitud para descarga multiple correcta");
		Thread.sleep(Duration.ofSeconds(5).toMillis());
		String successMsg = new WebDriverWait(driver, Duration.ofSeconds(60))
		        .until(ExpectedConditions.presenceOfElementLocated(By.id("dvAlert")))
		        .findElement(By.className("alert-success"))
		        .getText();
		String requestUuid = successMsg.substring(successMsg.indexOf(":") + 1, successMsg.indexOf(",")).trim();
		logInfo("Se recupero UUID de solicitud correctamente");
		Thread.sleep(Duration.ofSeconds(30).toMillis()); // El tiempo para que el paquete este listo es muy volatil. Esto da algo de chance
		driver.executeScript("document.location.href='../Consulta.aspx'");
		Thread.sleep(500);
		new WebDriverWait(driver, Duration.ofSeconds(20))
				.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Recuperar Descargas de CFDI"))).click();
		List<WebElement> rows = new WebDriverWait(driver, Duration.ofSeconds(20))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_GridViewReporte")))
				.findElements(By.tagName("tr"));
		logInfo("Buscando UUID de solicitud para descaragar");
		Thread.sleep(100);
		Optional<WebElement> request = rows.subList(1, rows.size()).stream()
										   .filter(we -> we.findElement(By.xpath(".//td[2]")).getText().equals(requestUuid))
										   .findFirst();
		if (request.isPresent()) 
			request.get().findElement(By.id("BtnDescarga")).click();
		else 
			logInfo("Paquete aun en proceso. UUID del paquete: " + requestUuid);
		Thread.sleep(200);
		logInfo("Proceso de descarga finalizado");
    }

}
