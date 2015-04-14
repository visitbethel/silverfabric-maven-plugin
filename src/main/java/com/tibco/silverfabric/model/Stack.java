package com.tibco.silverfabric.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fedex.scm.Policy;
import com.fedex.scm.PropertyOverride;

public class Stack {

	public Properties properties = new Properties();
	public String name;
	public String mode = "--";
	public String templateLevel;
	public List<Policy> policies;
	public String accountName;
	public String runMode;
	public List<PropertyOverride> propertyOverrides;
	public String owner;
	public String technology;
	public String icon = "/livecluster/admin/images/icons/stackIcons/defaults/6_Skyway_Generic_Default_Icon.png";
	public String description = "Default Template Description.";
	public List<Map> urls;
	public List<ComponentDependency> componentDependencies = new LinkedList<ComponentDependency>();
	public List<Component> components = new LinkedList<Component>();

	public Stack() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Stack [properties=" + properties + ", stackName=" + name
				+ ", mode=" + mode + ", templateLevel=" + templateLevel
				+ ", policies=" + policies + ", accountName=" + accountName
				+ ", runMode=" + runMode + ", propertyOverrides="
				+ propertyOverrides + ", owner=" + owner + ", technology="
				+ technology + ", icon=" + icon + ", description="
				+ description + ", urls=" + urls + ", componentDependencies="
				+ componentDependencies + ", components=" + components + "]";
	}

}
