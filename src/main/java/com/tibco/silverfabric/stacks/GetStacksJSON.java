/**
 * 
 */
package com.tibco.silverfabric.stacks;

import java.util.LinkedList;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.tibco.silverfabric.BrokerConfig;

/**
 * @author akaan
 *
 */
@Mojo( name = "info-stacks")
public class GetStacksJSON extends AbstractSilverStacks {

	/**
	 * 
	 */
	public GetStacksJSON() {
		super();
	}
	
	public GetStacksJSON(BrokerConfig config) {
		super();
		setBrokerConfig(config);
	}

	public void initialize() throws MojoFailureException {
		super.initialize();
		if (getActions() == null) {
			LinkedList<String> list = new LinkedList<String>();
			list.add("get info");
			setActions(list);
		}
		getLog().info("assign action " + getActions());
	}
}
