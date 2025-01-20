package com.active_mq.resource;

import com.active_mq.service.ExampleMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleResource {

    private final ExampleMessageService exampleMessageService;

    public ExampleResource(ExampleMessageService exampleMessageService) {
        this.exampleMessageService = exampleMessageService;
    }

    @GetMapping("/publish-message")
    public ResponseEntity<String> publishMessage(@RequestParam final String msg) {
        try {
            exampleMessageService.publishMessage(msg);
            return new ResponseEntity<>("Sent.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
