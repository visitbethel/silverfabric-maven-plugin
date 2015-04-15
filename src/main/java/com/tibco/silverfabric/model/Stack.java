package com.tibco.silverfabric.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.fedex.scm.Stacks;

public class Stack extends Stacks {

	public Properties properties = new Properties();
	public List<ComponentDependency> componentDependencies = new LinkedList<ComponentDependency>();
	public List<Component> components = new LinkedList<Component>();

	public Stack() {
		setMode("--");
		setIcon("/livecluster/admin/images/icons/stackIcons/defaults/6_Skyway_Generic_Default_Icon.png");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Stack [properties=" + properties + ", componentDependencies="
				+ componentDependencies + ", components=" + components
				+ ", getName()=" + getName() + ", getDisplayName()="
				+ getDisplayName() + ", getOwner()=" + getOwner()
				+ ", getLastModified()=" + getLastModified()
				+ ", getDescription()=" + getDescription() + ", getIcon()="
				+ getIcon() + ", getComponents()=" + getComponents()
				+ ", getModified()=" + getModified() + ", getModifiedBy()="
				+ getModifiedBy() + ", getPolicies()=" + getPolicies()
				+ ", getMode()=" + getMode() + ", getStartOn()=" + getStartOn()
				+ ", getCapturedOn()=" + getCapturedOn() + ", getExportedOn()="
				+ getExportedOn() + ", getTechnology()=" + getTechnology()
				+ ", getUrls()=" + getUrls() + ", getPropertyOverrides()="
				+ getPropertyOverrides() + ", getTemplateLevel()="
				+ getTemplateLevel() + ", getAdditionalProperties()="
				+ getAdditionalProperties() + "]";
	}

}
