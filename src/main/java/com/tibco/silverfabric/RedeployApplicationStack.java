/**
 * 
 */
package com.tibco.silverfabric;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.tibco.silverfabric.components.CreateComponentsJSON;
import com.tibco.silverfabric.components.DeleteComponentsJSON;
import com.tibco.silverfabric.model.Plan;
import com.tibco.silverfabric.stacks.CreateStacks;
import com.tibco.silverfabric.stacks.DeleteStacks;

/**
 * @author akaan
 *
 */
@Mojo(name = "redeploy")
public class RedeployApplicationStack extends AbstractMojo {

	/* input data */

	/**
	 */
	@Parameter
	public Plan plan;
	@Parameter
	public BrokerConfig brokerConfig;
	@Parameter
	public String stackName;

	/**
	 * 
	 */
	public RedeployApplicationStack() {
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

		try {
			DeleteStacks ds = new DeleteStacks(brokerConfig, plan, stackName);
			ds.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			getLog().error(e);
			e.printStackTrace();
		}
		// continue with the component deletion;

		DeleteComponentsJSON dc = new DeleteComponentsJSON(brokerConfig, plan);
		dc.execute();

		CreateComponentsJSON c = new CreateComponentsJSON(brokerConfig, plan);
		try {
			plan.merge(this, c);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
		c.execute();

		getLog().info("Executing Stack Creation for Component ");

		CreateStacks s = new CreateStacks(brokerConfig, plan);
		s.execute();
	}

}
