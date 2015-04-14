package com.tibco.silverfabric.model;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlanHelperTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testLoadPlan() {
		File f =new File ("src/test/resources/PlanHelper/plan.xml");
		PlanHelper helper = new PlanHelper();
		PlanModel model = helper.loadPlan(f);
		assertNotNull(model);
		assertEquals(model.models.get(0).id, "L1");
		assertEquals(model.models.get(0).stacks.get(0).components.get(0).properties.get("as.stack.hostname"), "urh00609");
	}

}
