package com.tibco.silverfabric.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fedex.scm.AllocationRule;
import com.fedex.scm.ComponentAllocationInfo;
import com.fedex.scm.Components;
import com.fedex.scm.DefaultSetting;
import com.fedex.scm.Policy;
import com.fedex.scm.RuntimeContextVariable;
import com.fedex.scm.Stacks;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.emory.mathcs.backport.java.util.LinkedList;

public class PlanHelper {

	private static Logger LOGGER = LoggerFactory.getLogger(PlanHelper.class);

	/**
	 * 
	 * @author akaan
	 *
	 */
	public static class PropertiesConverter implements Converter {
		public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz) {
			return Properties.class.isAssignableFrom(clazz);
		}

		public void marshal(Object value, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			Properties map = (Properties) value;
			for (Object obj : map.entrySet()) {
				@SuppressWarnings("rawtypes")
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

	/**
	 * 
	 * @author akaan
	 *
	 */
	public static class PropertiesScmConverter implements Converter {
		public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz) {
			return com.fedex.scm.Properties.class.isAssignableFrom(clazz);
		}

		public void marshal(Object value, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			com.fedex.scm.Properties map = (com.fedex.scm.Properties) value;
			writer.startNode(map.getName());
			writer.setValue(map.getValue());
			writer.endNode();
		}

		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			com.fedex.scm.Properties map = new com.fedex.scm.Properties();
			while (reader.hasMoreChildren()) {
				reader.moveDown();
				map.setName(reader.getNodeName());
				map.setValue(reader.getValue());
				reader.moveUp();
			}
			return map;
		}
	}

	/**
	 * 
	 * @author akaan
	 *
	 */
	public static class DefaultSettingConverter implements Converter {
		public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz) {
			return com.fedex.scm.DefaultSetting.class.isAssignableFrom(clazz);
		}

		public void marshal(Object value, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			DefaultSetting map = (DefaultSetting) value;
			writer.startNode(map.getName());
			writer.setValue(map.getValue());
			writer.endNode();
		}

		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			DefaultSetting map = new DefaultSetting();
			map.setName(reader.getAttribute("name"));
			map.setValue(reader.getAttribute("value"));
			return map;
		}
	}

	/**
	 * 
	 * @author akaan
	 *
	 */
	public static class RuntimeContextVariableConverter implements Converter {
		public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz) {
			return com.fedex.scm.RuntimeContextVariable.class
					.isAssignableFrom(clazz);
		}

		public void marshal(Object value, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			RuntimeContextVariable map = (RuntimeContextVariable) value;
			writer.startNode(map.getName());
			writer.setValue(map.getValue());
			writer.endNode();
		}

		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			RuntimeContextVariable map = new RuntimeContextVariable();
			map.setName(reader.getAttribute("name"));
			map.setValue(reader.getAttribute("value"));
			// assign defaults
			map.setType(1);
			map.setAutoIncrementType(0);
			map.setExport(Boolean.FALSE);
			while (reader.hasMoreChildren()) {
				reader.moveDown();
				if ("export".equals(reader.getNodeName())) {
					map.setExport(Boolean.valueOf(reader.getValue()));
				} else if ("autoincrement".equals(reader.getNodeName())) {
					map.setAutoIncrementType(Integer.valueOf(reader.getValue()));
				} else if ("type".equals(reader.getNodeName())) {
					map.setType(Integer.valueOf(reader.getValue()));
				} else if ("description".equals(reader.getNodeName())) {
					map.setDescription(reader.getValue());
				}
				reader.moveUp();
			}
			return map;
		}
	}

	public PlanHelper() {

	}

	/**
	 * 
	 * @param f
	 */
	public PlanModel loadPlan(File f) {
		if (f == null || !f.exists()) {
			return null;
		}
		LOGGER.info("loading external plan from " + f.getAbsolutePath() + ".");
		XStream xs = new XStream();
		xs.alias("planModel", PlanModel.class);
		xs.alias("dependency", Dependency.class);
		xs.alias("properties", Properties.class);
		xs.alias("props", com.fedex.scm.Properties.class);
		xs.alias("model", Model.class);
		xs.alias("componentDependency", ComponentDependency.class);
		xs.alias("component", Component.class);
		xs.alias("stack", Stack.class);
		xs.alias("defaultSetting", DefaultSetting.class);
		// xs.alias("allocationRule", AllocationRule.class);
		// xs.alias("componentAllocationInfo", ComponentAllocationInfo.class);
		xs.registerConverter(new PropertiesConverter());
		xs.registerConverter(new DefaultSettingConverter());
		xs.registerConverter(new RuntimeContextVariableConverter());
		xs.addImplicitCollection(PlanModel.class, "dependencies", "dependency",
				Dependency.class);
		xs.addImplicitCollection(PlanModel.class, "models", Model.class);
		xs.addImplicitCollection(Stack.class, "componentDependencies",
				ComponentDependency.class);
		xs.addImplicitCollection(Stack.class, "components", "component",
				Component.class);
		xs.addImplicitCollection(Stack.class, "policies", "policy",
				Policy.class);
		xs.addImplicitCollection(Policy.class, "componentAllocationInfo",
				"componentAllocationInfo", ComponentAllocationInfo.class);
		xs.addImplicitCollection(ComponentAllocationInfo.class,
				"allocationRules", "allocationRule", AllocationRule.class);

		xs.addImplicitCollection(Model.class, "stacks", "stack", Stack.class);
		xs.addImplicitCollection(Component.class, "defaultSettings",
				"defaultSetting", DefaultSetting.class);
		xs.addImplicitCollection(PlanModel.class, "archives", "archive",
				Archive.class);
		xs.addImplicitCollection(Component.class, "runtimeContextVariables",
				"runtimeContextVariable", RuntimeContextVariable.class);
		PlanModel planmodel = (PlanModel) xs.fromXML(f);

		// function for pre-populating mandatory fields like
		// - name
		// - description
		planmodel = normalizePlan(planmodel);

		return planmodel;

	}

	/**
	 * 
	 * @param planmodel
	 * @return
	 */
	public PlanModel normalizePlan(PlanModel planmodel) {
		if (planmodel.name == null) {
			throw new RuntimeException("PlanModel requires name.");
		}
		for (Iterator<Model> iterator = planmodel.models.iterator(); iterator
				.hasNext();) {
			Model model = iterator.next();
			int count = 1;
			for (Iterator miter = model.stacks.iterator(); miter.hasNext();) {
				Stack stack = (Stack) miter.next();
				if (stack.getName() == null) {
					stack.setName(String.format("%s_%s_Stack#%02d",
							planmodel.name, model.id, count));
				}
				if (stack.getDescription() == null) {
					stack.setDescription(String.format("%s Stack#%02d for %s",
							planmodel.name, count, model.id));
				}
				int ccount = 1;
				for (Iterator citer = stack.components.iterator(); citer
						.hasNext();) {
					Component component = (Component) citer.next();
					if (component.getName() == null) {
						component.setName(String.format("%s_%s_%d",
								planmodel.name, model.id, ccount));
					}
					if (component.getDescription() == null) {
						component.setDescription(String.format("%s_%s_%d",
								planmodel.name, model.id, ccount));
					}
					ccount++;
				}
				count++;
			}

		}
		return planmodel;
	}

	/**
	 * 
	 * @param plan
	 * @param modelId
	 * @return
	 */
	public Model getModel(PlanModel plan, String modelId) {
		for (Iterator<Model> miter = plan.models.iterator(); miter.hasNext();) {
			Model type = (Model) miter.next();
			if (type.id != null && type.id.equals(modelId)) {
				return type;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param cFromJSON
	 * @param plan
	 */
	public void overlay(Components cFromJSON, Model plan) {
		for (Iterator siter = plan.stacks.iterator(); siter.hasNext();) {
			Stack stack = (Stack) siter.next();
			for (Iterator citer = stack.components.iterator(); citer.hasNext();) {
				Component cFromXml = (Component) citer.next();
				merge(cFromJSON, cFromXml);
			}
		}
	}

	/**
	 * 
	 * @param cFromJSON
	 * @param plan
	 */
	public void overlay(Stacks cFromJSON, Model plan) {
		for (Iterator siter = plan.stacks.iterator(); siter.hasNext();) {
			Stack stack = (Stack) siter.next();
			if (stack.getComponents() == null) {
				stack.setComponents(new LinkedList());
			}
			merge(cFromJSON, stack);
			for (Iterator citer = stack.components.iterator(); citer.hasNext();) {
				Component cFromXml = (Component) citer.next();
				stack.getComponents().add(cFromXml.getName());
			}
		}
	}

	public void merge(Object from, Object target) {
		merge(from, target, false);
	}

	/**
	 * 
	 * @param from
	 * @param target
	 */
	public void merge(Object from, Object target, boolean overwrite) {
		// System.err.println(obj + " -> " + update);

		// if (!obj.getClass().isAssignableFrom(update.getClass())) {
		// return;
		// }

		Method[] methods = from.getClass().getMethods();

		for (Method fromMethod : methods) {
			if (fromMethod.getName().startsWith("get")) {

				String fromName = fromMethod.getName();
				String toName = fromName.replace("get", "set");

				Method targetMethod;
				try {
					targetMethod = target.getClass().getMethod(fromName);
				} catch (Exception e1) {
					break;
				}
				Object targetValue;
				try {
					targetValue = targetMethod.invoke(target);
				} catch (Exception e1) {
					break;
				}
				// if the to / target is null then do the merge otherwise
				// leave it alone.
				if (targetValue == null) {
					try {
						Method toMetod = from.getClass().getMethod(toName,
								fromMethod.getReturnType());
						Object value = fromMethod.invoke(from, (Object[]) null);
						if (value != null || !String.valueOf(value).equals("")) {
							toMetod.invoke(target, value);
						}
					} catch (Exception e) {
						LOGGER.warn("merge exception: " + e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param dependencyWorkDirectory
	 * @param fileName
	 * @return
	 */
	public Components loadComponentTemplate(
			String dependencyWorkDirectory, String fileName) {
		ObjectMapper m = new ObjectMapper();
		Components c;
		try {
			c = m.readValue(new File(dependencyWorkDirectory, fileName),
					Components.class);
		} catch (Exception e) {
			LOGGER.error("No component plan loaded: " + e.getMessage());
			return null;
		}
		return c;
	}

	/**
	 * 
	 * @param dependencyWorkDirectory
	 * @param fileName
	 * @return
	 */
	public Stacks loadStackTemplate(
			String dependencyWorkDirectory, String fileName) {
		ObjectMapper m = new ObjectMapper();
		Stacks s;
		try {
			s = m.readValue(new File(dependencyWorkDirectory, fileName),
					Stacks.class);
		} catch (Exception e) {
			LOGGER.error("No stack plan loaded: " + e.getMessage());
			return null;
		}
		return s;
	}

}
