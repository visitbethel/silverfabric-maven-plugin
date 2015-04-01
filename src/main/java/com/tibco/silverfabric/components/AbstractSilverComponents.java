/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric.components;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.model.PluginContainer;
import org.apache.maven.plugin.AbstractMojo;
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

import com.fedex.scm.sf.Component;
import com.tibco.silverfabric.AbstractSilverFabricMojo;
import com.tibco.silverfabric.Archive;
import com.tibco.silverfabric.Components;
import com.tibco.silverfabric.DefaultAllocationSetting;
import com.tibco.silverfabric.DefaultSetting;
import com.tibco.silverfabric.Feature;
import com.tibco.silverfabric.Option;
import com.tibco.silverfabric.RuntimeContextVariable;

/**
 * Actions related to components.
 *
 * @get: Queries the Components for names, all info, or blacklisted_names. This is the default action if action is not set.
 * * The parameters you can use in that case are:
 * * info (names, all, blacklisted_names)
 * * type name of type (i.e.: J2EE, "TIBCO ActiveMatrix BusinessWorks:2.0.0")
 * * engineId (only if info=blacklisted_names)
 * * instance (only if info=blacklisted_names)
 */
public abstract class AbstractSilverComponents extends Components {

	@Parameter(required = true)
	protected File plan;

	
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
    @Parameter
    private boolean override = false;

	/**
     * 
     */
	private Component component;    
    
	public void initialize() {
		getLog().info("loading plan " + this.plan);
		JAXBElement<Component> _component = (JAXBElement<Component>) marshaller
				.unmarshal(new StreamSource(this.plan));
		component = _component.getValue();
	}

    
    public void executeMojo() throws MojoExecutionException, MojoFailureException {
    	
    	initialize();

        List<String> actionList = getActions() != null ? getActions() : new ArrayList<String>();
        if (actionList.isEmpty()) actionList.add("get");
        String url = getBrokerConfig().getBrokerURL().toString() + "/livecluster/rest/v1/sf/components";
        getLog().debug(url);

        for (String action : actionList) {
            if (!action.equals("clean") && !action.equals("get") && !action.equals("get types") && !action.equals("get type names") && (componentName == null || componentName.isEmpty()))
                throw new MojoFailureException("The parameter \"componentName\" is required by the component action: " + action);
            try {
                switch (action) {
                    case "create":
                        Map<Object, Object> mapCreate = setComponentRequest();
                        if (mapCreate == null)
                            throw new MojoFailureException("The following parameters are required to create a component: enablerName, enablerVersion, componentType");
                        String result = "";
                        result = restTemplate.postForObject(url, mapCreate, String.class);
                        getLog().info(result.toString());
                        break;
                    case "publish":
                        restTemplate.put(url + "/{componentName}/published/true", null, componentName);
                        getLog().info(componentName + " published!");
                        break;
                    case "unpublish":
                        restTemplate.put(url + "/{componentName}/published/false", null, componentName);
                        getLog().info(componentName + " unpublished!");
                        break;
                    case "update":
                        Map<Object, Object> mapUpdate = setComponentRequest();
                        if (mapUpdate == null)
                            throw new MojoFailureException("The following parameters are required to " + action + " a component: enablerName, enablerVersion, componentType");
                        restTemplate.put(url + "/{componentName}", mapUpdate, componentName);
                        getLog().info(componentName + " updated!");
                        break;
                    case "delete":
                        restTemplate.delete(url + "/" + "{componentName}", componentName);
                        getLog().info(componentName + " deleted!");
                        break;
                    case "get info":
                        LinkedHashMap<String, LinkedHashMap<String, Object>> infoLinkedHashMap;
                        infoLinkedHashMap = restTemplate.getForObject(url + "/{componentName}", LinkedHashMap.class, componentName);
                        getLog().info(infoLinkedHashMap.get("result").get("value").toString());
                        break;
                    case "get archives":
                        getLog().info(restTemplate.getForObject(url + "/{componentName}/archives", LinkedHashMap.class, componentName).toString());
                        break;
                    case "add archives":
                        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
                        for (Archive archive : archives) {
                            parts.add("archiveFile", new FileSystemResource(archive.getPath() + "/" + archive.getName()));
                        }
                        String addArchivesResponse = restTemplate.postForObject(url + "/{componentName}/archives", parts, String.class, componentName);
                        getLog().info(addArchivesResponse);
                        break;
                    case "remove archive":
                        restTemplate.delete(url + "/{componentName}/archives/{archive}", componentName, archives.get(0).getName());
                        getLog().info("Archive deleted!");
                        break;
                    case "remove archives":
                        restTemplate.delete(url + "/{componentName}/archives", componentName);
                        getLog().info("Archives deleted!");
                        break;
                    case "assign to non cloud":
                        Map<String, String> accountNameMap = new HashMap<>();
                        accountNameMap.put("name", "name");
                        accountNameMap.put("value", accountName);
                        result = restTemplate.postForObject(url + "/{componentName}/assign-to-non-cloud", accountNameMap, String.class, componentName);
                        getLog().info(result.toString());
                        break;
                    case "get config file":
                        HttpHeaders requestHeaders = new HttpHeaders();
                        List<MediaType> mediaTypes = new ArrayList<>();
                        mediaTypes.add(MediaType.TEXT_PLAIN);
                        requestHeaders.setAccept(mediaTypes);
                        HttpEntity<?> requestEntity = new HttpEntity(requestHeaders);
                        getLog().info(restTemplate.exchange(url + "/{componentName}/config-file", HttpMethod.GET, requestEntity, String.class, componentName).toString());
                        break;
                    case "update config file":
                        HttpHeaders updateRequestHeaders = new HttpHeaders();
                        List<MediaType> updateMediaTypes = new ArrayList<>();
                        updateMediaTypes.add(MediaType.APPLICATION_JSON);
                        updateRequestHeaders.setAccept(updateMediaTypes);
                        updateRequestHeaders.setContentType(MediaType.TEXT_PLAIN);
                        HttpEntity<?> updateRequestEntity = new HttpEntity(new FileSystemResource(configFile.getPath() + "/" + configFile.getName()), updateRequestHeaders);
                        restTemplate.exchange(url + "/{componentName}/config-file", HttpMethod.PUT, updateRequestEntity, String.class, componentName);
                        getLog().info(componentName + " updated config file!");
                        break;
                    case "delete config file":
                        restTemplate.delete(url + "/{componentName}/config-file", componentName);
                        break;
                    case "get content file path":
                        getLog().info(restTemplate.getForObject(url + "/{componentName}/content-files", LinkedHashMap.class, componentName).toString());
                        break;
                    case "get content file path with regexp":
                        getLog().info(restTemplate.getForObject(url + "/{componentName}/content-files-by-regex?regex={contentFileRegex}", LinkedHashMap.class, componentName, contentFileRegex).toString());
                        break;
                    case "delete content file path with regex":
                        HashMap<String, String> deleteContentFileParts = new HashMap<>();
                        deleteContentFileParts.put("name", "regex");
                        deleteContentFileParts.put("value", contentFileRegex);
                        HttpHeaders deleteContentFileRequestHeaders = new HttpHeaders();
                        List<MediaType> deleteContentFileMediaTypes = new ArrayList<>();
                        deleteContentFileMediaTypes.add(MediaType.APPLICATION_JSON);
                        deleteContentFileRequestHeaders.setAccept(deleteContentFileMediaTypes);
                        deleteContentFileRequestHeaders.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<?> deleteContentFileRequestEntity = new HttpEntity(deleteContentFileParts, deleteContentFileRequestHeaders);
                        restTemplate.exchange(url + "/{componentName}/content-files-by-regex", HttpMethod.POST, deleteContentFileRequestEntity, String.class, componentName);
                        break;
                    case "add content file":
                        for (Archive contentFile : contentFiles) {
                            MultiValueMap<String, Object> addContentFileParts = new LinkedMultiValueMap<>();
                            addContentFileParts.add("contentFile", new FileSystemResource(contentFile.getPath() + "/" + contentFile.getName()));
                            addContentFileParts.add("relativePath", contentFile.getRelativePath());
                            String addContentFileResponse = restTemplate.postForObject(url + "/{componentName}/content-files", addContentFileParts, String.class, componentName);
                            getLog().info(contentFile.getName() + addContentFileResponse);
                        }
                        break;
                    case "get http-urls":
                        LinkedHashMap<Object, Object> getHttpUrl = restTemplate.getForObject(url + "/{component}/http-urls", LinkedHashMap.class, componentName);
                        getLog().info(getHttpUrl.get("result").toString());
                        break;
                    case "delete http-urls":
                        getLog().info(restTemplate.exchange(url + "/{component}/http-urls", HttpMethod.DELETE, null, String.class, componentName).toString());
                        break;
                    case "add http-urls":
                        List<String> addHttpUrlsRequest = new ArrayList<>();
                        for (String relativeURL : relativeURLs) {
                            addHttpUrlsRequest.add(relativeURL);
                        }
                        getLog().info(restTemplate.postForObject(url + "/{component}/http-urls", addHttpUrlsRequest, String.class, componentName).toString());
                        break;
                    case "update http-urls":
                        List<String> updateHttpUrlsRequest = new ArrayList<>();
                        for (String relativeURL : relativeURLs) {
                            updateHttpUrlsRequest.add(relativeURL);
                        }
                        restTemplate.put(url + "/{component}/http-urls", updateHttpUrlsRequest, componentName);
                        getLog().info("http urls updated!");
                        break;
                    case "auto-detect http-urls":
                        restTemplate.postForObject(url + "/{component}/http-urls/auto-detect", null, String.class, componentName);
                        getLog().info(restTemplate.postForObject(url + "/{component}/http-urls/auto-detect", null, String.class, componentName).toString());
                        break;
                    case "get patches":
                        LinkedHashMap<Object, Object> getPatches = restTemplate.getForObject(url + "/{component}/patches", LinkedHashMap.class, componentName);
                        getLog().info(getPatches.get("result").toString());
                        break;
                    case "get script-files":
                        LinkedHashMap<Object, Object> getScriptFiles = restTemplate.getForObject(url + "/{component}/script-files", LinkedHashMap.class, componentName);
                        getLog().info(getScriptFiles.get("result").toString());
                        break;
                    case "add script-files":
                        MultiValueMap<String, Object> addScriptFilesParts = new LinkedMultiValueMap<>();
                        addScriptFilesParts.add("scriptFile", new FileSystemResource(scriptFile.getPath() + "/" + scriptFile.getName()));
                        addScriptFilesParts.add("scriptLang", scriptLang);
                        addScriptFilesParts.add("scriptLangVersion", scriptLangVersion);
                        String addScriptFilesResponse = restTemplate.postForObject(url + "/{componentName}/script-files", addScriptFilesParts, String.class, componentName);
                        getLog().info(addScriptFilesResponse);
                        break;
                    case "get script-files content":
                        HttpHeaders getScriptFileHeaders = new HttpHeaders();
                        List<MediaType> mediaTypesScriptFile = new ArrayList<>();
                        mediaTypesScriptFile.add(MediaType.TEXT_PLAIN);
                        getScriptFileHeaders.setAccept(mediaTypesScriptFile);
                        HttpEntity<?> getScriptFileEntity = new HttpEntity(getScriptFileHeaders);
                        getLog().info(restTemplate.exchange(url + "/{componentName}/script-files/{script-name}", HttpMethod.GET, getScriptFileEntity, String.class, componentName, scriptName).toString());
                        break;
                    case "update script-files content":
                        HttpHeaders updateScriptFileRequestHeaders = new HttpHeaders();
                        updateScriptFileRequestHeaders.setContentType(MediaType.TEXT_PLAIN);
                        List<MediaType> scriptFileMediaTypeList = new ArrayList<>();
                        scriptFileMediaTypeList.add(MediaType.APPLICATION_JSON);
                        updateScriptFileRequestHeaders.setAccept(scriptFileMediaTypeList);
                        HttpEntity<?> updateScriptFileRequestEntity = new HttpEntity(new FileSystemResource(scriptFile.getPath() + "/" + scriptFile.getName()), updateScriptFileRequestHeaders);
                        restTemplate.exchange(url + "/{componentName}/script-files/{script-name}", HttpMethod.PUT, updateScriptFileRequestEntity, String.class, componentName, scriptName);
                        break;
                    case "remove script-files":
                        getLog().info(restTemplate.exchange(url + "/{componentName}/script-files/{script-name}", HttpMethod.DELETE, null, String.class, componentName, scriptName).toString());
                        break;
                    case "get script-files path with regex":
                        getLog().info(restTemplate.getForObject(url + "/{componentName}/script-files-by-regex?regex={scriptFileRegex}", LinkedHashMap.class, componentName, scriptFileRegex).toString());
                        break;
                    case "delete script-files path with regex":
                        HashMap<String, String> deleteScriptFileParts = new HashMap<>();
                        deleteScriptFileParts.put("name", "regex");
                        deleteScriptFileParts.put("value", scriptFileRegex);
                        HttpEntity<?> deleteScriptFileRequestEntity = new HttpEntity(deleteScriptFileParts);
                        getLog().info(restTemplate.postForObject(url + "/{componentName}/script-files-by-regex", deleteScriptFileRequestEntity, String.class, componentName));
                        break;
                    case "get type names":
                        getLog().info(restTemplate.getForObject(url + "/type-names", LinkedHashMap.class).get("result").toString());
                        break;
                    case "get types":
                        getLog().info(restTemplate.getForObject(url + "/types", LinkedHashMap.class).get("result").toString());
                        break;
                    case "get":
                        String getRequest = url + "?info=" + info;
                        if (type != null && !type.isEmpty()) getRequest += "&type=" + type;
                        if (engineId != 0) getRequest += "&engineId=" + engineId;
                        if (instance != null && !instance.isEmpty()) getRequest += "&instance=" + instance;
                        getLog().debug(getRequest);
                        LinkedHashMap<Object, Object> response = restTemplate.getForObject(getRequest, LinkedHashMap.class);
                        Integer status = (Integer) response.get("status");
                        if (status == 200 && ((LinkedHashMap<Object, Object>) (response.get("result"))).get("name").toString().equals("componentNames")) {
                            ArrayList<String> componentNameList = (ArrayList<String>) ((LinkedHashMap<Object, Object>) (response.get("result"))).get("value");
                            for (String componentName : componentNameList) {
                                getLog().info(componentName);
                            }
                        } else {
                            getLog().warn("Status = " + status);
                            getLog().warn(response.get("result").toString());
                        }
                        break;
                    case "clean":
                        LinkedHashMap<Object, Object> getResponse = restTemplate.getForObject(url + "?info=names", LinkedHashMap.class);
                        Integer getStatus = (Integer) getResponse.get("status");
                        if ( getStatus == 200 && ((LinkedHashMap<Object, Object>) (getResponse.get("result"))).get("name").toString().equals("componentNames")) {
                            ArrayList<String> componentNameList = (ArrayList<String>) ((LinkedHashMap<Object, Object>) (getResponse.get("result"))).get("value");
                            for (String componentName : componentNameList) {
                                restTemplate.put(url + "/{componentName}/published/false", null, componentName);
                                restTemplate.delete(url + "/" + "{componentName}", componentName);
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
                getLog().info("Error when running " + action + " on component " + componentName + " : " + httpException.getResponseBodyAsString());
                throw new MojoExecutionException("Error when running " + action 
                		+ " on component " + componentName + " : " + httpException.getResponseBodyAsString(), httpException);
            }
        }
    }


	/**
	 * 
	 * @return
	 */
	protected HashMap<Object, Object> setComponentRequest() {
		HashMap<Object, Object> request = new LinkedHashMap<Object, Object>();
		request.put("componentType",
				valueOf(component.getComponentType(), componentType));
		request.put("name", componentName);
		request.put("enablerName",
				valueOf(component.getEnablerName(), enablerName));
		request.put("enablerVersion",
				valueOf(component.getEnablerVersion(), enablerVersion));
		valueOf(request, "description", description, null);
		valueOf(request, "trackedStatistics", trackedStatistics, null);
		valueOf(request, "options", component.getOptions(), options);
		valueOf(request, "runtimeContextVariables",
				component.getRuntimeVariables(), runtimeContextVariables);
		valueOf(request, "features", component.getFeatures(), features);
		valueOf(request, "defaultAllocationRuleSettings",
				defaultAllocationRuleSettings, null);
		valueOf(request, "defaultSettings", defaultSettings, null);
		valueOf(request, "allocationConstraints", allocationConstraints, component.getAllocationConstraints());
		return request;
	}

	/**
	 * 
	 * @param request
	 * @param string
	 * @param description2
	 * @param object
	 */
	protected Object valueOf(HashMap<Object, Object> request, String string,
			Object a, Object b) {
		Object value = override && "features".equals(string) ? valueOf(b, a) : valueOf(a, b);
		if (value != null) {
			getLog().info("\n\nadding[" + string + "] \n\n\t= " + value );
			request.put(string, value);
			return value;
		}
		return null;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	protected static Object valueOf(Object a, Object b) {
		if (a == null && b == null) {
			return null;
		}
		if (a == null) {
			return b;
		} else {
			return a;
		}
	}

}