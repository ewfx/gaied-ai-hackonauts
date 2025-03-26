package com.hackonauts.hackonauts.dto;

public class GenerateRequest {
    private String prompt; // The input text prompt
    private String model;  // The model to be used (e.g., "Llama")
    private int maxTokens; // Maximum tokens for the response
    private double temperature; // The creativity/temperature parameter

    private boolean stream;

    public boolean isStream() {
        return stream;
    }
    public void setStream(boolean stream) {
        this.stream = stream;
    }

    // Getters and Setters
    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}