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

public class CreateComponentsJSONTest {

	private static final String PREFIX = "__UNIT-TEST";

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

	@After
	public void cleanup() {
		System.err.println("\n=========== CLEANUP ===================\n");
		DeleteComponentsJSON d = new DeleteComponentsJSON();
		d.setBrokerConfig(config);
		d.plan = plan;
		d.setComponentName(Utils.getEntityName(this.getClass(), Utils.PREFIX_COMPONENT));
		try {
			d.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testCreateComponentsFeatures1() throws MojoExecutionException,
			MojoFailureException {
		plan = Utils.getTestFile(CreateComponentsJSONTest.class, 1, "json");
		executeCreateComponent(plan);
	}

	private void executeCreateComponent(File planz) {
		CreateComponentsJSON c = new CreateComponentsJSON();
		c.setBrokerConfig(config);
		c.plan = planz;
		c.setComponentName(Utils.getEntityName(this.getClass(), Utils.PREFIX_COMPONENT));

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

	/**
	 * With feature properties
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	//@Test
	public void testCreateComponentsFeatures2() throws MojoExecutionException,
			MojoFailureException {

		plan = Utils.getTestFile(CreateComponentsJSONTest.class, 2, "xml");
		executeCreateComponent(plan);

	}

	/**
	 * With options
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	//@Test
	public void testCreateComponentsOptions3() throws MojoExecutionException,
			MojoFailureException {

		plan = Utils.getTestFile(CreateComponentsJSONTest.class, 3, "xml");
		executeCreateComponent(plan);
	}

	/**
	 * With runtimevars.
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	//@Test
	public void testCreateComponentsRuntimeVariables4() throws MojoExecutionException,
			MojoFailureException {

		plan = Utils.getTestFile(CreateComponentsJSONTest.class, 4, "xml");
		executeCreateComponent(plan);
	}

	/**
	 * With allocations
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	//@Test
	public void testCreateComponentsAllocationRule5() throws MojoExecutionException,
			MojoFailureException {

		plan = Utils.getTestFile(CreateComponentsJSONTest.class, 5, "xml");
		executeCreateComponent(plan);

	}
	
	/**
	 * With allocations
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	//@Test
	public void testCreateComponentsDefaultSettings6() throws MojoExecutionException,
			MojoFailureException {

		plan = Utils.getTestFile(CreateComponentsJSONTest.class, 6, "xml");
		executeCreateComponent(plan);
	}	

}
