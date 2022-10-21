package mx.com.bmf.satbot.controllers;

import java.io.IOException;
import java.util.UUID;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mx.com.bmf.satbot.services.TalkerService;

@RestController
public class TalkerController {
    
    @Autowired
    private TalkerService<ChromeDriver> chrome;
    @Autowired
    private TalkerService<FirefoxDriver> firefox;

    @GetMapping("/{browser}/oneByOne")
    public ResponseEntity<String> oneByOne(@PathVariable String browser, @RequestParam String year, @RequestParam String month) {
        UUID folio = UUID.randomUUID();
        if (isChrome(browser)) chrome.oneByOne(folio, year, month);
        else firefox.oneByOne(folio, year, month);
        return ResponseEntity.ok().body("ok! Ver recursos @" + folio);
    }

    @GetMapping("/{browser}/zip")
    public ResponseEntity<String> asZip(@PathVariable String browser, @RequestParam String year, @RequestParam String month) {
        UUID folio = UUID.randomUUID();
        if (isChrome(browser)) chrome.asZip(folio, year, month);
        else firefox.asZip(folio, year, month);
        return ResponseEntity.ok().body("ok!");
    }

    @GetMapping("/clear")
    public ResponseEntity<String> clear(@PathVariable String browser, @RequestParam String folio){
        try{
            chrome.clear(folio); 
            return ResponseEntity.ok().body("Recursos limpieados @" + folio);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return ResponseEntity.internalServerError().body("Error al limpiar recursos");
        }
    }
    
    private boolean isChrome(String broswer) {
    	return "chrome".equals(broswer);
    }

}
