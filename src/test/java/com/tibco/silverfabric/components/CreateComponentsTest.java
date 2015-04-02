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

public class CreateComponentsTest {
	

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
	
	//@After
	public void cleanup() {
		System.err.println("\n=========== CLEANUP ===================\n");
		DeleteComponents d = new DeleteComponents();
		d.setBrokerConfig(config);
		d.plan = plan;
		d.setComponentName(PREFIX + "-" + this.getClass());
		try {
			d.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreateComponentsFeatures1() throws MojoExecutionException, MojoFailureException {
		
		plan = new File("src/test/resources/components/CreateComponents-features.xml");
		CreateComponents c = new CreateComponents();
		c.setBrokerConfig(config);
		c.plan = plan;
		c.setComponentName(PREFIX + "-" + this.getClass());
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
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Test
	public void testCreateComponentsFeatures2() throws MojoExecutionException, MojoFailureException {
		
		plan = new File("src/test/resources/components/CreateComponents-features2.xml");
		CreateComponents c = new CreateComponents();
		c.setBrokerConfig(config);
		c.plan = plan;
		c.setComponentName(PREFIX + "-" + this.getClass());
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
	 * With options
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Test
	public void testCreateComponentsFeatures3() throws MojoExecutionException, MojoFailureException {
		
		plan = new File("src/test/resources/components/CreateComponents-features3.xml");
		CreateComponents c = new CreateComponents();
		c.setBrokerConfig(config);
		c.plan = plan;
		c.setComponentName(PREFIX + "-" + this.getClass());
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
	 * With runtimevars.
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Test
	public void testCreateComponentsFeatures4() throws MojoExecutionException, MojoFailureException {
		
		plan = new File("src/test/resources/components/CreateComponents-features4.xml");
		CreateComponents c = new CreateComponents();
		c.setBrokerConfig(config);
		c.plan = plan;
		c.setComponentName(PREFIX + "-" + this.getClass());
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
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Test
	public void testCreateComponentsFeatures5() throws MojoExecutionException, MojoFailureException {
		
		plan = new File("src/test/resources/components/CreateComponents-features5.xml");
		CreateComponents c = new CreateComponents();
		c.setBrokerConfig(config);
		c.plan = plan;
		c.setComponentName(PREFIX + "-" + this.getClass());
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
