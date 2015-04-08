/**
 * 
 */
package com.tibco.silverfabric;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.tibco.silverfabric.components.DeleteComponentsJSON;
import com.tibco.silverfabric.model.Plan;
import com.tibco.silverfabric.stacks.DeleteStacks;

/**
 * @author akaan
 *
 */
@Mojo(name = "destroy")
public class DestroyApplicationStack extends AbstractMojo {

	/* input data */

	/**
	 */
	@Parameter
	public Plan plan;
	@Parameter
	public BrokerConfig brokerConfig;

	/**
	 * 
	 */
	public DestroyApplicationStack() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		if (plan == null) {
			throw new MojoExecutionException("plan must be supplied!");
		}
		if (brokerConfig == null) {
			throw new MojoExecutionException("brokerConfig must be supplied!");
		}
		
		getLog().info(plan.toString());

		try {
			DeleteStacks s = new DeleteStacks(brokerConfig, plan);
			s.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			getLog().error(e);
		}
		// continue with the component deletion;
		
		DeleteComponentsJSON c =new DeleteComponentsJSON(brokerConfig, plan);
		c.execute();
		

	}

}
