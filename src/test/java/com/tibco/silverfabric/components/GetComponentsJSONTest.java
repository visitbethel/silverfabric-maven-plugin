package com.tibco.silverfabric.components;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tibco.silverfabric.BrokerConfig;
import com.tibco.silverfabric.Utils;

public class GetComponentsJSONTest {

	private static final String PREFIX = "__UNIT-TEST";

	File plan;
	String componentName = "";

	BrokerConfig config;

	@Before
	public void setup() {
		componentName = Utils.getEntityName(this.getClass(), Utils.PREFIX_COMPONENT);
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
		plan = Utils.getTestFile(CreateComponentsJSONTest.class, 1, "json");
		CreateComponentsJSON c = new CreateComponentsJSON();
		c.setBrokerConfig(config);
		c.plan = plan;
		c.setComponentName(componentName);

		assertNotNull(c.restTemplate);
		try {
			c.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if (!e.getMessage().contains("already exists")) {
				fail(e.getMessage());
			}
		}		
		
	}

	@After
	public void cleanup() {
		System.err.println("\n=========== CLEANUP ===================\n");
		DeleteComponentsJSON d = new DeleteComponentsJSON();
		d.setBrokerConfig(config);
		d.plan = plan;
		d.setComponentName(componentName);
		try {
			d.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetComponentsFeatures1() throws MojoExecutionException,
			MojoFailureException {
		plan = Utils.getTestFile(CreateComponentsJSONTest.class, 1, "json");
		executeGetComponent(plan);
	}

	private void executeGetComponent(File planz) {
		GetComponentsJSON c = new GetComponentsJSON(config);
		c.setComponentName(componentName);
		c.setComponentName("ASMetaspace-SU_ADDRESS_TAPAS_agent-1");
		c.setComponentName("ag_SUShipment_TAPTRA_be_CachePU-2");
		assertNotNull(c.restTemplate);
		try {
			c.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if (!e.getMessage().contains("already exists")) {
				fail(e.getMessage());
			}
		}		

	}	

}
