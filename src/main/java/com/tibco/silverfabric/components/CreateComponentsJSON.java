/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric.components;

import java.util.LinkedList;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.tibco.silverfabric.BrokerConfig;
import com.tibco.silverfabric.model.Plan;

/**
 * Actions related to components.
 *
 * @get: Queries the Components for names, all info, or blacklisted_names. This
 *       is the default action if action is not set. * The parameters you can
 *       use in that case are: * info (names, all, blacklisted_names) * type
 *       name of type (i.e.: J2EE, "TIBCO ActiveMatrix BusinessWorks:2.0.0") *
 *       engineId (only if info=blacklisted_names) * instance (only if
 *       info=blacklisted_names)
 */
@Mojo(name = "create-components-json")
public class CreateComponentsJSON extends AbstractSilverJSONComponents {

	public CreateComponentsJSON() {
		super();
	}
	
	public CreateComponentsJSON(BrokerConfig config, Plan plan) {
		super();
		this.plan = plan;
		setBrokerConfig(config);
	}


	public void initialize() throws MojoFailureException {
		super.initialize();
		getLog().info("initializing component " + this.toString());
		if (getActions() == null) {
			LinkedList<String> list = new LinkedList<String>();
			list.add("create");
			if (this.getConfigFile() != null) {
				list.add("update config file");
			}
			if (this.getContentFiles() != null && this.getContentFiles().size() > 0) {
				list.add("add content file");
			}
			if (this.getArchives() != null && this.getArchives().size() > 0) {
				list.add("add archives");
			}
			if (this.getScriptFile() != null) {
				list.add("add script-files");
			}
			list.add("publish");
			setActions(list);
		}
		getLog().info("assign action " + getActions());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CreateComponentsJSON [getComponentType()=" + getComponentType()
				+ ", getComponentName()=" + getComponentName()
				+ ", getEnablerName()=" + getEnablerName()
				+ ", getEnablerVersion()=" + getEnablerVersion()
				+ ", getArchives()=" + getArchives() + ", getConfigFile()="
				+ getConfigFile() + ", getContentFiles()=" + getContentFiles()
				+ ", getScriptFile()=" + getScriptFile() + ", getScriptLang()="
				+ getScriptLang() + ", getScriptLangVersion()="
				+ getScriptLangVersion() + ", getScriptName()="
				+ getScriptName() + "]";
	}

	
}