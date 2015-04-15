/**
 * 
 */
package com.tibco.silverfabric.components;

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.tibco.silverfabric.BrokerConfig;
import com.tibco.silverfabric.model.Archive;
import com.tibco.silverfabric.model.Component;

/**
 * @author akaan
 *
 */
public class CreateComponentRestCall extends AbstractSilverJSONComponents {

	/**
	 * @param component
	 * 
	 */
	public CreateComponentRestCall(BrokerConfig brokerConfig, List<Archive> archives, Component component) {
		super();
		setBrokerConfig(brokerConfig);
		getArchives().addAll(archives);
		
	}

	/**
	 * 
	 */
	public void initialize() throws MojoFailureException {
		super.initialize();
		getLog().info(
				"initializing component [" + this.componentName + "]: "
						+ this.toString());
		if (getActions() == null) {
			LinkedList<String> list = new LinkedList<String>();
			list.add("create");
			if (this.getConfigFile() != null) {
				list.add("update config file");
			}
			if (this.getContentFiles() != null
					&& this.getContentFiles().size() > 0) {
				list.add("add content file");
			}
			if (this.getArchives() != null && this.getArchives().size() > 0) {
				list.add("add archives");
			}
			if (this.getScriptFile() != null) {
				list.add("add script-files");
			}
			list.add("publish");
			setActions(list);
		}
		getLog().info("assign action " + getActions());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tibco.silverfabric.AbstractSilverFabricMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub
		super.execute();
	}

}
