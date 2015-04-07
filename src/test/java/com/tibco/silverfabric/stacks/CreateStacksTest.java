package com.tibco.silverfabric.stacks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import com.tibco.silverfabric.AbstractSilverJSONTest;
import com.tibco.silverfabric.Utils;
import com.tibco.silverfabric.components.CreateComponentsJSON;
import com.tibco.silverfabric.components.CreateComponentsJSONTest;
import com.tibco.silverfabric.model.Plan;

public class CreateStacksTest extends AbstractSilverJSONTest {

	private static final String PREFIX = "__UNIT-STACK";

	private String componentName = null;

	Plan stackplan;


	@Test
	public void testCreateStacksTest1() throws MojoExecutionException,
			MojoFailureException {

		stackplan = new Plan();
		stackplan.componentTemplateURI = Utils.getTestFile(
				CreateComponentsJSONTest.class, 1, "json").getAbsolutePath();
		stackplan.stackTemplateURI = Utils.getTestFile(CreateStacksTest.class,
				1, "json").getAbsolutePath();

		CreateComponentsJSON c = new CreateComponentsJSON();
		executeCreateComponent(stackplan, c);

		CreateStacks s = new CreateStacks(config, stackplan);
		s.initialize();
		s.setStackName(PREFIX + "-" + this.getClass());
		s.getStack().setComponents(Arrays.asList(new String[] { c.getComponentName() }));

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
		assertNotNull(s.getStack());
		assertNotNull(s.getStack().getPolicies());
		assertEquals(1, s.getStack().getPolicies().size());
	}
}
