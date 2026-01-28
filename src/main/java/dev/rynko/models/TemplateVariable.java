package dev.rynko.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Template variable definition.
 */
public class TemplateVariable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("required")
    private boolean required;

    @JsonProperty("defaultValue")
    private Object defaultValue;

    public TemplateVariable() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}
