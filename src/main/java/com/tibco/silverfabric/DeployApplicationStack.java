/**
 * 
 */
package com.tibco.silverfabric;

import java.io.File;
import java.util.Iterator;
import java.util.List;

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
	public Plan plan;
	@Parameter
	public BrokerConfig brokerConfig;
	@Parameter
	public String planFile;

	/**
	 * 
	 */
	public DeployApplicationStack() {
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

		// short hand

		if (planFile != null) {
			File pf = new File(planFile);
			if (!pf.exists()) {
				throw new MojoExecutionException("Planfile "
						+ pf.getAbsolutePath() + " does not exist.");
			}
			executePlanFile(pf);
		} else if (plan.components != null && !plan.components.isEmpty()) {
			executeLocalPlan(plan.components);
		} else {
			throw new MojoExecutionException(
					"planFile or localPlan must be specified");
		}

		getLog().info("found " + plan.components.size() + " listed components.");
		int count = 1;
		for (Iterator<String> iterator = plan.components.iterator(); iterator
				.hasNext();) {
			String component = (String) iterator.next();
			getLog().info(">> Deploying #" + count + ": " + component + ".");
			CreateComponentsJSON c = new CreateComponentsJSON(brokerConfig,
					plan, component);
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

	/**
	 * 
	 * @param pf
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	private void executePlanFile(File pf) throws MojoExecutionException, MojoFailureException {
		PlanHelper helper = new PlanHelper();
		PlanModel model = helper.loadPlan(pf);
		Model m = model.models.get(0);
		getLog().info("found " + m.components.size() + " listed components.");
		getLog().info("found " + m.componentDependencies.size() + " listed componentdependencies.");
		List<Component> components = m.components;
		int count = 1;
		for (Iterator<Component> iterator = components.iterator(); iterator
				.hasNext();) {
			Component component = (Component) iterator.next();
			getLog().info(">> Deploying #" + count + ": " + component + ".");
			CreateComponentsJSON c = new CreateComponentsJSON(brokerConfig,
					plan, component.name);
			try {
				plan.merge(this, c);
			} catch (Exception e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
			c.execute();
			
			// STACKS
			List<Stack> stax = component.stacks;
			for (Iterator iterator2 = stax.iterator(); iterator2.hasNext();) {
				Stack stack = (Stack) iterator2.next();
				CreateStacks s = new CreateStacks(brokerConfig, plan, stack.name);
				s.execute();
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
	private void executeLocalPlan(List<String> components)
			throws MojoExecutionException, MojoFailureException {
		getLog().info("found " + components.size() + " listed components.");
		int count = 1;
		for (Iterator<String> iterator = components.iterator(); iterator
				.hasNext();) {
			String component = (String) iterator.next();
			getLog().info(">> Deploying #" + count + ": " + component + ".");
			CreateComponentsJSON c = new CreateComponentsJSON(brokerConfig,
					plan, component);
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
