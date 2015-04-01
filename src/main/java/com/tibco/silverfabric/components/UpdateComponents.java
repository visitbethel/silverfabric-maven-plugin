/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric.components;

import java.util.LinkedList;

import org.apache.maven.plugins.annotations.Mojo;

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
@Mojo(name = "create-components")
public class UpdateComponents extends AbstractSilverComponents {

	public UpdateComponents() {
		super();
	}
	public void initialize() {
		super.initialize();
		if (getActions() == null) {
			LinkedList<String> list = new LinkedList<String>();
			list.add("create");
			list.add("publish");
			setActions(list);
		}
		getLog().info("assign action " + getActions());
	}

	

}