/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;

import java.io.*;
import java.util.*;

/**
 * Actions related to gridlibs.
 *
 * @get: Returns Grid Libraries registered with the primary Silver Fabric Broker. Note that this only returns published Grid Libraries. If you upload a new Grid Library and don't publish it first, it will not be listed.
 */
@Mojo(name = "gridlibs")
public class Gridlibs extends AbstractSilverFabricMojo {

    private static final RequestCallback ACCEPT_CALLBACK =
            new RequestCallback()
            {
                @Override
                public void doWithRequest ( ClientHttpRequest request ) throws IOException
                {
                    request.getHeaders().set( "Accept", "application/zip" );
                }
            };

    private static class FileResponseExtractor implements ResponseExtractor<Object>
    {
        private final File file;
        private       File file () { return this.file; }

        private FileResponseExtractor ( File file )
        {
            this.file = file;
        }

        @Override
        public Object extractData ( ClientHttpResponse response ) throws IOException
        {
            InputStream  is = response.getBody();
            OutputStream os = new BufferedOutputStream( new FileOutputStream( file()));

            IOUtils.copyLarge( is, os );
            IOUtils.closeQuietly( is );
            IOUtils.closeQuietly(os);

            return null;
        }
    }





    @Parameter(defaultValue = "all")
    private String getType;
    @Parameter(defaultValue = "names")
    private String getInfo;
    @Parameter
    private String gridlibraryName;
    @Parameter
    private String gridlibraryVersion;
    @Parameter(defaultValue = "all")
    private String os;
    @Parameter
    private String distributionName;
    @Parameter(defaultValue = "false")
    private String overWrite;
    @Parameter
    private List<Archive> archives;

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
        String url = getBrokerConfig().getBrokerURL().toString() + "/livecluster/rest/v1/sf/gridlibs";
        getLog().debug(url);

        for (String action : actionList) {
            if (!action.equals("clean") && !action.equals("get") && !action.equals("add") && (gridlibraryName == null || gridlibraryName.isEmpty()))
                throw new MojoFailureException("The parameter \"stackName\" is required by the stack action: " + action);
            try {
                switch (action) {
                    case "download":
                        Map<String, String> map = downloadUrl(url);
                        restTemplate.execute(map.get("gridlibDownloadURL"), HttpMethod.GET, ACCEPT_CALLBACK, new FileResponseExtractor(new File(map.get("filename"))));
                        break;
                    case "upload probe enabler":
                        restTemplate.postForObject(url + "/{component}/http-urls/auto-detect", null, String.class);
                        getLog().info(restTemplate.postForObject(url + "/{component}/http-urls/auto-detect", null, String.class).toString());
                        break;
                    case "upload probe component":
                        LinkedHashMap<Object, Object> getPatches = restTemplate.getForObject(url + "/{component}/patches", LinkedHashMap.class);
                        getLog().info(getPatches.get("result").toString());
                        break;
                    case "upload probe distribution":
                        LinkedHashMap<Object, Object> distributionProbeResponse = restTemplate.getForObject(url + "/archives/distro-upload-test?filename={distributionName}&overwrite={overWrite}", LinkedHashMap.class, distributionName, overWrite);
                        getLog().info(distributionProbeResponse.get("result").toString());
                        break;
                    case "add":
                        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
                        for (Archive archive : archives) {
                            parts.add("gridlibArchive", new FileSystemResource(archive.getPath() + "/" + archive.getName()));
                            parts.add("gridlibOverwrite", overWrite);
                        }
                        String addArchivesResponse = restTemplate.postForObject(url + "/archives", parts, String.class);
                        getLog().info(addArchivesResponse);break;
                    case "delete":
                        getLog().info(restTemplate.getForObject(url + "/types", LinkedHashMap.class).get("result").toString());
                        break;
                    case "get":
                        LinkedHashMap<String, Object> response = restTemplate.getForObject(url, LinkedHashMap.class);
                        Integer status = (Integer) response.get("status");
                        if (status == 200) {
                            LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) response.get("result");
                            List<LinkedHashMap<String, Object>> gridlibs = (List<LinkedHashMap<String, Object>>) result.get("value");
                            for (LinkedHashMap<String, Object> gridlib : gridlibs) {
                                if (getType.equals("distributions")) {
                                    if ((boolean)(gridlib.get("distribution"))) {
                                        if (getInfo.equals("names")) getLog().info(gridlib.get("name").toString());
                                        else getLog().info(gridlib.toString());
                                    }
                                } else if (getType.equals("components")) {
                                    if ((boolean)(gridlib.get("component"))) {
                                        if (getInfo.equals("names")) getLog().info(gridlib.get("name").toString());
                                        else getLog().info(gridlib.toString());
                                    }
                                } else if (getType.equals("enablers")) {
                                    if (!(boolean)(gridlib.get("component")) && !(boolean)(gridlib.get("distribution"))) {
                                        if (getInfo.equals("names")) getLog().info(gridlib.get("name").toString());
                                        else getLog().info(gridlib.toString());
                                    }
                                } else {
                                    if (getInfo.equals("names")) getLog().info(gridlib.get("name").toString());
                                    else getLog().info(gridlib.toString());
                                }
                            }
                        } else {
                            getLog().warn("Status = " + status);
                            getLog().warn(response.get("result").toString());
                        }
                        break;
                    case "clean":
                        LinkedHashMap<Object, Object> getResponse = restTemplate.getForObject(url, LinkedHashMap.class);
                        Integer getStatus = (Integer) getResponse.get("status");
                        if (getStatus == 200) {
                            LinkedHashMap<Object, Object> result = (LinkedHashMap<Object, Object>) getResponse.get("result");
                            List<LinkedHashMap<String, String>> stacksInfo = (List<LinkedHashMap<String, String>>) result.get("value");
                            for (LinkedHashMap<String, String> stackInfo : stacksInfo) {
                                restTemplate.put(url + "/{stackName}/published/false", null, stackInfo.get("name"));
                                restTemplate.delete(url + "/" + "{stackName}", stackInfo.get("name"));
                                getLog().warn(stackInfo.get("name") + "deleted!");
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
            }
        }
    }

    private Map<String,String> downloadUrl(String url) {
        LinkedHashMap<String,Object> downloadUrlResponse = restTemplate.getForObject(url + "/archives/download-url?name={gridlibrayrName}&version={gridlibraryVersion}&os={os}", LinkedHashMap.class, gridlibraryName, gridlibraryVersion, os);
        Integer downloadUrlStatus = (Integer) downloadUrlResponse.get("status");
        if(downloadUrlStatus == 200){
            HashMap<String, String> map = new HashMap<>();
            String str = ((Map<String,String>)(downloadUrlResponse.get("result"))).get("value");
            map.put("filename", str.substring(str.indexOf("archives/") + 9, str.indexOf("?")));
            map.put("gridlibDownloadURL", str);
            return map;
        } else {
            getLog().warn("Status = " + downloadUrlStatus);
            getLog().warn(downloadUrlResponse.get("result").toString());
        }
        return null;
    }

    private HashMap<Object, Object> setStackRequest() {
        HashMap<Object, Object> request = new LinkedHashMap<Object, Object>();
        request.put("icon", icon);
        request.put("description", description);

        if (owner != null) request.put("owner", owner);
        if (propertyOverrides != null) request.put("propertyOverrides", propertyOverrides);
        if (technology != null) request.put("technology", technology);
        if (urls != null) request.put("urls", urls);

        return request;
    }
}