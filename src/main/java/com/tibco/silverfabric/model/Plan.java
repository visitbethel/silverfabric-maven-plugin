/**
 * 
 */
package com.tibco.silverfabric.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fedex.scm.ComponentAllocationInfo;
import com.fedex.scm.Policy;
import com.tibco.silverfabric.components.CreateComponentsJSON;
import com.tibco.silverfabric.stacks.CreateStacks;

/**
 * @author akaan
 *
 */
public class Plan {

	/**
	 * 
	 */
	final static Logger LOGGER = LoggerFactory.getLogger(Plan.class);

	@Parameter(defaultValue = "${project.build.directory}/work")
	public File workDirectory = new File("target/work");
	public String componentPlan = "component.json";
	public String stackPlan = "stack.json";
	public String configFile = "config/configure.xml";
	@Parameter(defaultValue = "content")
	public String contentDirectory;
	public String scriptLanguage = "python";
	public String scriptLanguageVersion = "2.5";
	public String scriptFile = "scripts/sfs_component_script.py";
	private List<Archive> archives = new LinkedList<Archive>();

	public List<String> components = new LinkedList<String>();

	/**
	 * 
	 */
	public Plan() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Plan [workDirectory=" + workDirectory + ", componentPlan="
				+ componentPlan + ", stackPlan=" + stackPlan + ", configFile="
				+ configFile + ", contentDirectory=" + contentDirectory
				+ ", scriptLanguage=" + scriptLanguage
				+ ", scriptLanguageVersion=" + scriptLanguageVersion
				+ ", scriptFile=" + scriptFile + ", archives=" + archives
				+ ", components=" + components + "]";
	}

	/**
	 * 
	 * @param c
	 */
	public void merge(AbstractMojo log, CreateComponentsJSON c)
			throws Exception {
		if (c == null) {
			return;
		}
		if (this.workDirectory != null && !this.workDirectory.exists()) {
			if (!this.workDirectory.mkdirs()) {
				throw new FileNotFoundException("Create directory "
						+ this.workDirectory + " failed!");
			}
		}
		if (this.scriptFile != null) {
			File af = new File(this.workDirectory.getAbsolutePath()
					+ File.separator + scriptFile);
			if (af.exists()) {
				c.setScriptFile(new Archive(af));
				c.setScriptLang(this.scriptLanguage);
				c.setScriptLangVersion(this.scriptLanguageVersion);
			} else {
				log.getLog().warn(
						"script files were not found at "
								+ af.getAbsolutePath());
			}
		}
		String contentPath = this.workDirectory.getAbsolutePath()
				+ File.separator + this.contentDirectory;
		log.getLog().info("reading content-files from " + contentPath);
		File content = new File(contentPath);
		if (content.exists()) {
			recurseAdd(log, c.getContentFiles(), content, "");
			log.getLog().info(
					"registered " + c.getContentFiles().size()
							+ " files for content-files.");
		} else {
			log.getLog().error(
					"content path " + contentPath + " does not exist.");
		}
		String configFilePath = this.workDirectory.getAbsolutePath()
				+ File.separator + this.configFile;
		File configFile = new File(configFilePath);
		if (configFile.exists()) {
			c.setConfigFile(new Archive(configFile, ""));
			log.getLog().info(
					"registered " + configFile + " as configuration file.");
		} else {
			log.getLog().error(
					"config file " + configFilePath + " does not exist.");
		}
		if (this.archives != null && !this.archives.isEmpty()) {
			for (Iterator<Archive> iterator = archives.iterator(); iterator
					.hasNext();) {
				Archive archive = (Archive) iterator.next();
				c.getArchives().add(archive);
			}
			log.getLog().info(
					"registered " + c.getArchives().size()
							+ " file(s) as archives.");
		} else {
			log.getLog().warn("No archives included in this plan.");
		}

	}

	/**
	 * 
	 * @param c
	 */
	public void merge(AbstractMojo log, CreateStacks c) throws Exception {
		if (c == null) {
			return;
		}
	}

	/**
	 * 
	 * @param mojo
	 * @param root
	 * @param dir
	 */
	private void recurseAdd(AbstractMojo log, List<Archive> root, File dir,
			String relPath) {
		File[] fs = dir.listFiles();
		for (int i = 0; i < fs.length; i++) {
			if (fs[i].isDirectory()) {
				recurseAdd(log, root, fs[i], fs[i].getName());
			} else {
				root.add(new Archive(fs[i], relPath));
				log.getLog().info(
						"adding " + fs[i] + " with path[" + relPath + "].");
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public File getComponentPlanPath() {
		if (componentPlan.startsWith("/")) {
			return new File(componentPlan);
		}
		return new File(this.workDirectory.getAbsoluteFile(), componentPlan);
	}

	/**
	 * 
	 * @return
	 */
	public File getStackPlanPath() {
		if (this.stackPlan != null && this.stackPlan.startsWith("/")) { 
			return new File(this.stackPlan);
		}
		return new File(this.workDirectory, stackPlan);
	}

	public static ComponentAllocationInfo toComponentAllocation(String component) {
		ComponentAllocationInfo info = new ComponentAllocationInfo();
		info.setMax(1);
		info.setMin(1);
		info.setPriority(new Integer(4));
		info.setName(component);
		return info;
	}

	/**
	 * 
	 * @param stack
	 * @return
	 */
	public static Policy getFirstPolicy(com.fedex.scm.Stacks stack) {
		if (stack.getPolicies().isEmpty()) {
			Policy p = new Policy();
			p.setScheduled(false);
			p.setScheduleName(null);
			stack.getPolicies().add(p);
			return p;
		} else {
			return stack.getPolicies().get(0);
		}
	}

	/**
	 * 
	 * @param p
	 * @return
	 */
	public static ComponentAllocationInfo getFirstComponentAllocationInfo(
			String name) {
		ComponentAllocationInfo ca = new ComponentAllocationInfo();
		ca.setMax(1);
		ca.setMin(1);
		ca.setPriority(4);
		ca.setName(name);
		return ca;
	}

}
