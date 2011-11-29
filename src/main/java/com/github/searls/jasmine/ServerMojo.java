package com.github.searls.jasmine;

import java.io.File;
import java.io.IOException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import com.github.searls.jasmine.io.RelativizesFilePaths;
import com.github.searls.jasmine.server.JasmineResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

/**
 * @goal bdd
 * @execute lifecycle="jasmine-lifecycle" phase="generate-sources"
 * @requiresDirectInvocation true
 */
public class ServerMojo extends AbstractJasmineMojo {

	public static final String INSTRUCTION_FORMAT =
			"\n\n" +
					"Server started--it's time to spec some JavaScript! You can run your specs as you develop by visiting this URL in a web browser: \n\n" +
					"  http://localhost:%s" +
					"\n\n" +
					"The server will monitor these two directories for scripts that you add, remove, and change:\n\n" +
					"  source directory: %s\n\n" +
					"  spec directory: %s" +
					"\n\n" +
					"Just leave this process running as you test-drive your code, refreshing your browser window to re-run your specs. You can kill the server with Ctrl-C when you're done.";

	private Server server = new Server();

	private RelativizesFilePaths relativizesFilePaths = new RelativizesFilePaths();

	@Override
	public void run() throws Exception {
		addConnectorToServer();
		addHandlersToServer();
		startServer();
	}

	private void addConnectorToServer() {
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(serverPort);
		server.addConnector(connector);
	}

	private void addHandlersToServer() throws IOException {
		ResourceHandler resourceHandler = createResourceHandler();
		ResourceHandler srcHandler = createSrcResourceHandler();
		ResourceHandler specResourceHandler = createSpecResourceHandler();

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		ContextHandler srcContextHandler = contexts.addContext("/"+ this.srcDirectoryName, "");
		srcContextHandler.setHandler(srcHandler);

		ContextHandler specContextHandler = contexts.addContext("/" + this.specDirectoryName, "");
		specContextHandler.setHandler(specResourceHandler);

		ContextHandler resourceContextHandler = contexts.addContext("/", "");
		resourceContextHandler.setHandler(createResourceHandler());

		server.setHandler(contexts);

	}

	private ResourceHandler createSrcResourceHandler() throws IOException {
		ResourceHandler resourceHandler = new ResourceHandler();

		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setResourceBase(this.getSources().getDirectory().getAbsolutePath());
		return resourceHandler;
	}

	private ResourceHandler createSpecResourceHandler() throws IOException {
		ResourceHandler resourceHandler = new ResourceHandler();

		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setResourceBase(this.getSpecs().getDirectory().getAbsolutePath());
		return resourceHandler;
	}

	private ResourceHandler createResourceHandler() throws IOException {
		ResourceHandler resourceHandler = new JasmineResourceHandler(this);
		resourceHandler.setDirectoriesListed(false);
		resourceHandler.setWelcomeFiles(new String[]{manualSpecRunnerPath()});
		resourceHandler.setResourceBase(mavenProject.getBasedir().getAbsolutePath());
		return resourceHandler;
	}

	private void startServer() throws Exception {
		server.start();
		getLog().info(buildServerInstructions());
		server.join();
	}

	private String buildServerInstructions() throws IOException {
		return String.format(
				INSTRUCTION_FORMAT,
				serverPort,
				relativizesFilePaths.relativize(mavenProject.getBasedir(), sources.getDirectory()),
				relativizesFilePaths.relativize(mavenProject.getBasedir(), specs.getDirectory()));
	}

	private String manualSpecRunnerPath() throws IOException {
		return relativizesFilePaths.relativize(mavenProject.getBasedir(), jasmineTargetDir) + File.separator + manualSpecRunnerHtmlFileName;
	}

}
