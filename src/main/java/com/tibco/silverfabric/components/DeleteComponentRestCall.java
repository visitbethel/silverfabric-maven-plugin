/**
 * 
 */
package com.tibco.silverfabric.components;

import java.util.LinkedList;

import org.apache.maven.plugin.MojoFailureException;

import com.tibco.silverfabric.BrokerConfig;
import com.tibco.silverfabric.model.Component;

/**
 * @author akaan
 *
 */
public class DeleteComponentRestCall extends AbstractSilverComponentsRestCall {

	/**
	 * 
	 */
	public DeleteComponentRestCall(BrokerConfig brokerConfig,
			Component component) {
		super();
		setBrokerConfig(brokerConfig);
		this.component = component;
	}

	/**
	 * 
	 */
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
