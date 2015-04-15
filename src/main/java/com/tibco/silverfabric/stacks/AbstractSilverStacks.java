/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric.stacks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

import com.tibco.silverfabric.Stacks;
import com.tibco.silverfabric.model.Plan;
import com.tibco.silverfabric.model.Stack;

/**
 * Actions related to stacks.
 *
 * @get: Queries the stacks for names, all info, or blacklisted_names. This is
 *       the default action if action is not set. * The parameters you can use
 *       in that case are: * info (names, all, blacklisted_names) * type name of
 *       type (i.e.: J2EE, "TIBCO Administrator) * engineId (only if
 *       info=blacklisted_names) * instance (only if info=blacklisted_names)
 */
public abstract class AbstractSilverStacks extends Stacks {

	@Parameter
	protected Plan plan = new Plan();
	public Properties stackProperties = new Properties();

	// @Parameter
	// protected List<String> components = new LinkedList<String>();
	// @Parameter
	// protected String stackName;
	// @Parameter
	// protected String mode;
	// @Parameter(defaultValue = "--")
	// protected String templateLevel;
	// @Parameter
	// protected List<Policy> policies;
	// @Parameter
	// protected String accountName;
	// @Parameter
	// protected String runMode;
	// @Parameter
	// protected List<PropertyOverride> propertyOverrides;
	// @Parameter
	// protected String owner;
	// @Parameter
	// protected String technology;
	// @Parameter(defaultValue =
	// "/livecluster/admin/images/icons/stackIcons/defaults/6_Skyway_Generic_Default_Icon.png")
	// protected String icon;
	// @Parameter(defaultValue = "Default Template Description.")
	// protected String description;
	// @SuppressWarnings("rawtypes")
	// @Parameter
	// protected List<Map> urls;
	private File outputDirectory = new File("target");
	private boolean breakout = false;

	/**
     * 
     */
	protected Stack stack;

	/**
	 * 
	 * @throws MojoFailureException
	 */
	public void initialize() throws MojoFailureException {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void executeMojo() throws MojoExecutionException,
			MojoFailureException {

		initialize();

		getLog().info("execute from " + this.getClass());

		if (this.stack == null) {
			throw new MojoExecutionException(
					"Unable to create stack, plan loading failed.");
		}
		if (getActions().contains("create")
				&& (this.stack.getComponents() == null || this.stack
						.getComponents().isEmpty())) {
			throw new MojoExecutionException(
					"The components parameters are required to create a stack");
		}

		String stackName = stack.getName();

		List<String> actionList = getActions() != null ? getActions()
				: new ArrayList<String>();
		if (actionList.isEmpty())
			actionList.add("get");
		String url = getBrokerConfig().getBrokerURL().toString()
				+ "/livecluster/rest/v1/sf/stacks";
		getLog().debug(url);

		for (String action : actionList) {
			if (!action.equals("clean") && !action.equals("get")
					&& !action.equals("get types")
					&& !action.equals("get type names")
					&& (stackName == null || stackName.isEmpty()))
				throw new MojoFailureException(
						"The parameter \"stackName\" is required by the stack action: "
								+ action);
			try {
				if ("create".equals(action)) {
					Map<Object, Object> mapCreate = setStackRequest(stack);
					if (mapCreate == null)
						throw new MojoFailureException(
								"The policies and components parameters are required to create a stack");
					getLog().info(
							restTemplate.postForObject(url, mapCreate,
									String.class).toString());
				} else if ("publish".equals(action)) {
					restTemplate.put(url + "/{stackName}/published/true", null,
							stackName);
					getLog().info(
							">>>>>>>>>> STACK [" + stackName + "] published!");
				} else if ("unpublish".equals(action)) {
					restTemplate.put(url + "/{stackName}/published/false",
							null, stackName);
					getLog().info(
							">>>>>>>>>> STACK [" + stackName + "] unpublished!");
				} else if ("update".equals(action)) {
					Map<Object, Object> mapUpdate = setStackRequest(stack);
					if (mapUpdate == null)
						throw new MojoFailureException(
								"The following parameters are required to "
										+ action
										+ " a component: enablerName, enablerVersion, componentType");
					restTemplate
							.put(url + "/{stackName}", mapUpdate, stackName);
					getLog().info(
							">>>>>>>>>> STACK [" + stackName + "] updated!");
				} else if ("delete".equals(action)) {
					restTemplate.delete(url + "/" + "{stackName}", stackName);
					getLog().info(
							">>>>>>>>>> STACK [" + stackName + "] deleted!");
				} else if ("get info".equals(action)) {
					LinkedHashMap<String, LinkedHashMap<String, Object>> infoLinkedHashMap;
					infoLinkedHashMap = restTemplate.getForObject(url
							+ "/{stackName}", LinkedHashMap.class, stackName);
					getLog().info(
							infoLinkedHashMap.get("result").get("value")
									.toString());
				} else if ("assign to non cloud".equals(action)) {
					Map<String, String> accountNameMap = new HashMap();
					accountNameMap.put("name", "name");
					accountNameMap.put("value", stack.getOwner());
					getLog().info(
							restTemplate.postForObject(
									url + "/{stackName}/assign-to-non-cloud",
									accountNameMap, String.class, stackName)
									.toString());
				} else if ("get config file".equals(action)) {
					HttpHeaders requestHeaders = new HttpHeaders();
					List<MediaType> mediaTypes = new ArrayList();
					mediaTypes.add(MediaType.TEXT_PLAIN);
					requestHeaders.setAccept(mediaTypes);
					HttpEntity<?> requestEntity = new HttpEntity(requestHeaders);
					getLog().info(
							restTemplate.exchange(
									url + "/{stackName}/config-file",
									HttpMethod.GET, requestEntity,
									String.class, stackName).toString());
				} else if ("set run mode".equals(action)) {
					restTemplate.put(url + "/{stackName}/mode/{run-mode}",
							null, stackName, stack.getMode());
					getLog().info("run mode set to " + stack.getMode() + " !");
				} else if ("auto-detect http-urls".equals(action)) {

					restTemplate.postForObject(url
							+ "/{component}/http-urls/auto-detect", null,
							String.class, stackName);
					getLog().info(
							restTemplate.postForObject(
									url + "/{component}/http-urls/auto-detect",
									null, String.class, stackName).toString());
				} else if ("get patches".equals(action)) {
					LinkedHashMap<Object, Object> getPatches = restTemplate
							.getForObject(url + "/{component}/patches",
									LinkedHashMap.class, stackName);
					getLog().info(getPatches.get("result").toString());
				} else if ("get script-files".equals(action)) {
					LinkedHashMap<Object, Object> getScriptFiles = restTemplate
							.getForObject(url + "/{component}/script-files",
									LinkedHashMap.class, stackName);
					getLog().info(getScriptFiles.get("result").toString());
				} else if ("get type names".equals(action)) {
					getLog().info(
							restTemplate
									.getForObject(url + "/type-names",
											LinkedHashMap.class).get("result")
									.toString());
				} else if ("get types".equals(action)) {
					getLog().info(
							restTemplate
									.getForObject(url + "/types",
											LinkedHashMap.class).get("result")
									.toString());
				} else if ("get".equals(action)) {
					getLog().info(
							restTemplate.getForObject(url, LinkedHashMap.class)
									.get("result").toString());
				} else if ("clean".equals(action)) {
					LinkedHashMap<Object, Object> getResponse = restTemplate
							.getForObject(url, LinkedHashMap.class);
					Integer status = (Integer) getResponse.get("status");
					if (status == 200) {
						LinkedHashMap<Object, Object> result = (LinkedHashMap<Object, Object>) getResponse
								.get("result");
						List<LinkedHashMap<String, String>> stacksInfo = (List<LinkedHashMap<String, String>>) result
								.get("value");
						for (LinkedHashMap<String, String> stackInfo : stacksInfo) {
							restTemplate.put(url
									+ "/{stackName}/published/false", null,
									stackInfo.get("name"));
							restTemplate.delete(url + "/" + "{stackName}",
									stackInfo.get("name"));
							getLog().warn(stackInfo.get("name") + "deleted!");
						}
					} else {
						getLog().warn("Status = " + status);
						getLog().warn(getResponse.get("result").toString());
					}
				}
			} catch (HttpClientErrorException httpException) {
				getLog().info(
						"Error when running " + action + " on stack "
								+ stackName + " : "
								+ httpException.getResponseBodyAsString());
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	private HashMap<Object, Object> setStackRequest(Stack _stack) {

		HashMap<Object, Object> request = new LinkedHashMap<Object, Object>();
		request.put("name", _stack.getName());
		request.put("policies", _stack.getPolicies());
		request.put("components", _stack.getComponents());
		request.put("icon", _stack.getIcon());
		request.put("description", _stack.getDescription());
		request.put("owner", _stack.getOwner());
		request.put("mode", _stack.getMode());
		request.put("templateLevel", _stack.getTemplateLevel());
		request.put("propertyOverrides", _stack.getPropertyOverrides());
		request.put("technology", _stack.getTechnology());
		request.put("urls", _stack.getUrls());

		return request;
	}

	/**
	 * @return the breakout
	 */
	public final boolean isBreakout() {
		return breakout;
	}

	/**
	 * @param breakout
	 *            the breakout to set
	 */
	public final void setBreakout(boolean breakout) {
		this.breakout = breakout;
	}


}