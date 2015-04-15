/**
 * 
 */
package com.tibco.silverfabric;

import java.util.Iterator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.tibco.silverfabric.components.DeleteComponentRestCall;
import com.tibco.silverfabric.model.Component;
import com.tibco.silverfabric.model.Model;
import com.tibco.silverfabric.model.PlanModel;
import com.tibco.silverfabric.model.Stack;
import com.tibco.silverfabric.stacks.DeleteStackRestCall;

/**
 * @author akaan
 *
 */
@Mojo(name = "destroy")
public class DestroyApplicationStack extends AbstractApplicationStack {

	/**
	 * 
	 */
	public DestroyApplicationStack(BrokerConfig config,
			String dependencyWorkDirectory) {
		super();
		this.brokerConfig = config;
		this.dependencyWorkDirectory = dependencyWorkDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		executePlanFile(model);

	}

	/**
	 * 
	 * @param pf
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	private void executePlanFile(Model m) throws MojoExecutionException,
			MojoFailureException {
		info("found " + m.stacks.size() + " listed stacks.");

		int count = 1;
		for (Iterator<Stack> iterator = m.stacks.iterator(); iterator.hasNext();) {
			Stack stack = iterator.next();
			info(">> delete stack '" + stack.getName() + "' components.");
			info(">>> components in stack " + stack.getComponents());
			for (Iterator citer = stack.components.iterator(); citer.hasNext();) {
				Component component = (Component) citer.next();
				DeleteComponentRestCall d = new DeleteComponentRestCall(
						brokerConfig, component);
				d.execute();
			}
			DeleteStackRestCall call = new DeleteStackRestCall(brokerConfig, stack);
			call.execute();
			count++;
		}
	}

}
