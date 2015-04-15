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
import java.util.Properties;

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
import com.tibco.silverfabric.DefaultAllocationSetting;
import com.tibco.silverfabric.SilverFabricConfig;
import com.tibco.silverfabric.model.Archive;
import com.tibco.silverfabric.model.Component;
import com.tibco.silverfabric.model.Plan;

/**
 * Actions related to components.
 *
 */
public abstract class AbstractSilverComponentsRestCall extends
		AbstractSilverFabricMojo {

	
	@Parameter(defaultValue = "${project.build.directory}/work")
	public File workDirectory = new File("target/work");
	@Parameter(defaultValue = "names")
	private String info;
	@Parameter
	private String type;
	@Parameter
	private long engineId;
	@Parameter
	private String instance;
	
	//
	Component component;

//	@Parameter
//	private String componentType;
//	@Parameter
//	public String componentName;
//	@Parameter
//	private String enablerName;
//	@Parameter
//	private String enablerVersion;
//	@Parameter
//	private String description;
//	@Parameter
//	private List<String> trackedStatistics;
//	@Parameter
//	private LinkedList<Option> options;
//	@Parameter
//	private LinkedList<RuntimeContextVariable> runtimeContextVariables = new LinkedList<RuntimeContextVariable>();
//	@Parameter
//	private LinkedList<Feature> features = new LinkedList<Feature>();
	@Parameter
	private List<Archive> archives = new LinkedList<Archive>();
//	@Parameter
//	private List<DefaultAllocationSetting> defaultAllocationRuleSettings = new LinkedList<DefaultAllocationSetting>();
//	@Parameter
//	private List<DefaultSetting> defaultSettings = new LinkedList<DefaultSetting>();
//	@Parameter
//	private List<String> allocationConstraints = new LinkedList<String>();
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
	 * @throws MojoFailureException
	 */
	public void initialize() throws MojoFailureException {
//			File outPlan = filterFile(this.outputDirectory,
//					plan.getComponentPlanPath(), component.properties);
//			getLog().info(
//					"loading plan from " + outPlan + " for component "
//							+ this.componentName);
//
//		}
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
		String componentName = component.getName();

		for (String action : actionList) {
			if (!action.equals("clean") && !action.equals("get")
					&& !action.equals("get types")
					&& !action.equals("get type names")
					&& (componentName == null || componentName.isEmpty()))
				throw new MojoFailureException(
						"The parameter \"componentName\" is required by the component action: "
								+ action);
			String result = "";
			try {
				if ("create".equals(action)) {
					Map<Object, Object> mapCreate = setComponentRequest(component);
					if (mapCreate == null)
						throw new MojoFailureException(
								"The following parameters are required to create a component: enablerName, enablerVersion, componentType");
					result = restTemplate.postForObject(url, mapCreate,
							String.class);
					getLog().info(
							">>>>>>>>>> COMPONENT [" + result.toString() + "]");
				} else if ("publish".equals(action)) {
					failOnError = false;
					restTemplate.put(url + "/{componentName}/published/true",
							null, componentName);
					getLog().info(
							">>>>>>>>>> COMPONENT [" + componentName
									+ "] published!");
				} else if ("unpublish".equals(action)) {
					failOnError = false;
					restTemplate.put(url + "/{componentName}/published/false",
							null, componentName);
					getLog().info(
							">>>>>>>>>> COMPONENT [" + componentName
									+ "] unpublished!");
				} else if ("update".equals(action)) {
					Map<Object, Object> mapUpdate = setComponentRequest(component);
					if (mapUpdate == null)
						throw new MojoFailureException(
								"The following parameters are required to "
										+ action
										+ " a component: enablerName, enablerVersion, componentType");
					restTemplate.put(url + "/{componentName}", mapUpdate,
							componentName);
					getLog().info(
							">>>>>>>>>> COMPONENT [" + componentName
									+ "] updated!");
				} else if ("delete".equals(action)) {
					restTemplate.delete(url + "/" + "{componentName}",
							componentName);
					getLog().info(
							">>>>>>>>>> COMPONENT [" + componentName
									+ "] deleted!");
				} else if ("get info".equals(action)) {
					LinkedHashMap<String, LinkedHashMap<String, Object>> infoLinkedHashMap;
					infoLinkedHashMap = restTemplate.getForObject(url
							+ "/{componentName}", LinkedHashMap.class,
							componentName);
					getLog().info(
							infoLinkedHashMap.get("result").get("value")
									.toString());
				} else if ("get archives".equals(action)) {
					getLog().info(
							restTemplate.getForObject(
									url + "/{componentName}/archives",
									LinkedHashMap.class, componentName)
									.toString());
				} else if ("add archives".equals(action)) {
					MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
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
				} else if ("remove archive".equals(action)) {
					restTemplate.delete(url
							+ "/{componentName}/archives/{archive}",
							componentName, archives.get(0).getName());
					getLog().info("Archive deleted!");
				} else if ("remove archives".equals(action)) {
					restTemplate.delete(url + "/{componentName}/archives",
							componentName);
					getLog().info("Archives deleted!");
				} else if ("assign to non cloud".equals(action)) {
					Map<String, String> accountNameMap = new HashMap<String, String>();
					accountNameMap.put("name", "name");
					accountNameMap.put("value", accountName);
					result = restTemplate.postForObject(url
							+ "/{componentName}/assign-to-non-cloud",
							accountNameMap, String.class, componentName);
					getLog().info(result.toString());
				} else if ("get config file".equals(action)) {
					HttpHeaders requestHeaders = new HttpHeaders();
					List<MediaType> mediaTypes = new ArrayList<MediaType>();
					mediaTypes.add(MediaType.TEXT_PLAIN);
					requestHeaders.setAccept(mediaTypes);
					HttpEntity<?> requestEntity = new HttpEntity(requestHeaders);
					getLog().info(
							restTemplate.exchange(
									url + "/{componentName}/config-file",
									HttpMethod.GET, requestEntity,
									String.class, componentName).toString());
				} else if ("update config file".equals(action)) {
					HttpHeaders updateRequestHeaders = new HttpHeaders();
					List<MediaType> updateMediaTypes = new ArrayList<MediaType>();
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
				} else if ("delete config file".equals(action)) {
					restTemplate.delete(url + "/{componentName}/config-file",
							componentName);
				} else if ("get content file path".equals(action)) {
					getLog().info(
							restTemplate.getForObject(
									url + "/{componentName}/content-files",
									LinkedHashMap.class, componentName)
									.toString());
				} else if ("get content file path with regexp".equals(action)) {
					getLog().info(
							restTemplate
									.getForObject(
											url
													+ "/{componentName}/content-files-by-regex?regex={contentFileRegex}",
											LinkedHashMap.class, componentName,
											contentFileRegex).toString());
				} else if ("delete content file path with regex".equals(action)) {
					HashMap<String, String> deleteContentFileParts = new HashMap<String, String>();
					deleteContentFileParts.put("name", "regex");
					deleteContentFileParts.put("value", contentFileRegex);
					HttpHeaders deleteContentFileRequestHeaders = new HttpHeaders();
					List<MediaType> deleteContentFileMediaTypes = new ArrayList();
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
				} else if ("add content file".equals(action)) {
					for (Archive contentFile : contentFiles) {
						MultiValueMap<String, Object> addContentFileParts = new LinkedMultiValueMap();
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
				} else if ("get http-urls".equals(action)) {
					LinkedHashMap<Object, Object> getHttpUrl = restTemplate
							.getForObject(url + "/{component}/http-urls",
									LinkedHashMap.class, componentName);
					getLog().info(getHttpUrl.get("result").toString());
				} else if ("delete http-urls".equals(action)) {
					getLog().info(
							restTemplate.exchange(
									url + "/{component}/http-urls",
									HttpMethod.DELETE, null, String.class,
									componentName).toString());
				} else if ("add http-urls".equals(action)) {
					List<String> addHttpUrlsRequest = new ArrayList();
					for (String relativeURL : relativeURLs) {
						addHttpUrlsRequest.add(relativeURL);
					}
					getLog().info(
							restTemplate.postForObject(
									url + "/{component}/http-urls",
									addHttpUrlsRequest, String.class,
									componentName).toString());
				} else if ("update http-urls".equals(action)) {
					List<String> updateHttpUrlsRequest = new ArrayList();
					for (String relativeURL : relativeURLs) {
						updateHttpUrlsRequest.add(relativeURL);
					}
					restTemplate.put(url + "/{component}/http-urls",
							updateHttpUrlsRequest, componentName);
					getLog().info("http urls updated!");
				} else if ("auto-detect http-urls".equals(action)) {
					restTemplate.postForObject(url
							+ "/{component}/http-urls/auto-detect", null,
							String.class, componentName);
					getLog().info(
							restTemplate.postForObject(
									url + "/{component}/http-urls/auto-detect",
									null, String.class, componentName)
									.toString());
				} else if ("get patches".equals(action)) {
					LinkedHashMap<Object, Object> getPatches = restTemplate
							.getForObject(url + "/{component}/patches",
									LinkedHashMap.class, componentName);
					getLog().info(getPatches.get("result").toString());
				} else if ("get script-files".equals(action)) {
					LinkedHashMap<Object, Object> getScriptFiles = restTemplate
							.getForObject(url + "/{component}/script-files",
									LinkedHashMap.class, componentName);
					getLog().info(getScriptFiles.get("result").toString());
				} else if ("add script-files".equals(action)) {
					MultiValueMap<String, Object> addScriptFilesParts = new LinkedMultiValueMap();
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
				} else if ("get script-files content".equals(action)) {
					HttpHeaders getScriptFileHeaders = new HttpHeaders();
					List<MediaType> mediaTypesScriptFile = new ArrayList();
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
				} else if ("update script-files content".equals(action)) {
					HttpHeaders updateScriptFileRequestHeaders = new HttpHeaders();
					updateScriptFileRequestHeaders
							.setContentType(MediaType.TEXT_PLAIN);
					List<MediaType> scriptFileMediaTypeList = new ArrayList();
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
				} else if ("remove script-files".equals(action)) {
					getLog().info(
							restTemplate
									.exchange(
											url
													+ "/{componentName}/script-files/{script-name}",
											HttpMethod.DELETE, null,
											String.class, componentName,
											scriptName).toString());
				} else if ("get script-files path with regex".equals(action)) {
					getLog().info(
							restTemplate
									.getForObject(
											url
													+ "/{componentName}/script-files-by-regex?regex={scriptFileRegex}",
											LinkedHashMap.class, componentName,
											scriptFileRegex).toString());
				} else if ("delete script-files path with regex".equals(action)) {
					HashMap<String, String> deleteScriptFileParts = new HashMap();
					deleteScriptFileParts.put("name", "regex");
					deleteScriptFileParts.put("value", scriptFileRegex);
					HttpEntity<?> deleteScriptFileRequestEntity = new HttpEntity(
							deleteScriptFileParts);
					getLog().info(
							restTemplate.postForObject(url
									+ "/{componentName}/script-files-by-regex",
									deleteScriptFileRequestEntity,
									String.class, componentName));
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
						for (String _componentName : componentNameList) {
							getLog().info(_componentName);
						}
					} else {
						getLog().warn("Status = " + status);
						getLog().warn(response.get("result").toString());
					}
				} else if ("clean".equals(action)) {
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
						for (String _componentName : componentNameList) {
							restTemplate.put(url
									+ "/{componentName}/published/false", null,
									_componentName);
							restTemplate.delete(url + "/" + "{componentName}",
									_componentName);
							getLog().info(componentName + "removed!");
						}
					} else {
						getLog().warn("Status = " + getStatus);
						getLog().warn(getResponse.get("result").toString());
					}
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
	 * 
	 * @return
	 */
	protected HashMap<Object, Object> setComponentRequest(Component component) {
		HashMap<Object, Object> request = new LinkedHashMap<Object, Object>();

		request.put("componentType", component.getComponentType());
		request.put("name", component.getName());
		request.put("enablerName", component.getEnablerName());
		request.put("enablerVersion", component.getEnablerVersion());
		request.put("description", component.getDescription());
		request.put("trackedStatistics", component.getTrackedStatistics());
		request.put("options",  component.getOptions());
		request.put("runtimeContextVariables", component.getRuntimeContextVariables());
		request.put("features",component.getFeatures());
		request.put("defaultAllocationRuleSettings",component.getDefaultAllocationRuleSettings());
		request.put("defaultSettings", component.getDefaultSettings());
		request.put(
				"allocationConstraints",component.getAllocationConstraints() );
		return request;
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

	/**
	 * @return the component
	 */
	public final Component getComponent() {
		return component;
	}

	/**
	 * @param component the component to set
	 */
	public final void setComponent(Component component) {
		this.component = component;
	}

}