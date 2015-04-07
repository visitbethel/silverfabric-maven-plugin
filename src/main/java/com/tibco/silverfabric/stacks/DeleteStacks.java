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
@Mojo( name = "delete-stacks")
public class DeleteStacks extends AbstractSilverStacks {

	/**
	 * 
	 */
	public DeleteStacks() {
		super();
	}
	public DeleteStacks(BrokerConfig config, Plan stackplan) {
		super();
		this.setBrokerConfig(config);
		this.plan = stackplan;
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
