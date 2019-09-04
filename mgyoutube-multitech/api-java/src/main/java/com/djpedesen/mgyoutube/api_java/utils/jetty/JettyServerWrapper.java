package com.djpedesen.mgyoutube.api_java.utils.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class JettyServerWrapper {

	// TODO tie this to the Jetty constant that defines its default port
	public static final int DEFAULT_PORT = 8080;

	private final Server server;
	private final int port;

	public JettyServerWrapper() {
		this(DEFAULT_PORT);
	}

	public JettyServerWrapper(final int port) {
		this.server = new Server();
		this.port = port;
	}

	public void start() throws Exception {
		final ServerConnector connector = new ServerConnector(this.server);
		connector.setPort(this.port);
		this.server.setConnectors(new Connector[] { connector });
		this.server.start();
	}

	public void stop() throws Exception {
		this.server.stop();
	}

	public ServletHolder addServletContext(final String pathSpec) {
		// TODO: no session param here should be configurable
		final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

		// TODO: should parameterize or derive this
		context.setContextPath("/");
		this.server.setHandler(context);

		final ServletHolder servletHolder = context.addServlet(ServletContainer.class, pathSpec);
		return servletHolder;
	}

	public void join() throws InterruptedException {
		this.server.join();
	}
}
