package com.tibco.silverfabric;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;

import com.tibco.silverfabric.model.Plan;

public class DeployApplicationStackTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws MalformedURLException, MojoExecutionException, MojoFailureException {

		Plan p = new Plan();
		p.componentPlan = "src/test/resources/GSEFSAdminSetupTest1.component.json";
		p.stackPlan = "src/test/resources/GSEFSAdminSetupTest1.stack.json";
		URL broker = new URL("http://irh00610.ute.fedex.com:8080");
		BrokerConfig config = new BrokerConfig(broker, "sefsdev_operate",
				"test123");

		DeployApplicationStack deploy = new DeployApplicationStack();
		deploy.brokerConfig = config;
		deploy.plan = p;
		deploy.execute();

	}

}
