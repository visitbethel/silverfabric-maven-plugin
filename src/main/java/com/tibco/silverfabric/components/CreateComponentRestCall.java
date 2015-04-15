/**
 * 
 */
package com.tibco.silverfabric.components;

import java.io.File;
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
public class CreateComponentRestCall extends AbstractSilverComponentsRestCall {

	/**
	 * @param component
	 * 
	 */
	public CreateComponentRestCall(BrokerConfig brokerConfig,
			List<Archive> archives, Component component) {
		super();
		setBrokerConfig(brokerConfig);
		if (component == null) {
			throw new NullPointerException("Null component not allowed.");
		}
		setComponent(component);
		if (archives != null) {
			getArchives().addAll(archives);
		}

	}

	/**
	 * 
	 */
	public void initialize() throws MojoFailureException {
		super.initialize();
		getLog().info(
				"initializing component [" + component.getName() + "]: "
						+ this.toString());

		// STEP 1 - create work area
		if (component.workDirectory != null
				&& !component.workDirectory.exists()) {
			if (!component.workDirectory.mkdirs()) {
				throw new MojoFailureException("Create directory "
						+ this.workDirectory + " failed!");
			}
		}
		// STEP 2 - check if configure.xml exists and add it as
		// the configuration file.
		if (component.configFile != null) {
			String configFilePath = component.workDirectory.getAbsolutePath()
					+ File.separator + component.configFile;
			File configFile = new File(configFilePath);
			if (configFile.exists()) {
				this.setConfigFile(new Archive(configFile, ""));
				getLog().info(
						"registered " + configFile + " as configuration file.");
			} else {
				getLog().warn(
						"config file " + configFilePath + " does not exist.");
			}
		}

		// STEP 3 - check for content files that were extracted in the
		// work area under the content path and add them as archives
		String contentPath = component.workDirectory.getAbsolutePath()
				+ File.separator + component.contentDirectory;
		getLog().info("reading content-files from " + contentPath);
		File content = new File(contentPath);
		if (content.exists()) {
			recurseAdd(this.getContentFiles(), content, "");
			getLog().info(
					"registered " + this.getContentFiles().size()
							+ " files for content-files.");
		} else {
			getLog().error("content path " + contentPath + " does not exist.");
		}

		// STEP 4 - if archives were supplied at the construction, add
		// them to the upload list.
		if (getArchives() != null && !getArchives().isEmpty()) {
			getLog().info(
					"registered " + this.getArchives().size()
							+ " file(s) as archives.");
		} else {
			getLog().warn("No archives included in this plan.");
		}

		// STEP 5 - pattern factory script file with the interface for starting
		// stopping a certain enabler.
		if (component.scriptFile != null) {
			File af = new File(component.workDirectory.getAbsolutePath()
					+ File.separator + component.scriptFile);
			if (af.exists()) {
				this.setScriptFile(new Archive(af));
				this.setScriptLang("python");
				this.setScriptLangVersion("2.5");
			} else {
				getLog().warn(
						"script files were not found at "
								+ af.getAbsolutePath());
			}
		}
		//
		// End of Merging
		//

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

	/**
	 * 
	 * @param mojo
	 * @param root
	 * @param dir
	 */
	private void recurseAdd(List<Archive> root, File dir, String relPath) {
		File[] fs = dir.listFiles();
		for (int i = 0; i < fs.length; i++) {
			if (fs[i].isDirectory()) {
				recurseAdd(root, fs[i], fs[i].getName());
			} else {
				root.add(new Archive(fs[i], relPath));
				getLog().info(
						"  > adding " + fs[i] + " with path[" + relPath + "].");
			}
		}
	}

}
