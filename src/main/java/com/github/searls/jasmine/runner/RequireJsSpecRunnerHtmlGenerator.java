package com.github.searls.jasmine.runner;

import org.antlr.stringtemplate.StringTemplate;

import java.io.IOException;
import java.util.Set;

import static java.util.Arrays.asList;

public class RequireJsSpecRunnerHtmlGenerator extends AbstractSpecRunnerHtmlGenerator implements SpecRunnerHtmlGenerator {

	public static final String REQUIRE_JS_HTML_TEMPLATE_FILE = "/jasmine-templates/RequireJsSpecRunner.htmltemplate";
	public static final String REQUIRE_JS = "/vendor/js/require.js";

	protected RequireJsSpecRunnerHtmlGenerator(HtmlGeneratorConfiguration configuration) {
		super(configuration);
	}

	public String generate() {
		try {
			return generateHtml(getConfiguration().getSpecs(), getConfiguration().getSourceDirectory());
		} catch (IOException e) {
			throw new RuntimeException("Failed to load files for dependencies, sources, or a custom runner", e);
		}
	}

	public String generateWitRelativePaths() {
		try {
			return generateHtml(getConfiguration().getSpecsRelativePath(), getConfiguration().getSourceDirectoryRelativePath());
		} catch (IOException e) {
			throw new RuntimeException("Failed to load files for dependencies, sources, or a custom runner", e);
		}
	}

	private String generateHtml(Set<String> specsRelativePath, String sourceDirectory) throws IOException {
		StringTemplate template = resolveHtmlTemplate();

		includeJavaScriptDependencies(asList(JASMINE_JS, JASMINE_HTML_JS, REQUIRE_JS), template);
		applyCssToTemplate(asList(JASMINE_CSS), template);
		Set<String> preloads = getConfiguration().getPreloadsRelativePath();
		template.setAttribute("priority", createArrayOfScripts(preloads));
		setCustomConfig(template);
		template.setAttribute(REPORTER_ATTR_NAME, getConfiguration().getReporterType().name());
//		template.setAttribute("sourceDir", sourceDirectory);
		template.setAttribute("specs", createArrayOfScripts(specsRelativePath));
		setEncoding(getConfiguration(), template);

		return template.toString();
	}

	private void setCustomConfig(StringTemplate template) throws IOException {
		String customConfig = getConfiguration().getCustomRunnerConfiguration();
		if (null != customConfig) {
			template.setAttribute("customConfig", customConfig);
		}
	}

	private String createArrayOfScripts(Set<String> scripts) throws IOException {
		if (null == scripts || scripts.isEmpty()) {
			return null;
		}
		StringBuilder builder = new StringBuilder("[");
		for (String script : scripts) {
			builder.append("'" + script + "'");
			builder.append(", ");
		}
		if (!scripts.isEmpty()) {
			builder.delete(builder.lastIndexOf(", "), builder.length());
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	protected String getDefaultHtmlTemplatePath() {
		return REQUIRE_JS_HTML_TEMPLATE_FILE;
	}
}
