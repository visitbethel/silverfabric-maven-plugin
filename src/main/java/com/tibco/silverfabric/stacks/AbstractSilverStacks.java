/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric.stacks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;

import com.fedex.scm.Policy;
import com.fedex.scm.PropertyOverride;
import com.tibco.silverfabric.AbstractSilverFabricMojo;
import com.tibco.silverfabric.SilverFabricConfig;
import com.tibco.silverfabric.Stacks;
import com.tibco.silverfabric.model.Plan;

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
	protected Plan plan;

	@Parameter
	protected List<String> components;
	@Parameter
	protected String stackName;
	@Parameter
	protected String mode;
	@Parameter(defaultValue = "--")
	protected String templateLevel;
	@Parameter
	protected List<Policy> policies;
	@Parameter
	protected String accountName;
	@Parameter
	protected String runMode;
	@Parameter
	protected List<PropertyOverride> propertyOverrides;
	@Parameter
	protected String owner;
	@Parameter
	protected String technology;
	@Parameter(defaultValue = "/livecluster/admin/images/icons/stackIcons/defaults/6_Skyway_Generic_Default_Icon.png")
	protected String icon;
	@Parameter(defaultValue = "Default Template Description.")
	protected String description;
	@Parameter
	protected List<Map> urls;

	private boolean breakout = false;

	/**
     * 
     */
	private com.fedex.scm.Stacks stack;

	/**
	 * 
	 * @throws MojoFailureException
	 */
	@SuppressWarnings("unchecked")
	public void initialize() throws MojoFailureException {

		if (this.plan != null) {
			try {
				this.stack = SilverFabricConfig.loadingRESTPlan(this,
						this.plan.stackTemplateURI, com.fedex.scm.Stacks.class);
			} catch (FileNotFoundException e) {
				throw new MojoFailureException("Plan not found", e);
			}
		}
		if (this.stack != null && this.stackName == null) {
			this.stackName = this.stack.getName();
		}
		final AbstractSilverFabricMojo THIS = this;
		if (this.breakout) {
			getLog().warn(
					"[[[[[[[[[[[ BREAK OUT IS ON MALFUNCTIONING EXPECTED ]]]]]]]]");
			getRestTemplate().getInterceptors().add(
					new ClientHttpRequestInterceptor() {

						@Override
						public ClientHttpResponse intercept(HttpRequest arg0,
								byte[] arg1, ClientHttpRequestExecution arg2)
								throws IOException {
							// TODO Auto-generated method stub
							THIS.getLog()
									.info("message: \n\t"
											+ new String(arg1, "UTF-8"));
							return null;
						}
					});
		}
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
		mergeStackPlan();
		if (this.stack.getComponents() == null || this.stack.getComponents().isEmpty()) {
			throw new MojoExecutionException("The components parameters are required to create a stack");
		}
		if (this.stack.getPolicies() == null || this.stack.getPolicies().isEmpty()) {
			throw new MojoExecutionException("The policies parameters are required to create a stack");
		}

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
				switch (action) {
				case "create":
					Map<Object, Object> mapCreate = setStackRequest();
					if (mapCreate == null)
						throw new MojoFailureException(
								"The policies and components parameters are required to create a stack");
					getLog().info(
							restTemplate.postForObject(url, mapCreate,
									String.class).toString());
					break;
				case "publish":
					restTemplate.put(url + "/{stackName}/published/true", null,
							stackName);
					getLog().info(stackName + " published!");
					break;
				case "unpublish":
					restTemplate.put(url + "/{stackName}/published/false",
							null, stackName);
					getLog().info(stackName + " unpublished!");
					break;
				case "update":
					Map<Object, Object> mapUpdate = setStackRequest();
					if (mapUpdate == null)
						throw new MojoFailureException(
								"The following parameters are required to "
										+ action
										+ " a component: enablerName, enablerVersion, componentType");
					restTemplate
							.put(url + "/{stackName}", mapUpdate, stackName);
					getLog().info(stackName + " updated!");
					break;
				case "delete":
					restTemplate.delete(url + "/" + "{stackName}", stackName);
					getLog().info(stackName + " deleted!");
					break;
				case "get info":
					LinkedHashMap<String, LinkedHashMap<String, Object>> infoLinkedHashMap;
					infoLinkedHashMap = restTemplate.getForObject(url
							+ "/{stackName}", LinkedHashMap.class, stackName);
					getLog().info(
							infoLinkedHashMap.get("result").get("value")
									.toString());
					break;
				case "assign to non cloud":
					Map<String, String> accountNameMap = new HashMap<>();
					accountNameMap.put("name", "name");
					accountNameMap.put("value", accountName);
					getLog().info(
							restTemplate.postForObject(
									url + "/{stackName}/assign-to-non-cloud",
									accountNameMap, String.class, stackName)
									.toString());
					break;
				case "get config file":
					HttpHeaders requestHeaders = new HttpHeaders();
					List<MediaType> mediaTypes = new ArrayList<>();
					mediaTypes.add(MediaType.TEXT_PLAIN);
					requestHeaders.setAccept(mediaTypes);
					HttpEntity<?> requestEntity = new HttpEntity(requestHeaders);
					getLog().info(
							restTemplate.exchange(
									url + "/{stackName}/config-file",
									HttpMethod.GET, requestEntity,
									String.class, stackName).toString());
					break;
				case "set run mode":
					restTemplate.put(url + "/{stackName}/mode/{run-mode}",
							null, stackName, runMode);
					getLog().info("run mode set to " + runMode + " !");
					break;
				case "auto-detect http-urls":
					restTemplate.postForObject(url
							+ "/{component}/http-urls/auto-detect", null,
							String.class, stackName);
					getLog().info(
							restTemplate.postForObject(
									url + "/{component}/http-urls/auto-detect",
									null, String.class, stackName).toString());
					break;
				case "get patches":
					LinkedHashMap<Object, Object> getPatches = restTemplate
							.getForObject(url + "/{component}/patches",
									LinkedHashMap.class, stackName);
					getLog().info(getPatches.get("result").toString());
					break;
				case "get script-files":
					LinkedHashMap<Object, Object> getScriptFiles = restTemplate
							.getForObject(url + "/{component}/script-files",
									LinkedHashMap.class, stackName);
					getLog().info(getScriptFiles.get("result").toString());
					break;
				case "get type names":
					getLog().info(
							restTemplate
									.getForObject(url + "/type-names",
											LinkedHashMap.class).get("result")
									.toString());
					break;
				case "get types":
					getLog().info(
							restTemplate
									.getForObject(url + "/types",
											LinkedHashMap.class).get("result")
									.toString());
					break;
				case "get":
					getLog().info(
							restTemplate.getForObject(url, LinkedHashMap.class)
									.get("result").toString());
					break;
				case "clean":
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
					break;
				default:
					break;
				}
			} catch (HttpClientErrorException httpException) {
				getLog().info(
						"Error when running " + action + " on stack "
								+ stackName + " : "
								+ httpException.getResponseBodyAsString());
			}
		}
	}

	private void mergeStackPlan() {
		if (this.getComponents() == null || this.getComponents().isEmpty()) {
			this.setComponents(this.stack.getComponents());
		}
		if (this.getPolicies() == null || this.getPolicies().isEmpty()) {
			this.setPolicies(this.stack.getPolicies());
		}
		if (this.getStackName() == null) {
			this.setStackName(this.stack.getName());
		}
		if (this.getPropertyOverrides() == null || this.getPropertyOverrides().isEmpty()) {
			this.setPropertyOverrides(this.stack.getPropertyOverrides());
		}
		if (this.mode == null && this.stack.getMode() != null) {
			this.mode = this.stack.getMode();
		}
		if (this.templateLevel == null && this.stack.getTemplateLevel() != null) {
			this.templateLevel = this.stack.getTemplateLevel();
		}
		if (this.technology == null && this.stack.getTechnology() != null) {
			this.technology = this.stack.getTechnology();
		}
		if (this.urls == null && this.stack.getUrls() != null) {
			//this.urls = this.stack.getUrls();
		}
	}

	@SuppressWarnings("rawtypes")
	private HashMap<Object, Object> setStackRequest() {

        HashMap<Object, Object> request = new LinkedHashMap<Object, Object>();
        request.put("name", stackName);
        request.put("policies", policies);
        request.put("components", components);
        request.put("icon", icon);
        request.put("description", description);

        if (owner != null) request.put("owner", owner);
        if (mode != null) request.put("mode", mode);
        if (templateLevel != null) request.put("templateLevel", templateLevel);
        if (propertyOverrides != null) request.put("propertyOverrides", propertyOverrides);
        if (technology != null) request.put("technology", technology);
        if (urls != null) request.put("urls", urls);

		return request;
	}

	/**
	 * @return the components
	 */
	public final List<String> getComponents() {
		return components;
	}

	/**
	 * @param components
	 *            the components to set
	 */
	public final void setComponents(List<String> components) {
		this.components = components;
	}

	/**
	 * @return the stackName
	 */
	public final String getStackName() {
		return stackName;
	}

	/**
	 * @param stackName
	 *            the stackName to set
	 */
	public final void setStackName(String stackName) {
		this.stackName = stackName;
	}

	/**
	 * @return the mode
	 */
	public final String getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public final void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @return the templateLevel
	 */
	public final String getTemplateLevel() {
		return templateLevel;
	}

	/**
	 * @param templateLevel
	 *            the templateLevel to set
	 */
	public final void setTemplateLevel(String templateLevel) {
		this.templateLevel = templateLevel;
	}

	/**
	 * @return the policies
	 */
	public final List<Policy> getPolicies() {
		return policies;
	}

	/**
	 * @param policies
	 *            the policies to set
	 */
	public final void setPolicies(List<Policy> policies) {
		this.policies = policies;
	}

	/**
	 * @return the accountName
	 */
	public final String getAccountName() {
		return accountName;
	}

	/**
	 * @param accountName
	 *            the accountName to set
	 */
	public final void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	/**
	 * @return the runMode
	 */
	public final String getRunMode() {
		return runMode;
	}

	/**
	 * @param runMode
	 *            the runMode to set
	 */
	public final void setRunMode(String runMode) {
		this.runMode = runMode;
	}

	/**
	 * @return the propertyOverrides
	 */
	public final List<PropertyOverride> getPropertyOverrides() {
		return propertyOverrides;
	}

	/**
	 * @param propertyOverrides
	 *            the propertyOverrides to set
	 */
	public final void setPropertyOverrides(
			List<PropertyOverride> propertyOverrides) {
		this.propertyOverrides = propertyOverrides;
	}

	/**
	 * @return the owner
	 */
	public final String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public final void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the technology
	 */
	public final String getTechnology() {
		return technology;
	}

	/**
	 * @param technology
	 *            the technology to set
	 */
	public final void setTechnology(String technology) {
		this.technology = technology;
	}

	/**
	 * @return the icon
	 */
	public final String getIcon() {
		return icon;
	}

	/**
	 * @param icon
	 *            the icon to set
	 */
	public final void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * @return the description
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public final void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the urls
	 */
	public final List<Map> getUrls() {
		return urls;
	}

	/**
	 * @param urls
	 *            the urls to set
	 */
	public final void setUrls(List<Map> urls) {
		this.urls = urls;
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

	/**
	 * @return the stack
	 */
	public final com.fedex.scm.Stacks getStack() {
		return stack;
	}

}