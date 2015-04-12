package com.tibco.silverfabric.components;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import com.tibco.silverfabric.AbstractSilverJSONTest;
import com.tibco.silverfabric.Utils;
import com.tibco.silverfabric.model.Plan;

/**
 * 
 * @author akaan
 *
 */
public class CreateComponentsJSONTest extends AbstractSilverJSONTest {

	@Test
	public void testCreateComponentsFeatures1() throws MojoExecutionException,
			MojoFailureException {
		plan = new Plan();
		plan.componentPlan = Utils.getTestFile(
				CreateComponentsJSONTest.class, 1, "json").getAbsolutePath();
		CreateComponentsJSON c =new CreateComponentsJSON(this.config, plan);
		executeCreateComponent(plan, c);
	}

}
