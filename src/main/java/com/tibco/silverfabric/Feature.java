package com.tibco.silverfabric;

import java.util.LinkedList;
import java.util.List;

/**
 * User: franck
 * Date: 1/24/13
 */
public class Feature {
    private String name;
    private List<Property> properties;
    private String description = "";
    private String infoDescription = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInfoDescription() {
        return infoDescription;
    }

    public void setInfoDescription(String infoDescription) {
        this.infoDescription = infoDescription;
    }
}
