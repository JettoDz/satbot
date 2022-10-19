package mx.com.bmf.satbot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mx.com.bmf.satbot.services.TalkerService;

import java.io.IOException;
import java.util.UUID;

@RestController
public class TalkerController {
    
    @Autowired
    private TalkerService service;

    @GetMapping("/testProps")
    public ResponseEntity<String> testProps() {
        return ResponseEntity.ok(service.testProp());
    }

    @GetMapping("/oneByOne")
    public ResponseEntity<String> oneByOne(@RequestParam String year, @RequestParam String month) {
        UUID folio = UUID.randomUUID();
        service.oneByOne(folio, year, month);
        return ResponseEntity.ok().body("ok! Ver recursos @" + folio);
    }

    @GetMapping("/zip")
    public ResponseEntity<String> asZip() {
        UUID folio = UUID.randomUUID();
        service.asZip(folio);
        return ResponseEntity.ok().body("ok!");
    }

    @GetMapping("clear")
    public ResponseEntity<String> clear(@RequestParam String folio){
        try{
            service.clear(folio);
            return ResponseEntity.ok().body("Recursos limpieados @" + folio);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return ResponseEntity.internalServerError().body("Error al limpiar recursos");
        }
    }

}
