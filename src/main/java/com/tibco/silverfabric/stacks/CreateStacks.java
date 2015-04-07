/**
 * 
 */
package com.tibco.silverfabric.stacks;

import java.io.File;
import java.util.LinkedList;

import org.apache.maven.plugins.annotations.Mojo;

import com.tibco.silverfabric.BrokerConfig;

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
	
	public CreateStacks(BrokerConfig config, File plan) {
		super();
		this.plan = plan;
		setBrokerConfig(config);
	}

	public void initialize() {
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
