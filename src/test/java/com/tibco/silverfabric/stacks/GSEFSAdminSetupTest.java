package com.tibco.silverfabric.stacks;

import static org.junit.Assert.fail;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tibco.silverfabric.AbstractSilverJSONTest;
import com.tibco.silverfabric.Utils;
import com.tibco.silverfabric.components.CreateComponentsJSON;
import com.tibco.silverfabric.model.Plan;
import com.tibco.silverfabric.stacks.CreateStackRestCall;

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
		plan = new Plan();
		plan.componentPlan = Utils.getTestFile(GSEFSAdminSetupTest.class, 1,
				"component.json").getAbsolutePath();
		plan.stackPlan = Utils.getTestFile(GSEFSAdminSetupTest.class, 1,
				"stack.json").getAbsolutePath();

		CreateComponentsJSON c = new CreateComponentsJSON(getConfig(), plan);
		CreateStackRestCall s = new CreateStackRestCall(this.config, plan);


		executeCreateComponent(plan, c);
		
		executeCreateStack(plan, c);
	}
	
	/**
	 * With allocations
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Test
	public void testDeleteGSEFSAdminStackAndComponents()
			throws MojoExecutionException, MojoFailureException {
		plan = new Plan();
		plan.componentPlan = Utils.getTestFile(GSEFSAdminSetupTest.class, 1,
				"component.json").getAbsolutePath();
		plan.stackPlan = Utils.getTestFile(GSEFSAdminSetupTest.class, 1,
				"stack.json").getAbsolutePath();
	
		try {
			executeDeleteStack(plan, GSEFSAdminSetupTest.class);
	
			executeDeleteComponent(plan, GSEFSAdminSetupTest.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

	@After
	public void cleanup() {}

	/* (non-Javadoc)
	 * @see com.tibco.silverfabric.AbstractSilverJSONTest#setup()
	 */
	@Before
	public void setup() {
		setBrokerConfig();
	}



}
