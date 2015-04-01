/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;

public abstract class AbstractSilverFabricMojo extends AbstractMojo {

    ApplicationContext ctx = new AnnotationConfigApplicationContext(SilverFabricConfig.class);

    public RestTemplate restTemplate = ctx.getBean(RestTemplate.class);

    HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = (HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory();
    DefaultHttpClient httpClient = (DefaultHttpClient) httpComponentsClientHttpRequestFactory.getHttpClient();

    @Parameter (required = true)
    private BrokerConfig brokerConfig;

    @Parameter
    private LinkedList<String> actions;

    public LinkedList<String> getActions() {
        return actions;
    }

    public void setActions(LinkedList<String> actions) {
        this.actions = actions;
    }

    public BrokerConfig getBrokerConfig() {
        return brokerConfig;
    }

    public void setBrokerConfig(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(brokerConfig.getBrokerURL().getHost(),brokerConfig.getBrokerURL().getPort(),AuthScope.ANY_REALM),new UsernamePasswordCredentials(brokerConfig.getUsername(), brokerConfig.getPassword()));
        executeMojo();
    }

    public abstract void executeMojo() throws MojoExecutionException, MojoFailureException;
}
