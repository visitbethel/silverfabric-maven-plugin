/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric.components;

import java.util.LinkedList;
import java.util.Properties;

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
@Mojo(name = "delete-components-json")
public class DeleteComponentsJSON extends AbstractSilverJSONComponents {

	public DeleteComponentsJSON() {
		super();
	}
	public DeleteComponentsJSON(Plan plan) {
		super();
		this.plan = plan;
	}
	public DeleteComponentsJSON(BrokerConfig config, Plan plan) {
		super();
		this.plan = plan;
		this.setBrokerConfig(config);
	}
	public DeleteComponentsJSON(BrokerConfig brokerConfig, Plan plan,
			String component) {
		this(brokerConfig,plan,component,new Properties());
	}
	public DeleteComponentsJSON(BrokerConfig brokerConfig, Plan plan,
			String name, Properties filters) {
		super();
		this.componentName = name;
		this.plan = plan;
		this.componentProperties = filters;
		this.setBrokerConfig(brokerConfig);
	}
	/**
	 * 
	 */
	public void initialize() throws MojoFailureException {
		super.initialize();
		if (getActions() == null) {
			LinkedList<String> list = new LinkedList<String>();
			list.add("unpublish");
			list.add("delete");
			setActions(list);
		}
		getLog().info("assign action " + getActions());
	}

}