package com.hackonauts.hackonauts.dto;

public class GenerateResponse {
    private String status; // API response status (e.g., "success", "error")
    private String response; // The generated response content

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}