package com.tibco.silverfabric;

/**
 * User: franck
 * Date: 1/24/13
 */
public class Archive {
    private String path;
    private String name;
    private String relativePath = "";

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
