package com.hackonauts.hackonauts.controller;

import com.hackonauts.hackonauts.dto.ContentTestRequest;
import com.hackonauts.hackonauts.dto.Email;
import com.hackonauts.hackonauts.service.EmailProcessingService;
import com.hackonauts.hackonauts.service.LocalEmailReaderService;
import com.hackonauts.hackonauts.service.OllamaCategorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EmailController {

    @Autowired
    private EmailProcessingService emailProcessingService;

    @Autowired
    private LocalEmailReaderService localEmailReaderService;

    @Autowired
    OllamaCategorizationService ollamaCategorizationService;




    @GetMapping("/process-emails")
    public String processEmails() {
        try {
            emailProcessingService.fetchEmails();
            return "Emails processed successfully.";
        } catch (Exception e) {
            return "Error processing emails: " + e.getMessage();
        }
    }


    @GetMapping("/local-email")
    public List<Email> processLocalEmail() throws Exception {
        return localEmailReaderService.readEmailsFromLocalFolder("C:\\Users\\bommu\\WORK\\repo\\hackonauts\\sample-emails");
    }

    @PostMapping("/content-test")
    public Object processLocalEmail(@RequestBody ContentTestRequest request) throws Exception {
        return ollamaCategorizationService.analyzeEmailWithOllama(request.getContent());
    }


}