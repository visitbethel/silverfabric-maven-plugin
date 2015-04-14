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
import com.tibco.silverfabric.model.Component;
import com.tibco.silverfabric.model.Model;
import com.tibco.silverfabric.model.Plan;
import com.tibco.silverfabric.model.PlanHelper;
import com.tibco.silverfabric.model.PlanModel;
import com.tibco.silverfabric.model.Stack;
import com.tibco.silverfabric.stacks.CreateStacks;

/**
 * @author akaan
 *
 */
@Mojo(name = "deploy")
public class DeployApplicationStack extends AbstractMojo {

	/* input data */

	/**
	 */
	@Parameter
	public Plan plan = new Plan();
	@Parameter
	public BrokerConfig brokerConfig;
	@Parameter
	public String planFile;

	/**
	 * 
	 */
	public DeployApplicationStack(BrokerConfig config, Plan plan) {
		super();
		this.plan = plan;
		this.brokerConfig = config;
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
			executeLocalPlan(plan.components);
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

			for (Iterator<Component> iter = stack.components.iterator(); iter
					.hasNext();) {
				Component component = (Component) iter.next();
				getLog().info(">> Deploying #" + count + ": " + component + ".");
				Properties filters = new Properties();
				if (stack.properties != null) {
					filters.putAll(stack.properties);
				}
				if (component.properties != null ) {
					filters.putAll(component.properties);
				}
				filters.putAll(component.properties);
				getLog().info("component.properties: " + filters);
				CreateComponentsJSON c = new CreateComponentsJSON(brokerConfig,
						plan, component.name, filters);
				try {
					plan.merge(this, c);
				} catch (Exception e) {
					throw new MojoExecutionException(e.getMessage(), e);
				}
				c.execute();

				// STACKS
			}
			getLog().info("stack.properties: " + stack.properties);

			CreateStacks s = new CreateStacks(brokerConfig, plan.stackPlan,
					stack.components, stack.properties);
			s.execute();

			count++;
		}
	}

	/**
	 * 
	 * @param components
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	private void executeLocalPlan(List<String> components)
			throws MojoExecutionException, MojoFailureException {
		getLog().info("found " + components.size() + " listed components.");
		int count = 1;
		for (Iterator<String> iterator = components.iterator(); iterator
				.hasNext();) {
			String component = (String) iterator.next();
			getLog().info(">> Deploying #" + count + ": " + component + ".");
			CreateComponentsJSON c = new CreateComponentsJSON(brokerConfig,
					plan, component, new Properties());
			try {
				plan.merge(this, c);
			} catch (Exception e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
			c.execute();
			count++;
		}

		getLog().info("Executing Stack Creation for Component ");

		CreateStacks s = new CreateStacks(brokerConfig, plan);
		s.execute();
	}

}
