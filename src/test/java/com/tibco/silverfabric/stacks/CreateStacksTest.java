package com.tibco.silverfabric.stacks;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tibco.silverfabric.BrokerConfig;
import com.tibco.silverfabric.Utils;
import com.tibco.silverfabric.components.CreateComponents;
import com.tibco.silverfabric.components.CreateComponentsTest;
import com.tibco.silverfabric.components.DeleteComponents;

public class CreateStacksTest {

	private static final String PREFIX = "__UNIT-STACK";

	private String componentName = null;

	private boolean skipStackCleanup = false;

	File cplan;
	File splan;

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
		cplan = Utils.getTestFile(CreateComponentsTest.class, 6, "xml");

		CreateComponents c = new CreateComponents(config, cplan);
		c.setBrokerConfig(config);
		c.setComponentName(PREFIX + "-" + c.getClass().getSimpleName());
		componentName = c.getComponentName();
		assertNotNull(c.restTemplate);
		try {
			c.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if (!e.getMessage().contains("already exists")) {
				fail(e.getMessage());
				skipStackCleanup = true;
			}
		}

	}

	@After
	public void cleanup() {
		System.err.println("\n=========== CLEANUP ===================\n");
		Exception ex = null;
		if (skipStackCleanup) {
			DeleteStacks d = new DeleteStacks(config, splan);
			d.setBrokerConfig(config);
			d.plan = splan;
			d.setStackName(PREFIX + "-" + this.getClass());
			try {
				d.execute();
			} catch (Exception e) {
				ex = e;
			}
		}
		DeleteComponents dc = new DeleteComponents(config, cplan);
		dc.setComponentName(componentName);
		try {
			dc.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
		if (ex != null) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}

	}

	@Test
	public void testCreateStacksTest1() throws MojoExecutionException,
			MojoFailureException {

		splan = Utils.getTestFile(CreateStacksTest.class, 1, "xml");
		CreateStacks c = new CreateStacks(config, splan);
		c.setStackName(PREFIX + "-" + this.getClass());
		c.setComponents(Arrays.asList(new String[] { componentName }));

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
