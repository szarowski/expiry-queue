package com.expiryqueue.controller;

import com.expiryqueue.model.DataJson;
import com.expiryqueue.model.FullDataJson;
import com.expiryqueue.model.MessageJson;
import com.expiryqueue.service.ExpiryQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/expiry-queue")
public class ExpiryQueueController {

    private static final Logger LOG = LoggerFactory.getLogger(ExpiryQueueController.class);

    private final ExpiryQueueService expiryQueueService;

    @Autowired
    public ExpiryQueueController(final ExpiryQueueService expiryQueueService) {
        this.expiryQueueService = expiryQueueService;
    }

    @PostMapping(value = "/write")
    @ResponseStatus(HttpStatus.CREATED)
    public DataJson writeMessage(@Valid @RequestBody MessageJson message) {
        final DataJson data = expiryQueueService.writeAndStore(message);
        LOG.info("Message is stored");
        return data;
    }

    @GetMapping(value = "/read")
    @ResponseStatus(HttpStatus.OK)
    public List<DataJson> readRemainingMessages() {
        LOG.info("Retrieving remaining messages");
        return expiryQueueService.getRemainingMessages();
    }

    @GetMapping(value = "/log")
    @ResponseStatus(HttpStatus.OK)
    public List<FullDataJson> readAllDataFromQueue() {
        LOG.info("Retrieving history of all messages");
        return expiryQueueService.getAllMessages();
    }
}