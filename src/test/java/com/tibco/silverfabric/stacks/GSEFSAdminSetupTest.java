package com.tibco.silverfabric.stacks;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import com.tibco.silverfabric.AbstractSilverJSONTest;
import com.tibco.silverfabric.Utils;
import com.tibco.silverfabric.components.CreateComponentsJSON;
import com.tibco.silverfabric.model.Plan;
import com.tibco.silverfabric.stacks.CreateStacks;

public class GSEFSAdminSetupTest extends AbstractSilverJSONTest {

	public GSEFSAdminSetupTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * With allocations
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Test
	public void testCreateGSEFSAdminTest1()
			throws MojoExecutionException, MojoFailureException {
		CreateComponentsJSON c = new CreateComponentsJSON();
		CreateStacks s = new CreateStacks();

		plan = new Plan();
		plan.componentTemplateURI = Utils.getTestFile(GSEFSAdminSetupTest.class, 1,
				"component.json").getAbsolutePath();
		plan.stackTemplateURI = Utils.getTestFile(GSEFSAdminSetupTest.class, 1,
				"stack.json").getAbsolutePath();

		executeCreateComponent(plan, c);
		
		executeCreateStack(plan, c);
	}



}
