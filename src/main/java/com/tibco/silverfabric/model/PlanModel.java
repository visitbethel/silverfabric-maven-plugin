package com.tibco.silverfabric.model;

import java.util.List;

import org.apache.maven.model.Dependency;

public class PlanModel {

	public String name;
	public List<Model> models;
	public List<Dependency> dependencies;
	public List<Archive> archives;
	
	
	public PlanModel() {
	}

}
