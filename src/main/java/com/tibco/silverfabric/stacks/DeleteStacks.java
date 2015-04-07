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
@Mojo( name = "delete-stacks")
public class DeleteStacks extends AbstractSilverStacks {

	/**
	 * 
	 */
	public DeleteStacks() {
		super();
	}
	public DeleteStacks(BrokerConfig config, File splan) {
		super();
		this.plan = splan;
		this.setBrokerConfig(config);
	}
	public void initialize() {
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
