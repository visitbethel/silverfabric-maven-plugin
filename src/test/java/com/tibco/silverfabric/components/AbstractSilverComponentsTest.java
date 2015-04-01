package com.tibco.silverfabric.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

public class AbstractSilverComponentsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testValueOf() {
		Object a = new Object();
		Object b = new Object();
		assertEquals(null, AbstractSilverComponents.valueOf(null, null));
		assertEquals(a, AbstractSilverComponents.valueOf(a, null));
		assertEquals(b, AbstractSilverComponents.valueOf(null, b));
		assertEquals(b, AbstractSilverComponents.valueOf(b, a));
	}

	@Test
	public void testValueOfMap() {
		AbstractSilverComponents s = new AbstractSilverComponents() {
		};
		Object a = new Object();
		Object b = new Object();
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		assertEquals(null, s.valueOf(map, "a1", null, null));
		assertEquals(a, s.valueOf(map, "a2",a, null));
		assertEquals(b, s.valueOf(map, "a3",null, b));
		assertEquals(b, s.valueOf(map, "a4", b, a));
		
		assertEquals(3, map.size());
		assertTrue( map.containsKey("a2") );
		assertTrue( map.containsKey("a3") );
		assertTrue( map.containsKey("a4") );
		assertFalse( map.containsKey("a1") );
	}
	
	@Test
	public void testResponseFormat() {
		AbstractSilverComponents s = new AbstractSilverComponents() {
		};
	}
	
		
}
