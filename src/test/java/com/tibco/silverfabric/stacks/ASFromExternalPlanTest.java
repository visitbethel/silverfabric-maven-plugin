package com.tibco.silverfabric.stacks;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;

import com.tibco.silverfabric.AbstractSilverJSONTest;
import com.tibco.silverfabric.DeployApplicationStack;
import com.tibco.silverfabric.DestroyApplicationStack;
import com.tibco.silverfabric.model.Component;
import com.tibco.silverfabric.model.PlanHelper;
import com.tibco.silverfabric.model.Stack;

public class ASFromExternalPlanTest extends AbstractSilverJSONTest {

	@Before
	public void setup() {
		setBrokerConfig();

	}

	/**
	 * With allocations
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Test
	public void testCreateStackByExternalPlan()
			throws MojoExecutionException, MojoFailureException {

		PlanHelper helper = new PlanHelper();

		Component cs = helper.loadComponentTemplate(
				"src/test/resources/ASFromExternalPlanTest",
				"ASFromExternalPlanTest1.component.json");
		assertNotNull(cs);
		//cs.setName("UNITTEST");
		Stack ss = helper.loadStackTemplate(
				"src/test/resources/ASFromExternalPlanTest",
				"ASFromExternalPlanTest1.stack.json");
		assertNotNull(ss);
		//ss.setName("UNITTEST-Stack");

		DeployApplicationStack deploy = new DeployApplicationStack(this.config,
				"src/test/resources/ASFromExternalPlanTest");
		deploy.stackTemplateName = "ASFromExternalPlanTest1.stack.json";
		deploy.componentTemplateName = "ASFromExternalPlanTest1.component.json";
		deploy.id = "L2";
		deploy.plan = new File(
				"src/test/resources/ASFromExternalPlanTest/externalPlan.xml");

		deploy.execute();

	}

	/**
	 * With allocations
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Test
	public void testDestroyStackByExternalPlan()
			throws MojoExecutionException, MojoFailureException {

		PlanHelper helper = new PlanHelper();

		Component cs = helper.loadComponentTemplate(
				"src/test/resources/ASFromExternalPlanTest",
				"ASFromExternalPlanTest1.component.json");
		assertNotNull(cs);
		//cs.setName("UNITTEST");
		Stack ss = helper.loadStackTemplate(
				"src/test/resources/ASFromExternalPlanTest",
				"ASFromExternalPlanTest1.stack.json");
		assertNotNull(ss);
		//ss.setName("UNITTEST-Stack");

		DestroyApplicationStack destroy = new DestroyApplicationStack(
				this.config, "src/test/resources/ASFromExternalPlanTest");
		destroy.stackTemplateName = "ASFromExternalPlanTest1.stack.json";
		destroy.componentTemplateName = "ASFromExternalPlanTest1.component.json";
		destroy.id = "L2";
		destroy.plan = new File(
				"src/test/resources/ASFromExternalPlanTest/externalPlan.xml");
		destroy.execute();

	}
	
}
