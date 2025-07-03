package com.example.demo;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/public")
    public String publicHello() {
        return "Hello Public!";
    }

    @GetMapping("/private")
    public String privateHello(Principal principal) {
        return "Hello Private, " + principal.getName();
    }

    @GetMapping("/admin")
    public ResponseEntity<String> admin() {
        return ResponseEntity.ok("Admin content");
    }

}
