/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.maven.plugin.AbstractMojo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class SilverFabricConfig {

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate(
				new HttpComponentsClientHttpRequestFactory());
		restTemplate.getMessageConverters().add(
				new MappingJackson2HttpMessageConverter());
		return restTemplate;
	}

	/**
	 * 
	 * @param mojo
	 * @param source
	 * @param class1
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static <T> T loadingRESTPlan(AbstractMojo mojo, File f,
			Class<T> class1) throws FileNotFoundException {
		mojo.getLog().info("loading JSON plan " + f.getAbsolutePath() + ".");
		if (!f.exists()) {
			throw new FileNotFoundException(f.getAbsolutePath());
		}
		JsonFactory jf = new JsonFactory();
		// jf.enable(JsonParser.Feature.ALLOW_COMMENTS);
		jf.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
		ObjectMapper m = new ObjectMapper();
		T object = null;
		try {
			object = m.readValue(new FileInputStream(f), class1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mojo.getLog().error(e);
		}
		return object;
	}

	/**
	 * 
	 * @param mojo
	 * @param outPlan
	 * @param class1
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static <T> T loadingRESTPlan(AbstractMojo mojo, String source,
			Class<T> class1) throws FileNotFoundException {
		return loadingRESTPlan(mojo, new File(source), class1);
	}

}
