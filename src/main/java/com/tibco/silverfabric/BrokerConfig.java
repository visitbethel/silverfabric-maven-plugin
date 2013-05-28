package com.tibco.silverfabric;

import java.net.URL;

/**
 * User: franck
 * Date: 1/15/13
 */
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
}
