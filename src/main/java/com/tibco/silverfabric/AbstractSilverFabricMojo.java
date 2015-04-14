package com.tibco.silverfabric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFileFilterRequest;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.IOUtil;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractSilverFabricMojo extends AbstractMojo {

	// /opt/tibco/silver/5.6.0/fabric/webapps/livecluster/WEB-INF/log/server

	/**
	 * 
	 * @param request
	 * @param string
	 * @param description2
	 * @param object
	 */
	protected static Object valueOf(HashMap<Object, Object> request,
			String string, Object a, Object b) {
		Object value = valueOf(a, b);
		if (value != null) {
			request.put(string, value);
			return value;
		}
		return null;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	protected static Object valueOf(Object a, Object b) {
		if (a == null && b == null) {
			return null;
		}
		if (a == null) {
			return b;
		} else {
			return a;
		}
	}

	@Parameter
	private LinkedList<String> actions;

	@Parameter(required = true)
	private BrokerConfig brokerConfig;

	@Parameter(defaultValue = "${session}", readonly = true)
	protected MavenSession session;

	@Parameter(defaultValue = "${project}", readonly = true)
	protected MavenProject project;

	/**
	 * Maven shared filtering utility.
	 */
	@Component
	public MavenFileFilter mavenFileFilter;

	public final RestTemplate restTemplate; // =
											// ctx.getBean(RestTemplate.class);
	public final HttpComponentsClientHttpRequestFactory clientRequestfactory;

	public AbstractSilverFabricMojo() {
		super();
		this.restTemplate = new SilverFabricConfig().restTemplate();
		this.clientRequestfactory = (HttpComponentsClientHttpRequestFactory) restTemplate
				.getRequestFactory();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		DefaultHttpClient httpClient = (DefaultHttpClient) this.clientRequestfactory
				.getHttpClient();
		if (this.brokerConfig == null) {
			throw new MojoExecutionException("Missing brokerConfig!");
		}
		if (this.brokerConfig == null
				|| this.brokerConfig.getBrokerURL() == null) {
			getLog().error(this.brokerConfig.toString());
			throw new MojoExecutionException(
					"Missing brokerConfig credentials!");
		}
		if (httpClient == null) {
			throw new MojoExecutionException("Missing httpClient!");
		}
		if (httpClient.getCredentialsProvider() == null) {
			throw new MojoExecutionException(
					"Missing httpClient.credential provider!");
		}
		getLog().info(brokerConfig.toString());
		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(brokerConfig.getBrokerURL().getHost(),
						brokerConfig.getBrokerURL().getPort(),
						AuthScope.ANY_REALM),
				new UsernamePasswordCredentials(brokerConfig.getUsername(),
						brokerConfig.getPassword()));
		executeMojo();
	}

	public abstract void executeMojo() throws MojoExecutionException,
			MojoFailureException;

	public LinkedList<String> getActions() {
		return actions;
	}

	public BrokerConfig getBrokerConfig() {
		return brokerConfig;
	}

	/**
	 * @return the restTemplate
	 */
	public final RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setActions(LinkedList<String> actions) {
		this.actions = actions;
	}

	public void setBrokerConfig(BrokerConfig brokerConfig) {
		this.brokerConfig = brokerConfig;
	}

	/**
	 * 
	 * @param outputDirectory
	 * @param path
	 * @return
	 * @throws MojoFailureException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public File filterFile(File outputDirectory, File sourcePlan,
			Properties filterProperties) throws MojoFailureException {
		File prefiltered = new File(outputDirectory, sourcePlan.getName()
				+ ".prefiltered");
		DeploymentFileFilter ff = new DeploymentFileFilter();
		ff.enableLogging(new ConsoleLogger());
		try {
			ff.copyFile(sourcePlan, prefiltered, true, filterProperties, "UTF-8");
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			throw new MojoFailureException("Filtering plan " + prefiltered
					+ ".", e2);
		}
		File outputPlan = new File(outputDirectory, prefiltered.getName()
				+ ".filtered");
		/*
		 * if we are running testcases unharnessed with the maven runtime we
		 * still want to be able to run, however without filtering.
		 */
		if (project != null) {
			MavenFileFilterRequest req = new MavenFileFilterRequest(prefiltered,
					outputPlan, true, project, this.project.getFilters(), true,
					"UTF-8", session, null);
			try {
				if (outputDirectory != null && !outputDirectory.exists()) {
					if (!outputDirectory.mkdirs()) {
						throw new MojoFailureException(
								"Failure to create directories to publish plan to "
										+ outputDirectory);
					}
				}
				this.mavenFileFilter.copyFile(req);
			} catch (MavenFilteringException e1) {
				// TODO Auto-generated catch block
				throw new MojoFailureException("Publishing plan", e1);
			}
			return outputPlan;
		} else {
			try {
				IOUtil.copy(new FileInputStream(prefiltered),
						new FileOutputStream(outputPlan));
				return outputPlan;
			} catch (IOException e) {
				throw new MojoFailureException("Unable to copy plan '"
						+ sourcePlan + "'.", e);

			}
		}
	}
}
