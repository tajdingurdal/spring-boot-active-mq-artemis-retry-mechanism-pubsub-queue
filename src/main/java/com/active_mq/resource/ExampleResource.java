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

    @GetMapping("/queue-publish-message")
    public ResponseEntity<String> queuePublishMessage(@RequestParam final String msg) {
        try {
            exampleMessageService.queuePublishMessage(msg);
            return new ResponseEntity<>("Sent.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/topic-publish-message")
    public ResponseEntity<String> topicPublishMessage(@RequestParam final String msg) {
        try {
            exampleMessageService.topicPublishMessage(msg);
            return new ResponseEntity<>("Sent.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
