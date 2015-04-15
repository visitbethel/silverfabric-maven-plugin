package com.tibco.silverfabric.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.fedex.scm.AllocationRule;
import com.fedex.scm.ComponentAllocationInfo;
import com.fedex.scm.Components;
import com.fedex.scm.Policy;
import com.fedex.scm.Properties;
import com.fedex.scm.Stacks;

public class PlanHelperTest {

	PlanModel plan;
	PlanHelper helper;

	@Before
	public void setUp() throws Exception {
		File f = new File("src/test/resources/PlanHelper/plan.xml");
		helper = new PlanHelper();
		plan = helper.loadPlan(f);
	}

	@Test
	public void testLoadPlan() {
		assertNotNull(plan);
		assertEquals(plan.models.get(0).id, "L1");
		Stack s = plan.models.get(0).stacks.get(0);
		Component c = plan.models.get(0).stacks.get(0).components.get(0);
		assertEquals(c.properties.get("assigned.hostname"), "urh00609");
		assertEquals(c.getDefaultSettings().size(), 3);
		assertEquals(c.getDefaultSettings().get(0).getValue(), "1");
		assertEquals(c.getDefaultSettings().get(0).getName(),
				"Default Max Engines");
		assertEquals(c.getDefaultSettings().get(1).getValue(), "1");
		//
		assertEquals(c.getRuntimeContextVariables().get(0).getName(),
				"AMI_HAWK_NETWORK");
		assertEquals(c.getRuntimeContextVariables().get(0).getDescription(),
				"Describe");

		// normalization test
		assertEquals("GSEFS_DDS_L1_1", c.getName());
		assertEquals("GSEFS_DDS_L1_Stack#01", s.getName());
	}

	@Test
	public void testLoadPlanToComponentJSON() throws Exception {
		Components c = helper.loadComponentTemplate("src/test/resources/PlanHelper",
				"component.json");
		assertNotNull(c);
		assertEquals(c.getName(), "${sf.component.name}");

		Model l1 = helper.getModel(plan, "L1");
		assertNotNull(l1);
		assertEquals(2, l1.stacks.size());
		assertEquals(1, l1.stacks.get(0).components.size());
		Component fromXML = l1.stacks.get(0).components.get(0);
		assertEquals("GSEFS_DDS_L1_1", fromXML.getName());
		assertEquals(null, fromXML.getEnablerName());
		assertEquals("TIBCO BusinessEvents container", c.getEnablerName());
		assertEquals("TIBCO BusinessEvents:2.5.0", c.getComponentType());
		assertEquals(null, fromXML.getFeatures());
		helper.overlay(c, l1);
		assertEquals(2, l1.stacks.size());
		assertEquals("GSEFS_DDS_L1_1", fromXML.getName());
		assertEquals("TIBCO BusinessEvents container", c.getEnablerName());
		assertEquals("TIBCO BusinessEvents:2.5.0", c.getComponentType());
		assertEquals("TIBCO BusinessEvents container", fromXML.getEnablerName());
		assertEquals("TIBCO BusinessEvents:2.5.0", fromXML.getComponentType());
		assertEquals(3, fromXML.getFeatures().size());

	}

	@Test
	public void testLoadPlan1Stack1ComponentJSON() throws Exception {
		Stacks s = helper.loadStackTemplate("src/test/resources/PlanHelper",
				"stack.json");
		assertNotNull(s);

		assertEquals(s.getName(), "${sf.component.name}_Stack");

		Model l1 = helper.getModel(plan, "L2");
		assertNotNull(l1);
		assertEquals(1, l1.stacks.size());
		Stack stackXML = l1.stacks.get(0);
		assertEquals("GSEFS_StopGateway_BW_L1_Stack", stackXML.getName());
		assertEquals(1, s.getPolicies().size());
		assertEquals(1, stackXML.getPolicies().size());
		Policy p = s.getPolicies().get(0);
		assertEquals(1, p.getComponentAllocationInfo().size());
		
		helper.overlay(s, l1);
		
		
		assertEquals(s.getName(), "${sf.component.name}_Stack");
		assertEquals("GSEFS_StopGateway_BW_L1_Stack", stackXML.getName());
		assertEquals("9To5", stackXML.getPolicies().get(0).getScheduleName());
		
		// components in stack
		assertEquals("GSEFS_StopGateway_BW_L1", stackXML.getComponents().get(0));

		Policy pp = stackXML.getPolicies().get(0);
		assertEquals(1, pp.getComponentAllocationInfo().size());
		ComponentAllocationInfo info = pp.getComponentAllocationInfo().get(0);
		assertEquals("GSEFS_StopGateway_BW_L1", info.getName());
		assertEquals(1, info.getAllocationRules().size());
		AllocationRule rule = info.getAllocationRules().get(0);
		assertEquals(4, rule.getProperties().size());
		Properties prp = (Properties) rule.getProperties().get(2);
		assertEquals("urh00601", prp.getValue());
		
		
		

	}
	
	
	@Test
	public void testLoadPlanToStackJSON() throws Exception {
		Stacks s = helper.loadStackTemplate("src/test/resources/PlanHelper",
				"stack.json");
		assertNotNull(s);

		assertEquals(s.getName(), "${sf.component.name}_Stack");

		Model l1 = helper.getModel(plan, "L1");
		assertNotNull(l1);
		assertEquals(2, l1.stacks.size());
		Stack fromXML = l1.stacks.get(0);
		assertEquals("GSEFS_DDS_L1_Stack#01", fromXML.getName());
		assertEquals(1, s.getPolicies().size());
		assertEquals(null, fromXML.getPolicies());
		Policy p = s.getPolicies().get(0);
		assertEquals(1, p.getComponentAllocationInfo().size());
		
		helper.overlay(s, l1);
		
		
		assertEquals(s.getName(), "${sf.component.name}_Stack");
		assertEquals("GSEFS_DDS_L1_Stack#01", fromXML.getName());
		assertEquals("", fromXML.getPolicies().get(0).getScheduleName());
		
		
		assertEquals("GSEFS_DDS_L1_1", fromXML.getComponents().get(0));

	}
}
