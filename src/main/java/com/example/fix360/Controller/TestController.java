package com.example.fix360.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    public String test() {
        return "ok";
    }
    
    // @GetMapping("/test/protected")
    // public String protectedTest() {
    //     return "Protected endpoint is working!";

@GetMapping("/test/protected")
@PreAuthorize("hasAnyRole('USER','ADMIN','SERVICE_PROVIDER')")
public String protectedTest() {
    return "Protected endpoint both is working!";
}
    }
