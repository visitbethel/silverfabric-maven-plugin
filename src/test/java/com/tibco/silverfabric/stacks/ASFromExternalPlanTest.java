package com.tibco.silverfabric.stacks;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Bean;

import com.tibco.silverfabric.AbstractSilverJSONTest;
import com.tibco.silverfabric.Archive;
import com.tibco.silverfabric.DeployApplicationStack;
import com.tibco.silverfabric.DestroyApplicationStack;
import com.tibco.silverfabric.Utils;
import com.tibco.silverfabric.components.CreateComponentsJSON;
import com.tibco.silverfabric.model.Component;
import com.tibco.silverfabric.model.Plan;
import com.tibco.silverfabric.model.Stack;

public class ASFromExternalPlanTest extends AbstractSilverJSONTest {


	@Before
	public void setup(){
		setBrokerConfig();
		
	}
	
	/**
	 * With allocations
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Test
	public void testCreateComponentsForExternalPlan() throws MojoExecutionException,
			MojoFailureException {
		plan = new Plan();
		plan.componentPlan = Utils.getTestFile(ASFromExternalPlanTest.class, 1, "component.json").getAbsolutePath();
		plan.stackPlan = Utils.getTestFile(ASFromExternalPlanTest.class, 1, "stack.json").getAbsolutePath();
		
		DeployApplicationStack deploy =new DeployApplicationStack(this.config, plan);
		deploy.planFile = "src/test/resources/ASFromExternalPlanTest/externalPlan.xml";
		
		deploy.execute();
			
		
	}
	

	/**
	 * With allocations
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Test
	public void testDeleteComponentsForExternalPlan() throws MojoExecutionException,
			MojoFailureException {
		plan = new Plan();
		plan.componentPlan = Utils.getTestFile(ASFromExternalPlanTest.class, 1, "component.json").getAbsolutePath();
		plan.stackPlan = Utils.getTestFile(ASFromExternalPlanTest.class, 1, "stack.json").getAbsolutePath();
		
		DestroyApplicationStack deploy =new DestroyApplicationStack(this.config, plan);
		deploy.planFile = "src/test/resources/ASFromExternalPlanTest/externalPlan.xml";
		
		deploy.execute();
			
		
	}

	

}
