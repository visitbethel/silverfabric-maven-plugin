/**
 * 
 */
package com.tibco.silverfabric.stacks;

import java.util.LinkedList;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.tibco.silverfabric.BrokerConfig;
import com.tibco.silverfabric.model.Plan;

/**
 * @author akaan
 *
 */
@Mojo( name = "create-stacks")
public class CreateStacks extends AbstractSilverStacks {

	/**
	 * 
	 */
	public CreateStacks() {
		super();
	}
	
	public CreateStacks(BrokerConfig config, Plan plan) {
		super();
		this.plan = plan;
		setBrokerConfig(config);
	}

	public CreateStacks(BrokerConfig brokerConfig, Plan plan, String name) {
		super();
		this.plan = plan;
		setBrokerConfig(brokerConfig);
		this.stackName = name;
	}

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
}
