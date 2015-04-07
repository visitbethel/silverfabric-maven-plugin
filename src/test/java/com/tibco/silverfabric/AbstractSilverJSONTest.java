package com.tibco.silverfabric;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;

import com.tibco.silverfabric.BrokerConfig;
import com.tibco.silverfabric.components.CreateComponentsJSON;
import com.tibco.silverfabric.components.DeleteComponentsJSON;
import com.tibco.silverfabric.components.GetComponentsJSON;
import com.tibco.silverfabric.model.Plan;
import com.tibco.silverfabric.stacks.CreateStacks;

public abstract class AbstractSilverJSONTest {

	public BrokerConfig config;

	public Plan plan;

	public AbstractSilverJSONTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	//@After
	public void cleanup() {
		System.err.println("\n=========== CLEANUP ===================\n");
		executeDeleteComponent(plan);

	}

	/**
	 * 
	 * @param planz
	 * @param c
	 */
	public void executeCreateComponent(Plan planz, CreateComponentsJSON c) {
		c.setBrokerConfig(config);
		c.plan = planz;
		c.setComponentName(Utils.getEntityName(this.getClass(),
				Utils.PREFIX_COMPONENT));

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
	 * 
	 * @param planz
	 * @param c
	 */
	public void executeDeleteComponent(Plan planz) {
		DeleteComponentsJSON d = new DeleteComponentsJSON(config, plan);
		d.setComponentName(Utils.getEntityName(this.getClass(),
				Utils.PREFIX_COMPONENT));
		try {
			d.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param plan
	 * @param s
	 * @throws MojoFailureException 
	 */
	public void executeCreateStack(Plan stackplan, CreateComponentsJSON c) throws MojoFailureException {

		CreateStacks s = new CreateStacks(config, stackplan);
		s.initialize();
		s.setStackName(Utils.PREFIX_STACK + "-" + this.getClass());
		s.setComponents(Arrays.asList(new String[] { c.getComponentName() }));

		System.out.println(s.getStack());
		
		assertNotNull(s.restTemplate);
		try {
			s.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if (e.getMessage() == null || !e.getMessage().contains("already exists")) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		
	}	

	/**
	 * 
	 */
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
		/** remove previous component */
		GetComponentsJSON g = new GetComponentsJSON(config);
		try {
			g.setComponentName(Utils.getEntityName(this.getClass(),
					Utils.PREFIX_COMPONENT));
			g.execute();
		} catch (Exception e) {
			if (g.exists()) {
				System.err.println("component: " + g.getComponentName()
						+ ", already exists! Invoking cleanup before test.");
				cleanup();
			} else {
				e.printStackTrace();
				// fail(e.getMessage());
			}
		}

	}

	/**
	 * @return the config
	 */
	protected final BrokerConfig getConfig() {
		return config;
	}

	/**
	 * @return the plan
	 */
	protected final Plan getPlan() {
		return plan;
	}

}
