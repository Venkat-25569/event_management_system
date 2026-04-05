 package com.events.event_management.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TestController {
    
    @GetMapping("/test")
    public String test() {
        return "✅ Event Management System is running successfully!";
    }
    
    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "World") String name) {
        return "Hello, " + name + "!";
    }
}