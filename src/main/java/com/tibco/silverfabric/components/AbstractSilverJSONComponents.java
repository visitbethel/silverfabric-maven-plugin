/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import com.fedex.scm.DefaultSetting;
import com.fedex.scm.Feature;
import com.fedex.scm.Option;
import com.fedex.scm.RuntimeContextVariable;
import com.tibco.silverfabric.AbstractSilverFabricMojo;
import com.tibco.silverfabric.Archive;
import com.tibco.silverfabric.Components;
import com.tibco.silverfabric.DefaultAllocationSetting;
import com.tibco.silverfabric.SilverFabricConfig;
import com.tibco.silverfabric.model.Plan;

/**
 * Actions related to components.
 *
 */
public abstract class AbstractSilverJSONComponents extends Components {

	public abstract static class InternalCallback {
		public abstract void process(Object result);
	}

	/* input data */

	/**
	 * <pre>
	 * <plan>
	 * 		<componentTemplateURI>/templates/2.6.0.4/component.json</componentTemplateURI>
	 * 		<stackTemplateURI>/templates/stack.json</stackTemplateURI>
	 * </plan>
	 * </pre>
	 */
	@Parameter
	public Plan plan;

	/* interface */

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
	private LinkedList<RuntimeContextVariable> runtimeContextVariables = new LinkedList<RuntimeContextVariable>();
	@Parameter
	private LinkedList<Feature> features = new LinkedList<Feature>();
	@Parameter
	private List<Archive> archives = new LinkedList<Archive>();
	@Parameter
	private List<DefaultAllocationSetting> defaultAllocationRuleSettings = new LinkedList<DefaultAllocationSetting>();
	@Parameter
	private List<DefaultSetting> defaultSettings = new LinkedList<DefaultSetting>();
	@Parameter
	private List<String> allocationConstraints = new LinkedList<String>();
	@Parameter
	private String accountName;
	@Parameter
	private Archive configFile;
	@Parameter
	private List<Archive> contentFiles = new LinkedList<Archive>();
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
	@Parameter
	private boolean override = false;
	@Parameter(property = "breakout")
	private boolean breakout = false;
	private File outputDirectory = new File("target");
	private boolean failOnError = true;

	/**
     * 
     */
	private com.fedex.scm.Components component;
	private InternalCallback internalCallback;

	/**
	 * 
	 * @throws MojoFailureException
	 */
	public void initialize() throws MojoFailureException {
		if (this.plan != null) {
			File outPlan = filterFile(this.outputDirectory,
					plan.getComponentPlanPath());
			getLog().info("loading plan from " + outPlan);
			try {
				component = SilverFabricConfig.loadingRESTPlan(this, outPlan,
						com.fedex.scm.Components.class);
			} catch (FileNotFoundException e) {
				throw new MojoFailureException("Plan not found", e);
			}
		}
		if (component != null) {
			this.setComponentName(component.getName());
			this.setEnablerName(component.getEnablerName());
			this.setEnablerVersion(component.getEnablerVersion());
			this.setComponentType(component.getComponentType());
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

	@SuppressWarnings("unchecked")
	public void executeMojo() throws MojoExecutionException,
			MojoFailureException {

		initialize();

		getLog().info("execute from " + this.getClass());

		List<String> actionList = getActions() != null ? getActions()
				: new ArrayList<String>();
		if (actionList.isEmpty())
			actionList.add("get");
		String url = getBrokerConfig().getBrokerURL().toString()
				+ "/livecluster/rest/v1/sf/components";
		getLog().debug(url);

		for (String action : actionList) {
			if (!action.equals("clean") && !action.equals("get")
					&& !action.equals("get types")
					&& !action.equals("get type names")
					&& (componentName == null || componentName.isEmpty()))
				throw new MojoFailureException(
						"The parameter \"componentName\" is required by the component action: "
								+ action);
			try {
				switch (action) {
				case "create":
					Map<Object, Object> mapCreate = setComponentRequest();
					if (mapCreate == null)
						throw new MojoFailureException(
								"The following parameters are required to create a component: enablerName, enablerVersion, componentType");
					String result = "";
					result = restTemplate.postForObject(url, mapCreate,
							String.class);
					getLog().info(result.toString());
					break;
				case "publish":
					failOnError = false;
					restTemplate.put(url + "/{componentName}/published/true",
							null, componentName);
					getLog().info(componentName + " published!");
					break;
				case "unpublish":
					failOnError = false;
					restTemplate.put(url + "/{componentName}/published/false",
							null, componentName);
					getLog().info(componentName + " unpublished!");
					break;
				case "update":
					Map<Object, Object> mapUpdate = setComponentRequest();
					if (mapUpdate == null)
						throw new MojoFailureException(
								"The following parameters are required to "
										+ action
										+ " a component: enablerName, enablerVersion, componentType");
					restTemplate.put(url + "/{componentName}", mapUpdate,
							componentName);
					getLog().info(componentName + " updated!");
					break;
				case "delete":
					restTemplate.delete(url + "/" + "{componentName}",
							componentName);
					getLog().info(componentName + " deleted!");
					break;
				case "get info":
					LinkedHashMap<String, LinkedHashMap<String, Object>> infoLinkedHashMap;
					infoLinkedHashMap = restTemplate.getForObject(url
							+ "/{componentName}", LinkedHashMap.class,
							componentName);
					getLog().info(
							infoLinkedHashMap.get("result").get("value")
									.toString());
					if (internalCallback != null) {
						internalCallback.process(infoLinkedHashMap);
					}
					break;
				case "get archives":
					getLog().info(
							restTemplate.getForObject(
									url + "/{componentName}/archives",
									LinkedHashMap.class, componentName)
									.toString());
					break;
				case "add archives":
					MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
					for (Archive archive : archives) {
						if (archive.exists()) {
							parts.add("archiveFile",
									new FileSystemResource(archive.getPath()
											+ "/" + archive.getName()));
						}
					}
					if (!parts.isEmpty()) {
						String addArchivesResponse = restTemplate
								.postForObject(url
										+ "/{componentName}/archives", parts,
										String.class, componentName);
						getLog().info(addArchivesResponse);
					} else {
						getLog().warn(
								"no archives detected as [" + archives + "]");
					}
					break;
				case "remove archive":
					restTemplate.delete(url
							+ "/{componentName}/archives/{archive}",
							componentName, archives.get(0).getName());
					getLog().info("Archive deleted!");
					break;
				case "remove archives":
					restTemplate.delete(url + "/{componentName}/archives",
							componentName);
					getLog().info("Archives deleted!");
					break;
				case "assign to non cloud":
					Map<String, String> accountNameMap = new HashMap<>();
					accountNameMap.put("name", "name");
					accountNameMap.put("value", accountName);
					result = restTemplate.postForObject(url
							+ "/{componentName}/assign-to-non-cloud",
							accountNameMap, String.class, componentName);
					getLog().info(result.toString());
					break;
				case "get config file":
					HttpHeaders requestHeaders = new HttpHeaders();
					List<MediaType> mediaTypes = new ArrayList<>();
					mediaTypes.add(MediaType.TEXT_PLAIN);
					requestHeaders.setAccept(mediaTypes);
					HttpEntity<?> requestEntity = new HttpEntity(requestHeaders);
					getLog().info(
							restTemplate.exchange(
									url + "/{componentName}/config-file",
									HttpMethod.GET, requestEntity,
									String.class, componentName).toString());
					break;
				case "update config file":
					HttpHeaders updateRequestHeaders = new HttpHeaders();
					List<MediaType> updateMediaTypes = new ArrayList<>();
					updateMediaTypes.add(MediaType.APPLICATION_JSON);
					updateRequestHeaders.setAccept(updateMediaTypes);
					updateRequestHeaders.setContentType(MediaType.TEXT_PLAIN);
					HttpEntity<?> updateRequestEntity = new HttpEntity(
							new FileSystemResource(configFile.getPath() + "/"
									+ configFile.getName()),
							updateRequestHeaders);
					restTemplate.exchange(url + "/{componentName}/config-file",
							HttpMethod.PUT, updateRequestEntity, String.class,
							componentName);
					getLog().info(componentName + " updated config file!");
					break;
				case "delete config file":
					restTemplate.delete(url + "/{componentName}/config-file",
							componentName);
					break;
				case "get content file path":
					getLog().info(
							restTemplate.getForObject(
									url + "/{componentName}/content-files",
									LinkedHashMap.class, componentName)
									.toString());
					break;
				case "get content file path with regexp":
					getLog().info(
							restTemplate
									.getForObject(
											url
													+ "/{componentName}/content-files-by-regex?regex={contentFileRegex}",
											LinkedHashMap.class, componentName,
											contentFileRegex).toString());
					break;
				case "delete content file path with regex":
					HashMap<String, String> deleteContentFileParts = new HashMap<>();
					deleteContentFileParts.put("name", "regex");
					deleteContentFileParts.put("value", contentFileRegex);
					HttpHeaders deleteContentFileRequestHeaders = new HttpHeaders();
					List<MediaType> deleteContentFileMediaTypes = new ArrayList<>();
					deleteContentFileMediaTypes.add(MediaType.APPLICATION_JSON);
					deleteContentFileRequestHeaders
							.setAccept(deleteContentFileMediaTypes);
					deleteContentFileRequestHeaders
							.setContentType(MediaType.APPLICATION_JSON);
					HttpEntity<?> deleteContentFileRequestEntity = new HttpEntity(
							deleteContentFileParts,
							deleteContentFileRequestHeaders);
					restTemplate.exchange(url
							+ "/{componentName}/content-files-by-regex",
							HttpMethod.POST, deleteContentFileRequestEntity,
							String.class, componentName);
					break;
				case "add content file":
					for (Archive contentFile : contentFiles) {
						MultiValueMap<String, Object> addContentFileParts = new LinkedMultiValueMap<>();
						addContentFileParts.add("contentFile",
								new FileSystemResource(contentFile.getPath()
										+ "/" + contentFile.getName()));
						addContentFileParts.add("relativePath",
								contentFile.getRelativePath());
						String addContentFileResponse = restTemplate
								.postForObject(url
										+ "/{componentName}/content-files",
										addContentFileParts, String.class,
										componentName);
						getLog().info(
								contentFile.getName() + addContentFileResponse);
					}
					break;
				case "get http-urls":
					LinkedHashMap<Object, Object> getHttpUrl = restTemplate
							.getForObject(url + "/{component}/http-urls",
									LinkedHashMap.class, componentName);
					getLog().info(getHttpUrl.get("result").toString());
					break;
				case "delete http-urls":
					getLog().info(
							restTemplate.exchange(
									url + "/{component}/http-urls",
									HttpMethod.DELETE, null, String.class,
									componentName).toString());
					break;
				case "add http-urls":
					List<String> addHttpUrlsRequest = new ArrayList<>();
					for (String relativeURL : relativeURLs) {
						addHttpUrlsRequest.add(relativeURL);
					}
					getLog().info(
							restTemplate.postForObject(
									url + "/{component}/http-urls",
									addHttpUrlsRequest, String.class,
									componentName).toString());
					break;
				case "update http-urls":
					List<String> updateHttpUrlsRequest = new ArrayList<>();
					for (String relativeURL : relativeURLs) {
						updateHttpUrlsRequest.add(relativeURL);
					}
					restTemplate.put(url + "/{component}/http-urls",
							updateHttpUrlsRequest, componentName);
					getLog().info("http urls updated!");
					break;
				case "auto-detect http-urls":
					restTemplate.postForObject(url
							+ "/{component}/http-urls/auto-detect", null,
							String.class, componentName);
					getLog().info(
							restTemplate.postForObject(
									url + "/{component}/http-urls/auto-detect",
									null, String.class, componentName)
									.toString());
					break;
				case "get patches":
					LinkedHashMap<Object, Object> getPatches = restTemplate
							.getForObject(url + "/{component}/patches",
									LinkedHashMap.class, componentName);
					getLog().info(getPatches.get("result").toString());
					break;
				case "get script-files":
					LinkedHashMap<Object, Object> getScriptFiles = restTemplate
							.getForObject(url + "/{component}/script-files",
									LinkedHashMap.class, componentName);
					getLog().info(getScriptFiles.get("result").toString());
					break;
				case "add script-files":
					MultiValueMap<String, Object> addScriptFilesParts = new LinkedMultiValueMap<>();
					addScriptFilesParts.add("scriptFile",
							new FileSystemResource(scriptFile.getPath() + "/"
									+ scriptFile.getName()));
					addScriptFilesParts.add("scriptLang", scriptLang);
					addScriptFilesParts.add("scriptLangVersion",
							scriptLangVersion);
					String addScriptFilesResponse = restTemplate.postForObject(
							url + "/{componentName}/script-files",
							addScriptFilesParts, String.class, componentName);
					getLog().info(addScriptFilesResponse);
					break;
				case "get script-files content":
					HttpHeaders getScriptFileHeaders = new HttpHeaders();
					List<MediaType> mediaTypesScriptFile = new ArrayList<>();
					mediaTypesScriptFile.add(MediaType.TEXT_PLAIN);
					getScriptFileHeaders.setAccept(mediaTypesScriptFile);
					HttpEntity<?> getScriptFileEntity = new HttpEntity(
							getScriptFileHeaders);
					getLog().info(
							restTemplate
									.exchange(
											url
													+ "/{componentName}/script-files/{script-name}",
											HttpMethod.GET,
											getScriptFileEntity, String.class,
											componentName, scriptName)
									.toString());
					break;
				case "update script-files content":
					HttpHeaders updateScriptFileRequestHeaders = new HttpHeaders();
					updateScriptFileRequestHeaders
							.setContentType(MediaType.TEXT_PLAIN);
					List<MediaType> scriptFileMediaTypeList = new ArrayList<>();
					scriptFileMediaTypeList.add(MediaType.APPLICATION_JSON);
					updateScriptFileRequestHeaders
							.setAccept(scriptFileMediaTypeList);
					HttpEntity<?> updateScriptFileRequestEntity = new HttpEntity(
							new FileSystemResource(scriptFile.getPath() + "/"
									+ scriptFile.getName()),
							updateScriptFileRequestHeaders);
					restTemplate.exchange(url
							+ "/{componentName}/script-files/{script-name}",
							HttpMethod.PUT, updateScriptFileRequestEntity,
							String.class, componentName, scriptName);
					break;
				case "remove script-files":
					getLog().info(
							restTemplate
									.exchange(
											url
													+ "/{componentName}/script-files/{script-name}",
											HttpMethod.DELETE, null,
											String.class, componentName,
											scriptName).toString());
					break;
				case "get script-files path with regex":
					getLog().info(
							restTemplate
									.getForObject(
											url
													+ "/{componentName}/script-files-by-regex?regex={scriptFileRegex}",
											LinkedHashMap.class, componentName,
											scriptFileRegex).toString());
					break;
				case "delete script-files path with regex":
					HashMap<String, String> deleteScriptFileParts = new HashMap<>();
					deleteScriptFileParts.put("name", "regex");
					deleteScriptFileParts.put("value", scriptFileRegex);
					HttpEntity<?> deleteScriptFileRequestEntity = new HttpEntity(
							deleteScriptFileParts);
					getLog().info(
							restTemplate.postForObject(url
									+ "/{componentName}/script-files-by-regex",
									deleteScriptFileRequestEntity,
									String.class, componentName));
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
					String getRequest = url + "?info=" + info;
					if (type != null && !type.isEmpty())
						getRequest += "&type=" + type;
					if (engineId != 0)
						getRequest += "&engineId=" + engineId;
					if (instance != null && !instance.isEmpty())
						getRequest += "&instance=" + instance;
					getLog().debug(getRequest);
					LinkedHashMap<Object, Object> response = restTemplate
							.getForObject(getRequest, LinkedHashMap.class);
					Integer status = (Integer) response.get("status");
					if (status == 200
							&& ((LinkedHashMap<Object, Object>) (response
									.get("result"))).get("name").toString()
									.equals("componentNames")) {
						ArrayList<String> componentNameList = (ArrayList<String>) ((LinkedHashMap<Object, Object>) (response
								.get("result"))).get("value");
						for (String componentName : componentNameList) {
							getLog().info(componentName);
						}
					} else {
						getLog().warn("Status = " + status);
						getLog().warn(response.get("result").toString());
					}
					break;
				case "clean":
					LinkedHashMap<Object, Object> getResponse = restTemplate
							.getForObject(url + "?info=names",
									LinkedHashMap.class);
					Integer getStatus = (Integer) getResponse.get("status");
					if (getStatus == 200
							&& ((LinkedHashMap<Object, Object>) (getResponse
									.get("result"))).get("name").toString()
									.equals("componentNames")) {
						ArrayList<String> componentNameList = (ArrayList<String>) ((LinkedHashMap<Object, Object>) (getResponse
								.get("result"))).get("value");
						for (String componentName : componentNameList) {
							restTemplate.put(url
									+ "/{componentName}/published/false", null,
									componentName);
							restTemplate.delete(url + "/" + "{componentName}",
									componentName);
							getLog().info(componentName + "removed!");
						}
					} else {
						getLog().warn("Status = " + getStatus);
						getLog().warn(getResponse.get("result").toString());
					}
					break;
				default:
					break;
				}
			} catch (HttpClientErrorException httpException) {
				getLog().info(
						"Error when running " + action + " on component "
								+ componentName + " : "
								+ httpException.getResponseBodyAsString());
				if (failOnError) {
					throw new MojoExecutionException("Error when running "
							+ action + " on component " + componentName + " : "
							+ httpException.getResponseBodyAsString(),
							httpException);
				}
			} finally {
				failOnError = true;
			}
		}
	}

	/**
	 * @param internalCallback
	 *            the internalCallback to set
	 */
	public final void setInternalCallback(InternalCallback internalCallback) {
		this.internalCallback = internalCallback;
	}

	/**
	 * 
	 * @return
	 */
	protected HashMap<Object, Object> setComponentRequest() {
		HashMap<Object, Object> request = new LinkedHashMap<Object, Object>();

		request.put(
				"componentType",
				valueOf(component != null ? component.getComponentType() : null,
						componentType));
		request.put("name", componentName);
		request.put(
				"enablerName",
				valueOf(component != null ? component.getEnablerName() : null,
						enablerName));
		request.put(
				"enablerVersion",
				valueOf(component != null ? component.getEnablerVersion()
						: null, enablerVersion));
		valueOf(request, "description", description, null);
		valueOf(request, "trackedStatistics", trackedStatistics, null);
		valueOf(request, "options", component != null ? component.getOptions()
				: null, options);
		valueOf(request, "runtimeContextVariables",
				component != null ? component.getRuntimeContextVariables()
						: null, runtimeContextVariables);
		valueOf(request, "features",
				component != null ? component.getFeatures() : null, features);
		valueOf(request, "defaultAllocationRuleSettings",
				defaultAllocationRuleSettings, null);
		valueOf(request, "defaultSettings", defaultSettings, null);
		valueOf(request,
				"allocationConstraints",
				component != null ? component.getAllocationConstraints() : null,
				allocationConstraints);
		return request;
	}

	/**
	 * @return the componentType
	 */
	public final String getComponentType() {
		return componentType;
	}

	/**
	 * @param componentType
	 *            the componentType to set
	 */
	public final void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	/**
	 * @return the componentName
	 */
	public final String getComponentName() {
		return componentName;
	}

	/**
	 * @param componentName
	 *            the componentName to set
	 */
	public final void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	/**
	 * @return the enablerName
	 */
	public final String getEnablerName() {
		return enablerName;
	}

	/**
	 * @param enablerName
	 *            the enablerName to set
	 */
	public final void setEnablerName(String enablerName) {
		this.enablerName = enablerName;
	}

	/**
	 * @return the enablerVersion
	 */
	public final String getEnablerVersion() {
		return enablerVersion;
	}

	/**
	 * @param enablerVersion
	 *            the enablerVersion to set
	 */
	public final void setEnablerVersion(String enablerVersion) {
		this.enablerVersion = enablerVersion;
	}

	/**
	 * @return the archives
	 */
	public final List<Archive> getArchives() {
		return archives;
	}

	/**
	 * @return the configFile
	 */
	public final Archive getConfigFile() {
		return configFile;
	}

	/**
	 * @param configFile
	 *            the configFile to set
	 */
	public final void setConfigFile(Archive configFile) {
		this.configFile = configFile;
	}

	/**
	 * @return the contentFiles
	 */
	public final List<Archive> getContentFiles() {
		return contentFiles;
	}

	/**
	 * @param contentFiles
	 *            the contentFiles to set
	 */
	public final void setContentFiles(List<Archive> contentFiles) {
		this.contentFiles = contentFiles;
	}

	/**
	 * @return the scriptFile
	 */
	public final Archive getScriptFile() {
		return scriptFile;
	}

	/**
	 * @param scriptFile
	 *            the scriptFile to set
	 */
	public final void setScriptFile(Archive scriptFile) {
		this.scriptFile = scriptFile;
	}

	/**
	 * @return the scriptLang
	 */
	public final String getScriptLang() {
		return scriptLang;
	}

	/**
	 * @param scriptLang
	 *            the scriptLang to set
	 */
	public final void setScriptLang(String scriptLang) {
		this.scriptLang = scriptLang;
	}

	/**
	 * @return the scriptLangVersion
	 */
	public final String getScriptLangVersion() {
		return scriptLangVersion;
	}

	/**
	 * @param scriptLangVersion
	 *            the scriptLangVersion to set
	 */
	public final void setScriptLangVersion(String scriptLangVersion) {
		this.scriptLangVersion = scriptLangVersion;
	}

	/**
	 * @return the scriptName
	 */
	public final String getScriptName() {
		return scriptName;
	}

	/**
	 * @param scriptName
	 *            the scriptName to set
	 */
	public final void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	/**
	 * @return the scriptFileRegex
	 */
	public final String getScriptFileRegex() {
		return scriptFileRegex;
	}

	/**
	 * @param scriptFileRegex
	 *            the scriptFileRegex to set
	 */
	public final void setScriptFileRegex(String scriptFileRegex) {
		this.scriptFileRegex = scriptFileRegex;
	}

}
