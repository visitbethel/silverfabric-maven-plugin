/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import com.fedex.scm.Policy;
import com.fedex.scm.PropertyOverride;

/**
 * Actions related to stacks.
 *
 * @get: Queries the stacks for names, all info, or blacklisted_names. This is
 *       the default action if action is not set. * The parameters you can use
 *       in that case are: * info (names, all, blacklisted_names) * type name of
 *       type (i.e.: J2EE, "TIBCO Administrator) * engineId (only if
 *       info=blacklisted_names) * instance (only if info=blacklisted_names)
 */
public abstract class Stacks extends AbstractSilverFabricMojo {
	@Parameter
	private List<String> components;
	@Parameter
	private String stackName;
	@Parameter
	private String mode;
	@Parameter(defaultValue = "--")
	private String templateLevel;
	@Parameter
	private List<Policy> policies;
	@Parameter
	private String accountName;
	@Parameter
	private String runMode;
	@Parameter
	private List<PropertyOverride> propertyOverrides;
	@Parameter
	private String owner;
	@Parameter
	private String technology;
	@Parameter(defaultValue = "/livecluster/admin/images/icons/stackIcons/defaults/6_Skyway_Generic_Default_Icon.png")
	private String icon;
	@Parameter(defaultValue = "Default Template Description.")
	private String description;
	@Parameter
	private List<Map> urls;

	public abstract void executeMojo() throws MojoExecutionException,
			MojoFailureException;

	private HashMap<Object, Object> setStackRequest() {
		if (policies == null || policies.isEmpty() || components == null
				|| components.isEmpty())
			return null;
		HashMap<Object, Object> request = new LinkedHashMap<Object, Object>();
		request.put("name", stackName);
		request.put("policies", policies);
		request.put("components", components);
		request.put("icon", icon);
		request.put("description", description);

		if (owner != null)
			request.put("owner", owner);
		if (mode != null)
			request.put("mode", mode);
		if (templateLevel != null)
			request.put("templateLevel", templateLevel);
		if (propertyOverrides != null)
			request.put("propertyOverrides", propertyOverrides);
		if (technology != null)
			request.put("technology", technology);
		if (urls != null)
			request.put("urls", urls);

		return request;
	}
}