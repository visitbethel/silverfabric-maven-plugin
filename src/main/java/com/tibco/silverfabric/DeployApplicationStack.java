/**
 * 
 */
package com.tibco.silverfabric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

import scm.tibco.plugins.AbstractTibcoMojo;
import scm.tibco.plugins.events.ResolveDependenciesEventImpl;

import com.tibco.silverfabric.components.CreateComponentRestCall;
import com.tibco.silverfabric.components.CreateComponentsJSON;
import com.tibco.silverfabric.model.Archive;
import com.tibco.silverfabric.model.Component;
import com.tibco.silverfabric.model.Model;
import com.tibco.silverfabric.model.PlanHelper;
import com.tibco.silverfabric.model.PlanModel;
import com.tibco.silverfabric.model.Stack;

/**
 * @author akaan
 *
 */
@Mojo(name = "deploy")
public class DeployApplicationStack extends AbstractTibcoMojo {

	/* input data */

	/**
	 */
	@Parameter(defaultValue = "src/main/resources/plan.xml")
	public File plan;
	@Parameter(required = true)
	public BrokerConfig brokerConfig;
	@Parameter(required = true)
	public String id;
	@Parameter(defaultValue = "${project.build.directory}/dependency")
	private String dependencyWorkDirectory = "target/dependency";

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
		if (!plan.exists()) {
			throw new MojoExecutionException("Planfile "
					+ plan.getAbsolutePath() + " does not exist.");
		}

		// STEP 1 - load plan and evaluate structure.
		PlanHelper helper = new PlanHelper();
		PlanModel planModel = helper.loadPlan(plan);

		// STEP 2 - resolve the enabler dependency
		initializeRepositorySystem();
		resolveDependencies(planModel.dependencies);

		// STEP 3 - load the stack and component templates
		com.fedex.scm.Components templateComponent = helper
				.loadComponentTemplate(this.dependencyWorkDirectory,
						"component.json");
		com.fedex.scm.Stacks templateStack = helper.loadStackTemplate(
				this.dependencyWorkDirectory, "stack.json");

		// STEP 4 - get work model
		Model model = helper.getModel(planModel, id);
		if (model == null) {
			throw new MojoExecutionException("Model for id=" + id
					+ " could not be found in the provided plan "
					+ this.plan.getAbsolutePath() + ".");
		}
		// STEP 5 - overlay stacks
		if (templateStack != null) {
			helper.overlay(templateStack, model);
		}
		else {
			getLog().warn("Taking the stack template as is, without plan overlay.");
		}

		// STEP 5 - overlay components
		if (templateComponent != null) {
			helper.overlay(templateComponent, model);
		}
		else {
			getLog().warn("Taking the component template as is, without plan overlay.");
		}

		executePlanFile(model, planModel.archives);
	}

	/**
	 * 
	 * @throws MojoFailureException
	 */
	public void resolveDependencies(List<Dependency> dependencies)
			throws MojoFailureException {
		if (dependencies != null && dependencies.size() > 0) {
			try {
				if (system == null) {
					throw new MojoFailureException(
							"repo system was not initialized");
				}
				ResolveDependenciesEventImpl event = new ResolveDependenciesEventImpl(
						this, getProject().getRemoteProjectRepositories(),
						dependencies, system, true);
				List<File> list = event.call();
				List<File> outputFiles = new ArrayList<File>();
				if (list.size() == 0) {
					throw new FileNotFoundException(
							"No Dependencies loaded although specified.");
				}
				for (File file : list) {
					debug(
							"copying dependency '" + file.getName() + "' to '"
									+ this.outputDirectory);
					String fileName = file.getName();
					File outFile = new File(this.outputDirectory, fileName);
					// FileUtils.copyFileToDirectory(file,
					// this.outputDirectory);
					FileUtils.copyFile(file, outFile);
					// unpack in work area
					unpackArchive(outFile, new File(dependencyWorkDirectory));
				}
			} catch (Exception e) {
				getLog().error(e);
				throw new MojoFailureException(e.getMessage(), e);
			}
		}
	}

	/**
	 * 
	 * @param arg0
	 * @param extractDirectory
	 * @return
	 * @throws IOException
	 */
	public boolean unpackArchive(File arg0, File extractDirectory)
			throws IOException {
		if (!extractDirectory.exists() || !extractDirectory.isDirectory()) {
			if (!extractDirectory.mkdirs()) {
				throw new FileNotFoundException("Unable to create structure "
						+ extractDirectory.getAbsolutePath() + ".");
			}
		}
		JarFile jar = new JarFile(arg0);
		Enumeration<JarEntry> enumEntries = jar.entries();
		File barFile = null;
		while (enumEntries.hasMoreElements()) {
			java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries
					.nextElement();
			java.io.File f = new java.io.File(extractDirectory, file.getName());

			// skip meta-inf directory
			if (file.getName().startsWith("META-INF")) {
				continue;
			}

			debug("  [dir=" + file.isDirectory() + "] " + file + ".");
			
			if (file.isDirectory()) { // if its a directory, create it
				f.mkdir();
				continue;
			}
			java.io.InputStream is = jar.getInputStream(file); // get the input
																// stream
			java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
			while (is.available() > 0) { // write contents of 'is' to 'fos'
				fos.write(is.read());
			}
			fos.close();
			is.close();
			getLog().info(file + " extracted.");
		}
		return true;
	}

	/**
	 * 
	 * @param pf
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	private void executePlanFile(Model m, List<Archive> archives) throws MojoExecutionException,
			MojoFailureException {

		info("found " + m.stacks.size() + " listed stacks.");

		int count = 1;
		for (Iterator<Stack> iterator = m.stacks.iterator(); iterator.hasNext();) {
			Stack stack = iterator.next();
			info(">> processing stack '" + stack.getName() + "'.");
			info(">>> components in stack " + stack.getComponents());
			for (Iterator citer = stack.components.iterator(); citer.hasNext();) {
				Component component = (Component) citer.next();
				CreateComponentRestCall c = new CreateComponentRestCall(component);
				c.execute();
			}
		}
	}
	
	private void debug(String message) {
		if (this.debug) {
			getLog().info(message);
		}
	}
	private void info(String message) {
		//if (this.debug) {
			getLog().info(message);
		//}
	}

}
