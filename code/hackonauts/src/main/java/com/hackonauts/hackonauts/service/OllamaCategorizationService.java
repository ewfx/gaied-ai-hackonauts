package com.hackonauts.hackonauts.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackonauts.hackonauts.dto.CategorizationResult;
import com.hackonauts.hackonauts.dto.GenerateRequest;
import com.hackonauts.hackonauts.dto.GenerateResponse;
import com.hackonauts.hackonauts.dto.ServiceRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OllamaCategorizationService {
    public ServiceRequest analyzeEmailWithOllama(String emailContent) throws JsonProcessingException {
        System.out.println("emailContent=" + emailContent);
        String ollamaApiUrl = "http://localhost:11434/api/generate";

        // Prepare headers for the API request
        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer YOUR_OLLAMA_API_KEY");
        headers.set("Content-Type", "application/json");

        // Prepare request payload
//        String requestBody = String.format("{\"content\":\"%s\"}", emailContent);
        GenerateRequest requestBody= new GenerateRequest();
        requestBody.setModel("phi4-mini");
        requestBody.setPrompt("categorize below content and send response in JSON object format with keys [requestType, subType, description, originalContent]. Include key details or important references in the description as plain text. originalContent keep the following text as is." +
                " :" + emailContent);
        requestBody.setStream(false);
        HttpEntity<GenerateRequest> request = new HttpEntity<>(requestBody, headers);

        // Make the API call
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<GenerateResponse> response = restTemplate.exchange(
                ollamaApiUrl,
                HttpMethod.POST,
                request,
                GenerateResponse.class
        );

        GenerateResponse ollamaResponse = response.getBody();


        return extractRequestTypeSubType(ollamaResponse.getResponse());
    }


    private ServiceRequest extractRequestTypeSubType(String ollamaResponseJson) throws JsonProcessingException {
        System.out.println(ollamaResponseJson);
        ollamaResponseJson = ollamaResponseJson.replace("`", "").replace("json", "");
        ObjectMapper objectMapper = new ObjectMapper();
        ServiceRequest  serviceRequest =objectMapper.readValue(ollamaResponseJson, ServiceRequest.class);
        System.out.println("Request Type: " + serviceRequest.getRequestType());
        System.out.println("Sub-Type: " + serviceRequest.getSubType());
        System.out.println("Description: " + serviceRequest.getDescription());
        System.out.println("Original Content: " + serviceRequest.getOriginalContent());

        return serviceRequest;

    }

}
