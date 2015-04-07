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

import com.tibco.silverfabric.Archive;
import com.tibco.silverfabric.BrokerConfig;
import com.tibco.silverfabric.Utils;
import com.tibco.silverfabric.model.Plan;

public class CanPostTransformTest {

	private static final String PREFIX = "__UNIT-TEST";

	Plan plan;

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

	//@After
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

	private void executeCreateComponent(Plan planz, CreateComponentsJSON c) {
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
	 * With allocations
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Test
	public void testCreateComponentsForCanPostTransform1() throws MojoExecutionException,
			MojoFailureException {
		CreateComponentsJSON c = new CreateComponentsJSON();

		plan = new Plan();
		plan.componentTemplateURI = Utils.getTestFile(CanPostTransformTest.class, 1, ".component.json").getAbsolutePath();
		plan.stackTemplateURI = Utils.getTestFile(CanPostTransformTest.class, 1, ".stack.json").getAbsolutePath();
		
		File f = new File("/Users/akaan/STAGING/shipmentefs-CanPostTransformer/DEV/NA/", "CanPostTransformer.ear.zip");
		c.getArchives().add(new Archive(f));
		executeCreateComponent(plan, c);
	}	

}
