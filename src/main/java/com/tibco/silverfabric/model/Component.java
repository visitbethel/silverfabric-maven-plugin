package com.tibco.silverfabric.model;

import java.util.Properties;

import com.fedex.scm.Components;

public class Component extends Components {

	public Properties properties;
	
	public Component() {
		super();
	}

	public Component(String name) {
		super();
		setName(name);
	}

	/**
	 * @return the properties
	 */
	protected final Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	protected final void setProperties(Properties properties) {
		this.properties = properties;
	}




}
