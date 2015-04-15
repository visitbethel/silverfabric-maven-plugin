package com.tibco.silverfabric.model;

import java.io.File;
import java.util.Properties;

import com.fedex.scm.Components;

public class Component extends Components {

	public File workDirectory = new File("target/work");
	public String componentTemplate = "component.json";
	public String stackTemplate = "stack.json";
	public String configFile = "config/configure.xml";
	public String contentDirectory = "content";
	public String scriptLanguage = "python";
	public String scriptLanguageVersion = "2.5";
	public String scriptFile = "scripts/sfs_component_script.py";

	public Properties properties;

	public Component() {
		super();
	}

	public Component(String name) {
		super();
		setName(name);
	}

	/**
	 * @return the workDirectory
	 */
	public final File getWorkDirectory() {
		return workDirectory;
	}

	/**
	 * @param workDirectory the workDirectory to set
	 */
	public final void setWorkDirectory(File workDirectory) {
		this.workDirectory = workDirectory;
	}

	/**
	 * @return the componentTemplate
	 */
	public final String getComponentTemplate() {
		return componentTemplate;
	}

	/**
	 * @param componentTemplate the componentTemplate to set
	 */
	public final void setComponentTemplate(String componentTemplate) {
		this.componentTemplate = componentTemplate;
	}

	/**
	 * @return the stackTemplate
	 */
	public final String getStackTemplate() {
		return stackTemplate;
	}

	/**
	 * @param stackTemplate the stackTemplate to set
	 */
	public final void setStackTemplate(String stackTemplate) {
		this.stackTemplate = stackTemplate;
	}

	/**
	 * @return the configFile
	 */
	public final String getConfigFile() {
		return configFile;
	}

	/**
	 * @param configFile the configFile to set
	 */
	public final void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	/**
	 * @return the contentDirectory
	 */
	public final String getContentDirectory() {
		return contentDirectory;
	}

	/**
	 * @param contentDirectory the contentDirectory to set
	 */
	public final void setContentDirectory(String contentDirectory) {
		this.contentDirectory = contentDirectory;
	}

	/**
	 * @return the scriptLanguage
	 */
	public final String getScriptLanguage() {
		return scriptLanguage;
	}

	/**
	 * @param scriptLanguage the scriptLanguage to set
	 */
	public final void setScriptLanguage(String scriptLanguage) {
		this.scriptLanguage = scriptLanguage;
	}

	/**
	 * @return the scriptLanguageVersion
	 */
	public final String getScriptLanguageVersion() {
		return scriptLanguageVersion;
	}

	/**
	 * @param scriptLanguageVersion the scriptLanguageVersion to set
	 */
	public final void setScriptLanguageVersion(String scriptLanguageVersion) {
		this.scriptLanguageVersion = scriptLanguageVersion;
	}

	/**
	 * @return the scriptFile
	 */
	public final String getScriptFile() {
		return scriptFile;
	}

	/**
	 * @param scriptFile the scriptFile to set
	 */
	public final void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
	}

	/**
	 * @return the properties
	 */
	public final Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public final void setProperties(Properties properties) {
		this.properties = properties;
	}


}
