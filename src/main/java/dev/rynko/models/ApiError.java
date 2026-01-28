package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * API error response.
 */
public class ApiError {

    @JsonProperty("message")
    private String message;

    @JsonProperty("code")
    private String code;

    @JsonProperty("statusCode")
    private int statusCode;

    public ApiError() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
