package io.github.jettodz.satbot.controllers;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.jettodz.satbot.services.TalkerService;
import io.github.jettodz.satbot.util.Logging;

@RestController
public class TalkerController implements Logging {
    
    @Autowired
    private TalkerService<ChromeDriver> chrome;
    @Autowired
    private TalkerService<FirefoxDriver> firefox;

    @GetMapping("/{browser}/oneByOne")
    public ResponseEntity<String> oneByOne(@PathVariable String browser, @RequestParam String year, @RequestParam String month) {
    	if (Objects.isNull(month) || Objects.isNull(year)) return ResponseEntity.badRequest().build();
        UUID folio = UUID.randomUUID();
        if (isChrome(browser)) chrome.oneByOne(folio, year, month);
        else firefox.oneByOne(folio, year, month);
        return ResponseEntity.ok().body("ok! Ver recursos @" + folio);
    }

    @GetMapping("/{browser}/zip")
    public ResponseEntity<String> asZip(@PathVariable String browser, @RequestParam String year, @RequestParam String month) {
    	if (Objects.isNull(month) || Objects.isNull(year)) return ResponseEntity.badRequest().build();
        UUID folio = UUID.randomUUID();
        if (isChrome(browser)) chrome.asZip(folio, year, month);
        else firefox.asZip(folio, year, month);
        return ResponseEntity.ok().body("ok!");
    }

    @GetMapping("/clear")
    public ResponseEntity<String> clear(@RequestParam String folio){
    	if (Objects.isNull(folio)) return ResponseEntity.badRequest().build();
        try{
            chrome.clear(folio); 
            return ResponseEntity.ok().body("Recursos limpiados @" + folio);
        } catch (IOException e) {
        	logError(e);
            return ResponseEntity.internalServerError().body("Error al limpiar recursos");
        }
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<String> missingStringParam(MissingServletRequestParameterException e) {
		return ResponseEntity.badRequest().build();
	}
    
    private boolean isChrome(String broswer) {
    	return "chrome".equals(broswer);
    }

}
