package com.tibco.silverfabric.components;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import com.tibco.silverfabric.AbstractSilverJSONTest;
import com.tibco.silverfabric.Utils;
import com.tibco.silverfabric.model.Plan;

public class GetComponentsJSONTest extends AbstractSilverJSONTest {


	@Test
	public void testGetComponentsFeatures1() throws MojoExecutionException,
			MojoFailureException {
		plan = new Plan();
		plan.componentTemplateURI = Utils.getTestFile(CreateComponentsJSONTest.class, 1, "json").getAbsolutePath();
		CreateComponentsJSON c = new CreateComponentsJSON(config,plan);
		executeCreateComponent(plan, c);
		
		
		executeGetComponent(plan);
	}

	private void executeGetComponent(Plan planz) {
		GetComponentsJSON c = new GetComponentsJSON(config);
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
