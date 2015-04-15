/**
 * 
 */
package com.tibco.silverfabric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.AbstractMavenFilteringRequest;
import org.apache.maven.shared.filtering.DefaultMavenFileFilter;
import org.apache.maven.shared.filtering.DefaultMavenReaderFilter;
import org.apache.maven.shared.filtering.FilteringUtils;
import org.apache.maven.shared.filtering.MavenFileFilterRequest;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenReaderFilter;
import org.apache.maven.shared.filtering.MultiDelimiterInterpolatorFilterReaderLineEnding;
import org.apache.maven.shared.utils.io.FileUtils;
import org.apache.maven.shared.utils.io.FileUtils.FilterWrapper;
import org.apache.maven.shared.utils.io.IOUtil;
import org.codehaus.plexus.interpolation.InterpolationPostProcessor;
import org.codehaus.plexus.interpolation.PrefixAwareRecursionInterceptor;
import org.codehaus.plexus.interpolation.PrefixedObjectValueSource;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.RecursionInterceptor;
import org.codehaus.plexus.interpolation.SimpleRecursionInterceptor;
import org.codehaus.plexus.interpolation.ValueSource;
import org.codehaus.plexus.interpolation.multi.MultiDelimiterStringSearchInterpolator;

/**
 * @author akaan
 * 
 */
public class DeploymentFileFilter extends DefaultMavenFileFilter {

	private static final class Wrapper extends FileUtils.FilterWrapper {

		private LinkedHashSet<String> delimiters;

		private String escapeString;

		private boolean escapeWindowsPaths;

		private MavenProject project;

		private List<String> projectStartExpressions;

		private ValueSource propertiesValueSource;

		private boolean supportMultiLineFiltering;

		Wrapper(LinkedHashSet<String> delimiters,
				ValueSource propertiesValueSource,
				List<String> projectStartExpressions, String escapeString,
				boolean escapeWindowsPaths, boolean supportMultiLineFiltering) {
			super();
			this.delimiters = delimiters;
			this.propertiesValueSource = propertiesValueSource;
			this.projectStartExpressions = projectStartExpressions;
			this.escapeString = escapeString;
			this.escapeWindowsPaths = escapeWindowsPaths;
			this.supportMultiLineFiltering = supportMultiLineFiltering;
		}

		@Override
		public Reader getReader(Reader reader) {
			MultiDelimiterStringSearchInterpolator interpolator = new MultiDelimiterStringSearchInterpolator();
			interpolator.setDelimiterSpecs(delimiters);

			RecursionInterceptor ri = null;
			if (projectStartExpressions != null
					&& !projectStartExpressions.isEmpty()) {
				ri = new PrefixAwareRecursionInterceptor(
						projectStartExpressions, true);
			} else {
				ri = new SimpleRecursionInterceptor();
			}

			interpolator.addValueSource(propertiesValueSource);

			if (project != null) {
				interpolator.addValueSource(new PrefixedObjectValueSource(
						projectStartExpressions, project, true));
			}

			interpolator.setEscapeString(escapeString);

			if (escapeWindowsPaths) {
				interpolator.addPostProcessor(new InterpolationPostProcessor() {
					public Object execute(String expression, Object value) {
						if (value instanceof String) {
							return FilteringUtils
									.escapeWindowsPath((String) value);
						}

						return value;
					}
				});
			}

			MultiDelimiterInterpolatorFilterReaderLineEnding filterReader = new MultiDelimiterInterpolatorFilterReaderLineEnding(
					reader, interpolator, supportMultiLineFiltering);
			filterReader.setRecursionInterceptor(ri);
			filterReader.setDelimiterSpecs(delimiters);

			filterReader.setInterpolateWithPrefixPattern(false);
			filterReader.setEscapeString(escapeString);

			return filterReader;
		}

	}

	// compensate for null parameter value.
	final AbstractMavenFilteringRequest request = new MavenFileFilterRequest();
	final MavenReaderFilter readerFilter;

	/**
	 * 
	 */
	public DeploymentFileFilter() {
		super();
		this.readerFilter = new DefaultMavenReaderFilter();
	}

	/**
	 * @param from
	 * @param to
	 * @param filtering
	 * @param filterProperties
	 * @param encoding
	 * @throws MavenFilteringException
	 * @throws FileNotFoundException 
	 */
	public void copyFile(File from, File to, boolean filtering,
			Properties filterProperties, String encoding)
			throws Exception {
		final ValueSource propertiesValueSource = new PropertiesBasedValueSource(
				filterProperties);
		@SuppressWarnings("unchecked")
		Wrapper wrapper = new Wrapper(request.getDelimiters(),
				propertiesValueSource, request.getProjectStartExpressions(),
				request.getEscapeString(), request.isEscapeWindowsPaths(),
				request.isSupportMultiLineFiltering());
		List<FileUtils.FilterWrapper> defaultFilterWrappers = new ArrayList<FileUtils.FilterWrapper>(
				1);
		defaultFilterWrappers.add(wrapper);
		filterFile(from, to, defaultFilterWrappers);
	}
	

	/**
	 * 
	 * @param from
	 * @param to
	 * @param encoding
	 * @param wrappers
	 * @throws IOException
	 * @throws MavenFilteringException
	 * @throws FileNotFoundException 
	 */
	private void filterFile(@Nonnull File from, @Nonnull File to,
			 @Nullable List<FilterWrapper> wrappers)
			throws Exception {
		if (wrappers != null && wrappers.size() > 0) {
			Reader fileReader = null;
			Writer fileWriter = null;
			try {
				fileReader = new FileReader(from);
				fileWriter = new FileWriter(to);
				Reader src = readerFilter.filter(fileReader, true, wrappers);

				IOUtil.copy(src, fileWriter);
			} finally {
				IOUtil.close(fileReader);
				IOUtil.close(fileWriter);
			}
		} else {
			if (to.lastModified() < from.lastModified()) {
				FileUtils.copyFile(from, to);
			}
		}
	}


}
