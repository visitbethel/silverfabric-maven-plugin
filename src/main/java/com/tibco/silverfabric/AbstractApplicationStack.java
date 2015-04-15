/**
 * 
 */
package com.tibco.silverfabric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

import com.tibco.silverfabric.model.Component;
import com.tibco.silverfabric.model.Model;
import com.tibco.silverfabric.model.PlanHelper;
import com.tibco.silverfabric.model.PlanModel;

import scm.tibco.plugins.AbstractTibcoMojo;
import scm.tibco.plugins.events.ResolveDependenciesEventImpl;

/**
 * @author akaan
 *
 */
public abstract class AbstractApplicationStack extends AbstractTibcoMojo {

	@Parameter(defaultValue = "src/main/resources/plan.xml")
	public String executionPlan;
	@Parameter(required = true)
	public BrokerConfig brokerConfig;
	@Parameter(required = true)
	public String id;
	@Parameter(defaultValue = "${project.build.directory}/dependency")
	public String dependencyWorkDirectory = "target/dependency";
	public String componentTemplateName = "component.json";
	public String stackTemplateName = "stack.json";

	protected final PlanHelper helper = new PlanHelper();
	protected PlanModel planModel;
	protected Model model;

	/**
	 * 
	 */
	protected AbstractApplicationStack() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see scm.core.AbstractRemoteInvocationMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (executionPlan == null) {
			throw new MojoExecutionException("plan must be supplied!");
		}
		if (brokerConfig == null) {
			throw new MojoExecutionException("brokerConfig must be supplied!");
		}
		File pf = new File(this.executionPlan);
		// short hand
		if (!pf.exists()) {
			throw new MojoExecutionException("Planfile "
					+ pf.getAbsolutePath() + " does not exist.");
		}

		// STEP 1 - load plan and evaluate structure.
		planModel = helper.loadPlan(pf);

		// STEP 2 - resolve the enabler dependency
		initializeRepositorySystem();
		resolveDependencies(planModel.dependencies);

		// STEP 3 - load the stack and component templates
		com.fedex.scm.Components templateComponent = helper
				.loadComponentTemplate(this.dependencyWorkDirectory,
						this.componentTemplateName);
		com.fedex.scm.Stacks templateStack = helper.loadStackTemplate(
				this.dependencyWorkDirectory, this.stackTemplateName);

		// STEP 4 - get work model
		model = helper.getModel(planModel, id);
		if (model == null) {
			throw new MojoExecutionException("Model for id=" + id
					+ " could not be found in the provided plan "
					+ pf.getAbsolutePath() + ".");
		}
		// STEP 5 - overlay stacks
		if (templateStack != null) {
			helper.overlay(templateStack, model);
		} else {
			getLog().warn(
					"Taking the stack template as is, without plan overlay.");
		}

		// STEP 5 - overlay components
		if (templateComponent != null) {
			helper.overlay(templateComponent, model);
		} else {
			getLog().warn(
					"Taking the component template as is, without plan overlay.");
		}

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
				if (getProject() == null) {
					return;
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
					debug("copying dependency '" + file.getName() + "' to '"
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

	protected void debug(String message) {
		if (this.debug) {
			getLog().info(message);
		}
	}

	protected void info(String message) {
		// if (this.debug) {
		getLog().info(message);
		// }
	}

}
