package com.tibco.silverfabric;

import java.util.List;

/**
 * User: franck
 * Date: 1/24/13
 */
public class Condition {
    private List<Property> properties;
    private String type;
    private String description;

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
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
}
