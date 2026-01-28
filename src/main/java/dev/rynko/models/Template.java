package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Template model.
 */
public class Template {

    @JsonProperty("id")
    private String id;

    @JsonProperty("shortId")
    private String shortId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("type")
    private String type;

    @JsonProperty("outputFormats")
    private List<String> outputFormats;

    @JsonProperty("variables")
    private List<TemplateVariable> variables;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("updatedAt")
    private Instant updatedAt;

    public Template() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getOutputFormats() {
        return outputFormats;
    }

    public void setOutputFormats(List<String> outputFormats) {
        this.outputFormats = outputFormats;
    }

    public List<TemplateVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<TemplateVariable> variables) {
        this.variables = variables;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
