/**
 * 
 */
package com.tibco.silverfabric.stacks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.tibco.silverfabric.BrokerConfig;
import com.tibco.silverfabric.model.Component;
import com.tibco.silverfabric.model.Plan;
import com.tibco.silverfabric.model.Stack;

/**
 * @author akaan
 *
 */
@Mojo(name = "create-stacks")
public class CreateStacks extends AbstractSilverStacks {

	/**
	 * Local Plan Loading
	 * 
	 * @param config
	 * @param plan
	 */
	public CreateStacks(BrokerConfig config, Plan plan) {
		this(config, plan.stackPlan, CreateStacks.toComponentList(plan.components), new Properties());
	}

	/**
	 * External Plan loading
	 * 
	 * @param brokerConfig
	 * @param components
	 * @param properties
	 */
	public CreateStacks(BrokerConfig brokerConfig, String stackPlan, List<Component> components,
			Properties properties) {
		super();
		setBrokerConfig(brokerConfig);
		this.plan.stackPlan = stackPlan;
		this.stackProperties = properties;

	}

	/**
	 * 
	 */
	public void initialize() throws MojoFailureException {
		super.initialize();
		if (getActions() == null) {
			LinkedList<String> list = new LinkedList<String>();
			list.add("create");
			list.add("publish");
			setActions(list);
		}
		getLog().info("assign action " + getActions());
	}

	/**
	 * 
	 * @param componentNames
	 * @return
	 */
	public static List<Component> toComponentList(List<String> componentNames) {
		List<Component> components = new LinkedList<Component>();
		if (components != null) {
			for (Iterator<String> iterator = componentNames.iterator(); iterator
					.hasNext();) {
				String name = (String) iterator.next();
				components.add(new Component(name));
			}
		}
		return components;
	}
}
