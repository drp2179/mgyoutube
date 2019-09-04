package com.djpedesen.mgyoutube.api_java;

import org.eclipse.jetty.servlet.ServletHolder;

import com.djpedesen.mgyoutube.api_java.repos.SimplisticUserDataRepoImpl;
import com.djpedesen.mgyoutube.api_java.utils.jetty.JettyServerWrapper;
import com.djpedesen.mgyoutube.api_java.utils.jetty.JettyShutdownRunnable;
import com.djpedesen.mgyoutube.api_java.webservices.ModuleRepoRegistry;

public class Main {

	public static void main(final String[] args) throws Exception {

		ModuleRepoRegistry.setUserDataRepo(new SimplisticUserDataRepoImpl());

		// final H2ServerWrapper h2ServerWrapper = new H2ServerWrapper();
		// final Runnable h2ShutdownRunner = new H2ShutdownRunnable(h2ServerWrapper);
		// final Thread h2ShutdownThread = new Thread(h2ShutdownRunner);
		// Runtime.getRuntime().addShutdownHook(h2ShutdownThread);
		//
		final JettyServerWrapper jettyServerWrapper = new JettyServerWrapper();
		final Runnable jettyShutdownRunner = new JettyShutdownRunnable(jettyServerWrapper);
		final Thread jettyShutdownThread = new Thread(jettyShutdownRunner);
		Runtime.getRuntime().addShutdownHook(jettyShutdownThread);

		final String urlPath = "/api/*";
		final ServletHolder servletHolder = jettyServerWrapper.addServletContext(urlPath);
		servletHolder.setInitOrder(0);
		servletHolder.setInitParameter("jersey.config.server.provider.packages",
				"com.djpedesen.mgyoutube.api_java.webservices");

		// h2ServerWrapper.start();
		jettyServerWrapper.start();
		jettyServerWrapper.join();
	}
}
