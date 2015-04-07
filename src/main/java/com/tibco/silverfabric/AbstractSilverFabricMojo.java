/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric;

import java.util.HashMap;
import java.util.LinkedList;

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
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractSilverFabricMojo extends AbstractMojo {

    /**
	 * 
	 * @param request
	 * @param string
	 * @param description2
	 * @param object
	 */
	protected static Object valueOf(HashMap<Object, Object> request, String string, Object a,
			Object b) {
				Object value = valueOf(a, b);
				if (value != null) {
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

	ApplicationContext ctx = new AnnotationConfigApplicationContext(SilverFabricConfig.class);

    public RestTemplate restTemplate = ctx.getBean(RestTemplate.class);


	/**
	 * 
	 */
	protected Jaxb2Marshaller marshaller = ctx.getBean(Jaxb2Marshaller.class);

	
    
    
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
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = (HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory();
        DefaultHttpClient httpClient = (DefaultHttpClient) httpComponentsClientHttpRequestFactory.getHttpClient();
    	
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(brokerConfig.getBrokerURL().getHost(),brokerConfig.getBrokerURL().getPort(),AuthScope.ANY_REALM),new UsernamePasswordCredentials(brokerConfig.getUsername(), brokerConfig.getPassword()));
        executeMojo();
    }

    public abstract void executeMojo() throws MojoExecutionException, MojoFailureException;

	/**
	 * @return the restTemplate
	 */
	public final RestTemplate getRestTemplate() {
		return restTemplate;
	}
}
