/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import com.fedex.scm.DefaultSetting;
import com.fedex.scm.Feature;
import com.fedex.scm.Option;
import com.fedex.scm.RuntimeContextVariable;
import com.tibco.silverfabric.model.Archive;

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
public abstract class Components extends AbstractSilverFabricMojo {
	@Parameter(defaultValue = "names")
	private String info;
	@Parameter
	private String type;
	@Parameter
	private long engineId;
	@Parameter
	private String instance;

	@Parameter
	private String componentType;
	@Parameter
	private String componentName;
	@Parameter
	private String enablerName;
	@Parameter
	private String enablerVersion;
	@Parameter
	private String description;
	@Parameter
	private List<String> trackedStatistics;
	@Parameter
	private LinkedList<Option> options;
	@Parameter
	private LinkedList<RuntimeContextVariable> runtimeContextVariables;
	@Parameter
	private LinkedList<Feature> features;
	@Parameter
	private List<Archive> archives;
	@Parameter
	private List<DefaultAllocationSetting> defaultAllocationRuleSettings;
	@Parameter
	private List<DefaultSetting> defaultSettings;
	@Parameter
	private List<String> allocationConstraints;
	@Parameter
	private String accountName;
	@Parameter
	private Archive configFile;
	@Parameter
	private List<Archive> contentFiles;
	@Parameter
	private String contentFileRegex;
	@Parameter
	private List<String> relativeURLs;
	@Parameter
	private Archive scriptFile;
	@Parameter
	private String scriptLang;
	@Parameter
	private String scriptLangVersion;
	@Parameter
	private String scriptName;
	@Parameter
	private String scriptFileRegex;

	public abstract void executeMojo() throws MojoExecutionException,
			MojoFailureException;

	protected HashMap<Object, Object> setComponentRequest() {
		if (componentType == null || enablerName == null
				|| enablerVersion == null)
			return null;
		HashMap<Object, Object> request = new LinkedHashMap<Object, Object>();
		request.put("componentType", componentType);
		request.put("name", componentName);
		request.put("enablerName", enablerName);
		request.put("enablerVersion", enablerVersion);
		if (description != null)
			request.put("description", description);
		if (trackedStatistics != null)
			request.put("trackedStatistics", trackedStatistics);
		if (options != null)
			request.put("options", options);
		if (runtimeContextVariables != null)
			request.put("runtimeContextVariables", runtimeContextVariables);
		if (features != null)
			request.put("features", features);
		if (defaultAllocationRuleSettings != null)
			request.put("defaultAllocationRuleSettings",
					defaultAllocationRuleSettings);
		if (defaultSettings != null)
			request.put("defaultSettings", defaultSettings);
		if (allocationConstraints != null)
			request.put("allocationConstraints", allocationConstraints);

		return request;
	}
}