package com.vieguys.eventservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> body = new HashMap<>();
        body.put("service", "event-service");
        body.put("status", "ok");
        body.put("time", LocalDateTime.now());

        return ResponseEntity.ok(body);
    }
}