/**
 * 
 */
package com.tibco.silverfabric.model;

import java.util.Map;

/**
 * @author akaan
 *
 */
public class Plan {

	public String componentTemplateURI;
	public String stackTemplateURI;
	public String level;
	public String name;
	public String type;
	public Map<String, String> properties;

	/**
	 * 
	 */
	public Plan() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the componentTemplateURI
	 */
	protected final String getComponentTemplateURI() {
		return componentTemplateURI;
	}

	/**
	 * @param componentTemplateURI the componentTemplateURI to set
	 */
	protected final void setComponentTemplateURI(String componentTemplateURI) {
		this.componentTemplateURI = componentTemplateURI;
	}

	/**
	 * @return the stackTemplateURI
	 */
	protected final String getStackTemplateURI() {
		return stackTemplateURI;
	}

	/**
	 * @param stackTemplateURI the stackTemplateURI to set
	 */
	protected final void setStackTemplateURI(String stackTemplateURI) {
		this.stackTemplateURI = stackTemplateURI;
	}

	/**
	 * @return the level
	 */
	protected final String getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	protected final void setLevel(String level) {
		this.level = level;
	}

	/**
	 * @return the name
	 */
	protected final String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	protected final void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	protected final String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	protected final void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the properties
	 */
	protected final Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	protected final void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Plan [componentTemplateURI=" + componentTemplateURI
				+ ", stackTemplateURI=" + stackTemplateURI + ", level=" + level
				+ ", name=" + name + ", type=" + type + ", properties="
				+ properties + "]";
	}


}
