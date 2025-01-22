package com.example.wildtide.lockey;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class ControllerB {
    @GetMapping("")
    public ResponseEntity<Void> redirectToNewUrl() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/landing.html"); // Set the new URL to redirect to
        return new ResponseEntity<>(headers, HttpStatus.FOUND); // Return 302 Found status
    }
}
