/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric;

import java.io.File;

public class Archive {
    private String path;
    private String name;
    private String relativePath = "";

    
    
    public Archive() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Archive(File f) {
    	this( f, "");
	}
	
	public Archive(File f, String relativePath) {
		this.relativePath = relativePath;
    	this.path = f.getParent();
    	this.name = f.getName();
	}
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
