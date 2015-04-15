/**
 * 
 */
package com.tibco.silverfabric;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.tibco.silverfabric.components.CreateComponentRestCall;
import com.tibco.silverfabric.model.Archive;
import com.tibco.silverfabric.model.Component;
import com.tibco.silverfabric.model.Model;
import com.tibco.silverfabric.model.PlanModel;
import com.tibco.silverfabric.model.Stack;
import com.tibco.silverfabric.stacks.CreateStackRestCall;

/**
 * @author akaan
 *
 */
@Mojo(name = "deploy")
public class DeployApplicationStack extends AbstractApplicationStack {

	private final static Pattern FILE_PATTERN = Pattern
			.compile("^(\\w+[^\\-]).+\\.([A-Za-z]+)");

	public DeployApplicationStack() {
		super();
	}

	/**
	 * 
	 */
	public DeployApplicationStack(BrokerConfig config) {
		super();
		this.brokerConfig = config;
	}

	/**
	 * 
	 */
	public DeployApplicationStack(BrokerConfig config, String workDirectory) {
		super();
		this.brokerConfig = config;
		this.dependencyWorkDirectory = workDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// STEP 0 - prerequisits check in parent
		super.execute();
		executePlanFile(model, this.planModel.archives);
	}



	/**
	 * 
	 * @param pf
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	private void executePlanFile(Model m, List<Archive> archives)
			throws MojoExecutionException, MojoFailureException {

		info("found " + m.stacks.size() + " listed stacks.");

		int count = 1;
		for (Iterator<Stack> iterator = m.stacks.iterator(); iterator.hasNext();) {
			Stack stack = iterator.next();
			info(">> creating stack '" + stack.getName() + "'.");
			info(">>> components in stack " + stack.getComponents());
			for (Iterator citer = stack.components.iterator(); citer.hasNext();) {
				Component component = (Component) citer.next();
				CreateComponentRestCall c = new CreateComponentRestCall(
						brokerConfig, archives, component);
				c.execute();
			}
			
			CreateStackRestCall cs = new CreateStackRestCall(brokerConfig, stack);
			cs.execute();
			
		}
	}



}
