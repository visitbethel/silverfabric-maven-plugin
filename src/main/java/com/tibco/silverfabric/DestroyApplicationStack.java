/**
 * 
 */
package com.tibco.silverfabric;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.tibco.silverfabric.components.CreateComponentsJSON;
import com.tibco.silverfabric.components.DeleteComponentsJSON;
import com.tibco.silverfabric.model.Component;
import com.tibco.silverfabric.model.Model;
import com.tibco.silverfabric.model.Plan;
import com.tibco.silverfabric.model.PlanHelper;
import com.tibco.silverfabric.model.PlanModel;
import com.tibco.silverfabric.model.Stack;
import com.tibco.silverfabric.stacks.CreateStacks;
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
	@Parameter
	public String planFile;
	@Parameter
	public String stackName;

	/**
	 * 
	 */
	public DestroyApplicationStack(BrokerConfig config, Plan plan, String name) {
		super();
		this.brokerConfig = config;
		this.plan = plan;
		this.stackName = name;
	}

	/**
	 * 
	 */
	public DestroyApplicationStack(BrokerConfig config, Plan plan) {
		this(config, plan, null);
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

		// short hand

		if (planFile != null) {
			File pf = new File(planFile);
			if (!pf.exists()) {
				throw new MojoExecutionException("Planfile "
						+ pf.getAbsolutePath() + " does not exist.");
			}
			getLog().info("loading external plan from " + pf.getAbsolutePath());
			executePlanFile(pf);
		} else if (plan.components != null && !plan.components.isEmpty()) {
			executeLocalPlan(plan.components, stackName);
		} else {
			throw new MojoExecutionException(
					"planFile or localPlan must be specified");
		}

	}

	/**
	 * 
	 * @param pf
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	private void executePlanFile(File pf) throws MojoExecutionException,
			MojoFailureException {
		PlanHelper helper = new PlanHelper();
		PlanModel model = helper.loadPlan(pf);
		Model m = model.models.get(0);

		getLog().info("found " + m.stacks.size() + " listed stacks.");

		int count = 1;
		for (Iterator<Stack> iterator = m.stacks.iterator(); iterator.hasNext();) {
			Stack stack = iterator.next();
			getLog().info("stack.properties: " + stack.properties);
			DeleteStacks s = new DeleteStacks(brokerConfig, plan,
					stack.name, stack.properties);
			s.execute();

			for (Iterator<Component> iter = stack.components.iterator(); iter
					.hasNext();) {
				Component component = (Component) iter.next();
				getLog().info(
						">> Destroying #" + count + ": " + component + ".");
				Properties filters = new Properties();
				if (stack.properties != null) {
					filters.putAll(stack.properties);
				}
				if (component.properties != null ) {
					filters.putAll(component.properties);
				}
				filters.putAll(component.properties);
				getLog().info("model.properties: " + filters);
				DeleteComponentsJSON c = new DeleteComponentsJSON(brokerConfig,
						plan, component.name, filters);
				c.execute();

				// STACKS
			}

			count++;
		}
	}

	/**
	 * 
	 * @param components
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	private void executeLocalPlan(List<String> components, String name)
			throws MojoExecutionException, MojoFailureException {
		try {
			DeleteStacks s = new DeleteStacks(brokerConfig, plan, name);
			s.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			getLog().error(e);
		}
		// continue with the component deletion;

		getLog().info("found " + plan.components.size() + " listed components.");
		int count = 1;
		for (Iterator iterator = plan.components.iterator(); iterator.hasNext();) {
			String component = (String) iterator.next();
			getLog().info(">> Deploying #" + count + ": " + component + ".");
			DeleteComponentsJSON c = new DeleteComponentsJSON(brokerConfig,
					plan, component);
			c.execute();
			count++;
		}
	}

}
