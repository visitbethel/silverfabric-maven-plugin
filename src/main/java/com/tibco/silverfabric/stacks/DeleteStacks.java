/**
 * 
 */
package com.tibco.silverfabric.stacks;

import java.util.LinkedList;
import java.util.Properties;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.tibco.silverfabric.BrokerConfig;
import com.tibco.silverfabric.model.Plan;

/**
 * @author akaan
 *
 */
@Mojo( name = "delete-stacks")
public class DeleteStacks extends AbstractSilverStacks {

	/**
	 * 
	 */
	public DeleteStacks() {
		super();
	}
	public DeleteStacks(BrokerConfig config, Plan stackplan, String name) {
		this(config,stackplan,name, new Properties());
	}
	
	public DeleteStacks(BrokerConfig brokerConfig, Plan plan, String name,
			Properties properties) {
		super();
		setBrokerConfig(brokerConfig);
		this.plan = plan;
		this.stackName = name;
		this.stackProperties = properties;
	}
	public void initialize() throws MojoFailureException {
		super.initialize();
		if (getActions() == null) {
			LinkedList<String> list = new LinkedList<String>();
			list.add("unpublish");
			list.add("delete");
			setActions(list);
		}
		getLog().info("assign action " + getActions());
	}
}
