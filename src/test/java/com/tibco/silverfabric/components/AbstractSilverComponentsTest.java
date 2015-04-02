package com.tibco.silverfabric.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.tibco.silverfabric.AbstractSilverFabricMojo;
import com.tibco.silverfabric.BrokerConfig;

public class AbstractSilverComponentsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testResponseFormat() throws MojoExecutionException,
			MojoFailureException {
		CreateComponents c = new CreateComponents();
		BrokerConfig config = new BrokerConfig();
		try {
			config.setBrokerURL(new URL("http://urh00172.ute.fedex.com:8080"));
		} catch (MalformedURLException e) {
			Assert.fail("check url");
		}
		config.setUsername("admin");
		config.setPassword("admin");
		assertEquals(8, c.getRestTemplate().getMessageConverters().size());
		c.setBrokerConfig(config);
		c.setComponentName("test");
		c.setComponentType("TYPE");
		c.setEnablerName("enablerName");
		c.setEnablerVersion("enablerVersion");
		c.plan = Utils.getTestFile(AbstractSilverComponentsTest.class, 1, "xml");
		c.initialize();
		MappingJackson2HttpMessageConverter converter = null;
		for (Iterator<HttpMessageConverter<?>> iterator = c.getRestTemplate()
				.getMessageConverters().iterator(); iterator.hasNext();) {
			Object type = (Object) iterator.next();
			if (String.valueOf(type).contains("MappingJackson2")) {
				converter = (MappingJackson2HttpMessageConverter) type;
				break;
			}
		}
		assertNotNull(converter);
		Map<Object, Object> m = new LinkedHashMap<Object, Object>();
		m.put("it", "is");
		try {
			String s = converter.getObjectMapper().writeValueAsString(m);
			assertEquals("{\"it\":\"is\"}", s);
			s = converter.getObjectMapper()
					.writeValueAsString(c.getComponent());
			List<String> l = IOUtils.readLines(new FileReader(Utils
					.getTestFile(AbstractSilverComponentsTest.class, 1, "json")));
			System.out.println(s);
			assertEquals(l.get(0), s);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
