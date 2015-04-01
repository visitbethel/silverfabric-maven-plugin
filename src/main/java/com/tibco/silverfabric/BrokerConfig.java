/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric;

import java.net.URL;

public class BrokerConfig {

    private URL brokerURL;
    private String username;
    private String password;

    public URL getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(URL brokerURL) {
        this.brokerURL = brokerURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BrokerConfig [brokerURL=" + brokerURL + ", username="
				+ username + ", password=" + password + "]";
	}
}
