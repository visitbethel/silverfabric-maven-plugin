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
public class CreateStackRestCall extends AbstractSilverStacks {

	/**
	 * Local Plan Loading
	 * 
	 * @param config
	 * @param plan
	 */
	public CreateStackRestCall(BrokerConfig config, Stack stack) {
		super();
		setBrokerConfig(config);
		this.stack = stack;
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
