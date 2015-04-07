/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric.components;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import com.tibco.silverfabric.BrokerConfig;

/**
 * Actions related to components.
 *
 * @get: Queries the Components for names, all info, or blacklisted_names. This
 *       is the default action if action is not set. * The parameters you can
 *       use in that case are: * info (names, all, blacklisted_names) * type
 *       name of type (i.e.: J2EE, "TIBCO ActiveMatrix BusinessWorks:2.0.0") *
 *       engineId (only if info=blacklisted_names) * instance (only if
 *       info=blacklisted_names)
 */
@Mojo(name = "get-components-json")
public class GetComponentsJSON extends AbstractSilverJSONComponents {

	private boolean exists;

	public GetComponentsJSON() {
		super();
	}

	public GetComponentsJSON(BrokerConfig config) {
		super();
		setBrokerConfig(config);
	}

	public void initialize() throws MojoFailureException {
		super.initialize();
		if (getActions() == null) {
			LinkedList<String> list = new LinkedList<String>();
			list.add("get info");
			setActions(list);
		}
		getLog().info("assign action " + getActions());

		this.getRestTemplate().setErrorHandler(new ResponseErrorHandler() {

			@Override
			public boolean hasError(ClientHttpResponse response)
					throws IOException {
				return GetComponentsJSON.isError(response.getStatusCode());
			}

			@Override
			public void handleError(ClientHttpResponse response)
					throws IOException {
				List<String> list = IOUtils.readLines(response.getBody(),
						"UTF-8");
				exists = list.toString().contains("already exists");
				if (!exists) {
					getLog().info(
							"Response error: " + response.getStatusCode() + " "
									+ response.getStatusText());
					getLog().error(list.toString());
				}

			}
		});
	}

	public boolean exists() {
		return exists;
	}

	public static boolean isError(HttpStatus status) {
		HttpStatus.Series series = status.series();
		return (HttpStatus.Series.CLIENT_ERROR.equals(series) || HttpStatus.Series.SERVER_ERROR
				.equals(series));
	}

}