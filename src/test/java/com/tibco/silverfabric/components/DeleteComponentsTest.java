package com.tibco.silverfabric.components;

import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.tibco.silverfabric.BrokerConfig;

public class DeleteComponentsTest {

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
		DeleteComponents d = new DeleteComponents();
		d.setBrokerConfig(config);
		d.plan = plan;
		d.setComponentName("__UNIT-TEST-" + CreateComponentsTest.class);
		try {
			d.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
