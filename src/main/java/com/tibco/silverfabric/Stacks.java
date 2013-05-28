package com.tibco.silverfabric;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

/**
 * Actions related to stacks.
 *
 * @get: Queries the stacks for names, all info, or blacklisted_names. This is the default action if action is not set.
 * * The parameters you can use in that case are:
 * * info (names, all, blacklisted_names)
 * * type name of type (i.e.: J2EE, "TIBCO Administrator)
 * * engineId (only if info=blacklisted_names)
 * * instance (only if info=blacklisted_names)
 */
@Mojo(name = "stacks")
public class Stacks extends AbstractSilverFabricMojo {
    @Parameter
    private List<String> components;
    @Parameter
    private String stackName;
    @Parameter
    private String mode;
    @Parameter (defaultValue = "--")
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

    public void executeMojo() throws MojoExecutionException, MojoFailureException {

        List<String> actionList = getActions() != null ? getActions() : new ArrayList<String>();
        if (actionList.isEmpty()) actionList.add("get");
        String url = getBrokerConfig().getBrokerURL().toString() + "/livecluster/rest/v1/sf/stacks";
        getLog().debug(url);

        for (String action : actionList) {
            if (!action.equals("clean") && !action.equals("get") && !action.equals("get types") && !action.equals("get type names") && (stackName == null || stackName.isEmpty()))
                throw new MojoFailureException("The parameter \"stackName\" is required by the stack action: " + action);
            try {
                switch (action) {
                    case "create":
                        Map<Object, Object> mapCreate = setStackRequest();
                        if (mapCreate == null) throw new MojoFailureException("The policies and components parameters are required to create a stack");
                        getLog().info(restTemplate.postForObject(url, mapCreate, String.class).toString());
                        break;
                    case "publish":
                        restTemplate.put(url + "/{stackName}/published/true", null, stackName);
                        getLog().info(stackName + " published!");
                        break;
                    case "unpublish":
                        restTemplate.put(url + "/{stackName}/published/false", null, stackName);
                        getLog().info(stackName + " unpublished!");
                        break;
                    case "update":
                        Map<Object, Object> mapUpdate = setStackRequest();
                        if (mapUpdate == null)
                            throw new MojoFailureException("The following parameters are required to " + action + " a component: enablerName, enablerVersion, componentType");
                        restTemplate.put(url + "/{stackName}", mapUpdate, stackName);
                        getLog().info(stackName + " updated!");
                        break;
                    case "delete":
                        restTemplate.delete(url + "/" + "{stackName}", stackName);
                        getLog().info(stackName + " deleted!");
                        break;
                    case "get info":
                        LinkedHashMap<String, LinkedHashMap<String, Object>> infoLinkedHashMap;
                        infoLinkedHashMap = restTemplate.getForObject(url + "/{stackName}", LinkedHashMap.class, stackName);
                        getLog().info(infoLinkedHashMap.get("result").get("value").toString());
                        break;
                    case "assign to non cloud":
                        Map<String, String> accountNameMap = new HashMap<>();
                        accountNameMap.put("name", "name");
                        accountNameMap.put("value", accountName);
                        getLog().info(restTemplate.postForObject(url + "/{stackName}/assign-to-non-cloud", accountNameMap, String.class, stackName).toString());
                        break;
                    case "get config file":
                        HttpHeaders requestHeaders = new HttpHeaders();
                        List<MediaType> mediaTypes = new ArrayList<>();
                        mediaTypes.add(MediaType.TEXT_PLAIN);
                        requestHeaders.setAccept(mediaTypes);
                        HttpEntity<?> requestEntity = new HttpEntity(requestHeaders);
                        getLog().info(restTemplate.exchange(url + "/{stackName}/config-file", HttpMethod.GET, requestEntity, String.class, stackName).toString());
                        break;
                    case "set run mode":
                        restTemplate.put(url + "/{stackName}/mode/{run-mode}", null, stackName, runMode);
                        getLog().info("run mode set to " + runMode + " !");
                        break;
                    case "auto-detect http-urls":
                        restTemplate.postForObject(url + "/{component}/http-urls/auto-detect", null, String.class, stackName);
                        getLog().info(restTemplate.postForObject(url + "/{component}/http-urls/auto-detect", null, String.class, stackName).toString());
                        break;
                    case "get patches":
                        LinkedHashMap<Object, Object> getPatches = restTemplate.getForObject(url + "/{component}/patches", LinkedHashMap.class, stackName);
                        getLog().info(getPatches.get("result").toString());
                        break;
                    case "get script-files":
                        LinkedHashMap<Object, Object> getScriptFiles = restTemplate.getForObject(url + "/{component}/script-files", LinkedHashMap.class, stackName);
                        getLog().info(getScriptFiles.get("result").toString());
                        break;
                    case "get type names":
                        getLog().info(restTemplate.getForObject(url + "/type-names", LinkedHashMap.class).get("result").toString());
                        break;
                    case "get types":
                        getLog().info(restTemplate.getForObject(url + "/types", LinkedHashMap.class).get("result").toString());
                        break;
                    case "get":
                        getLog().info(restTemplate.getForObject(url, LinkedHashMap.class).get("result").toString());
                        break;
                    case "clean":
                        LinkedHashMap<Object, Object> getResponse = restTemplate.getForObject(url, LinkedHashMap.class);
                        Integer status = (Integer) getResponse.get("status");
                        if(status == 200){
                            LinkedHashMap<Object, Object> result = (LinkedHashMap<Object, Object>) getResponse.get("result");
                            List<LinkedHashMap<String, String>> stacksInfo = (List<LinkedHashMap<String, String>>)result.get("value");
                            for(LinkedHashMap<String, String> stackInfo: stacksInfo){
                                restTemplate.put(url + "/{stackName}/published/false", null, stackInfo.get("name"));
                                restTemplate.delete(url + "/" + "{stackName}", stackInfo.get("name"));
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
                getLog().info("Error when running " + action + " on stack " + stackName + " : " + httpException.getResponseBodyAsString());
            }
        }
    }

    private HashMap<Object, Object> setStackRequest() {
        if (policies == null || policies.isEmpty() || components==null || components.isEmpty()) return null;
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
}