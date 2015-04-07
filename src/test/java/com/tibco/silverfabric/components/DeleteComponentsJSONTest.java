package com.tibco.silverfabric.components;

import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.tibco.silverfabric.BrokerConfig;
import com.tibco.silverfabric.Utils;

public class DeleteComponentsJSONTest {

	File plan;
	
	BrokerConfig config;
	
	@Before
	public void setup() {
		config = new BrokerConfig();
		config.setUsername("sefsdev_operate");
		config.setPassword("test123");
		try {
			config.setBrokerURL(new URL("http://irh00610.ute.fedex.com:8080"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void cleanup() {
		System.err.println("\n=========== CLEANUP ===================\n");
		DeleteComponentsJSON d = new DeleteComponentsJSON();
		d.setBrokerConfig(config);
		d.plan = plan;
		d.setComponentName(Utils.getEntityName(CreateComponentsJSONTest.class, Utils.PREFIX_COMPONENT));
		try {
			d.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
