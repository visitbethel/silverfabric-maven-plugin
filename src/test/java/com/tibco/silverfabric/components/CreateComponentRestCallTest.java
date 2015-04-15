package com.tibco.silverfabric.components;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;
import static org.junit.Assert.*;

import com.tibco.silverfabric.AbstractSilverJSONTest;
import com.tibco.silverfabric.model.Component;
import com.tibco.silverfabric.model.PlanHelper;

/**
 * 
 * @author akaan
 *
 */
public class CreateComponentRestCallTest extends AbstractSilverJSONTest {

	@Test
	public void testCreateComponentsFeatures1() throws MojoExecutionException,
			MojoFailureException {
		
		setBrokerConfig();
		PlanHelper helper = new PlanHelper();
		
		Component cs = helper.loadComponentTemplate("src/test/resources/CreateComponentRestCallTest", "CreateComponentsJSONTest1.json");
		assertNotNull(cs);
		cs.setName("UNITTEST");
		CreateComponentRestCall c = new CreateComponentRestCall(this.config, null, cs);
		
		
		c.execute();
	}

	
	@Test
	public void testDeleteComponentsFeatures1() throws MojoExecutionException,
			MojoFailureException {
		
		setBrokerConfig();
		PlanHelper helper = new PlanHelper();
		
		Component cs = helper.loadComponentTemplate("src/test/resources/CreateComponentRestCallTest", "CreateComponentsJSONTest1.json");
		assertNotNull(cs);
		cs.setName("UNITTEST");
		DeleteComponentRestCall c = new DeleteComponentRestCall(this.config, cs);
		
		
		c.execute();
	}	
}
