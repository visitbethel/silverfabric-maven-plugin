package com.tibco.silverfabric.stacks;

import static org.junit.Assert.fail;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tibco.silverfabric.AbstractSilverJSONTest;
import com.tibco.silverfabric.Utils;
import com.tibco.silverfabric.model.Plan;

public class GSEFSAdminTearDownTest extends AbstractSilverJSONTest {

	public GSEFSAdminTearDownTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.tibco.silverfabric.AbstractSilverJSONTest#setup()
	 */
	@Before
	public void setup() {
		setBrokerConfig();
	}
	
	@After
	public void cleanup() {}

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
		plan.componentTemplateURI = Utils.getTestFile(GSEFSAdminSetupTest.class, 1,
				"component.json").getAbsolutePath();
		plan.stackTemplateURI = Utils.getTestFile(GSEFSAdminSetupTest.class, 1,
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





}
