package com.tibco.silverfabric.model;

import java.util.Properties;

public class Component {

	public String name;
	public Properties properties;
	
	public Component() {
		// TODO Auto-generated constructor stub
	}

	public Component(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Component [name=" + name + ", properties=" + properties + "]";
	}


}
