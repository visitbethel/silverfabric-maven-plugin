package com.tibco.silverfabric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

public class AbstractSilverFabricMojoTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testValueOf() {
		Object a = new Object();
		Object b = new Object();
		assertEquals(null, AbstractSilverFabricMojo.valueOf(null, null));
		assertEquals(a, AbstractSilverFabricMojo.valueOf(a, null));
		assertEquals(b, AbstractSilverFabricMojo.valueOf(null, b));
		assertEquals(b, AbstractSilverFabricMojo.valueOf(b, a));
	}

	@Test
	public void testValueOfMap() {
		;
		Object a = new Object();
		Object b = new Object();
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		assertEquals(null,
				AbstractSilverFabricMojo.valueOf(map, "a1", null, null));
		assertEquals(a, AbstractSilverFabricMojo.valueOf(map, "a2", a, null));
		assertEquals(b, AbstractSilverFabricMojo.valueOf(map, "a3", null, b));
		assertEquals(b, AbstractSilverFabricMojo.valueOf(map, "a4", b, a));

		assertEquals(3, map.size());
		assertTrue(map.containsKey("a2"));
		assertTrue(map.containsKey("a3"));
		assertTrue(map.containsKey("a4"));
		assertFalse(map.containsKey("a1"));
	}
}
