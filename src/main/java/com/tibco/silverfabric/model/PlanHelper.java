package com.tibco.silverfabric.model;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PlanHelper {

	public static class PropertiesConverter implements Converter {
		public boolean canConvert(Class clazz) {
			return Properties.class.isAssignableFrom(clazz);
		}

		public void marshal(Object value, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			Properties map = (Properties) value;
			for (Object obj : map.entrySet()) {
				Entry entry = (Entry) obj;
				writer.startNode(entry.getKey().toString());
				writer.setValue(entry.getValue().toString());
				writer.endNode();
			}
		}

		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			Properties map = new Properties();
			while (reader.hasMoreChildren()) {
				reader.moveDown();
				map.put(reader.getNodeName(), reader.getValue());
				reader.moveUp();
			}
			return map;
		}
	}

	public PlanHelper() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param f
	 */
	public PlanModel loadPlan(File f) {
		if (f == null || !f.exists()) {
			return null;
		}

		XStream xs = new XStream();
		xs.alias("planModel", PlanModel.class);
		xs.alias("properties", Properties.class);
		xs.alias("model", Model.class);
		xs.alias("componentDependency", ComponentDependency.class);
		xs.alias("component", Component.class);
		xs.alias("stack", Stack.class);
		xs.registerConverter(new PropertiesConverter());
		xs.addImplicitCollection(PlanModel.class, "models", Model.class);
		xs.addImplicitCollection(Model.class, "componentDependencies",
				ComponentDependency.class);
		xs.addImplicitCollection(Model.class, "components", "component",
				Component.class);
		xs.addImplicitCollection(Component.class, "stacks", "stack",
				Stack.class);
		Object planmodel = xs.fromXML(f);

		return (PlanModel) planmodel;

	}

}
