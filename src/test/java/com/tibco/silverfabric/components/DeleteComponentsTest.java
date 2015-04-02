package com.tibco.silverfabric.components;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DeleteComponentsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testDeleteComponents() {
		CreateComponents c = new CreateComponents();
		assertEquals(3, c.getActions().size());
	}


}
