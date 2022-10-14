package mx.com.bmf.satbot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import mx.com.bmf.services.TalkerService;

@RestController
public class TalkerController {
    
    @Autowired
    private TalkerService service;

    @GetMapping("/testProps")
    public ResponseEntity<String> testProps() {
        return ResponseEntity.ok(service.testProp());
    }

    @GetMapping("/dl")
    public ResponseEntity<String> download() {
        service.test();
        return ResponseEntity.ok().body("ok!");
    }

}
