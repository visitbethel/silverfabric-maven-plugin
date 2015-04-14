package com.tibco.silverfabric.stacks;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tibco.silverfabric.AbstractSilverJSONTest;
import com.tibco.silverfabric.Archive;
import com.tibco.silverfabric.BrokerConfig;
import com.tibco.silverfabric.Utils;
import com.tibco.silverfabric.components.CreateComponentsJSON;
import com.tibco.silverfabric.model.Plan;

public class CanPostTransformTest extends AbstractSilverJSONTest {


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
		plan.componentPlan = Utils.getTestFile(CanPostTransformTest.class, 1, "component.json").getAbsolutePath();
		plan.stackPlan = Utils.getTestFile(CanPostTransformTest.class, 1, "stack.json").getAbsolutePath();
		
		File f = new File("/Users/akaan/STAGING/shipmentefs-CanPostTransformer/DEV-SF/NA/", "CanPostTransformer.ear.zip");
		c.getArchives().add(new Archive(f));
		f = new File("src/main/resources/content/scripts/bw.py");
		c.getContentFiles().add(new Archive(f,"scripts"));
		
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
	public void testDeleteComponentsForCanPostTransform1() throws MojoExecutionException,
			MojoFailureException {
		plan = new Plan();
		plan.componentPlan = Utils.getTestFile(CanPostTransformTest.class, 1, "component.json").getAbsolutePath();
		plan.stackPlan = Utils.getTestFile(CanPostTransformTest.class, 1, "stack.json").getAbsolutePath();

		executeDeleteStack(plan);
		executeDeleteComponent(plan);
	}

	

}