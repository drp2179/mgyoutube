package com.djpedesen.mgyoutube.api_java;

import org.eclipse.jetty.servlet.ServletHolder;

import com.djpedesen.mgyoutube.api_java.modules.DefaultVideoModuleImpl;
import com.djpedesen.mgyoutube.api_java.modules.VideoModule;
import com.djpedesen.mgyoutube.api_java.repos.SimplisticUserDataRepoImpl;
import com.djpedesen.mgyoutube.api_java.repos.UserDataRepo;
import com.djpedesen.mgyoutube.api_java.utils.jetty.JettyServerWrapper;
import com.djpedesen.mgyoutube.api_java.utils.jetty.JettyShutdownRunnable;
import com.djpedesen.mgyoutube.api_java.webservices.ModuleRepoRegistry;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class Main {

	public static void main(final String[] args) throws Exception {

		final YouTubeProperties youTubeProperties = new YouTubeProperties();

		final HttpTransport httpTransport = new ApacheHttpTransport();
		final JsonFactory jsonFactory = new JacksonFactory();
		final VideoModule videoModule = new DefaultVideoModuleImpl(youTubeProperties.apiKey,
				youTubeProperties.applicationName, httpTransport, jsonFactory);
		ModuleRepoRegistry.setVideoModule(videoModule);

		final UserDataRepo userDataRepo = new SimplisticUserDataRepoImpl();
		ModuleRepoRegistry.setUserDataRepo(userDataRepo);

		final JettyServerWrapper jettyServerWrapper = new JettyServerWrapper();
		final Runnable jettyShutdownRunner = new JettyShutdownRunnable(jettyServerWrapper);
		final Thread jettyShutdownThread = new Thread(jettyShutdownRunner);
		Runtime.getRuntime().addShutdownHook(jettyShutdownThread);

		final String urlPath = "/api/*";
		final ServletHolder servletHolder = jettyServerWrapper.addServletContext(urlPath);
		servletHolder.setInitOrder(0);
		servletHolder.setInitParameter("jersey.config.server.provider.packages",
				"com.djpedesen.mgyoutube.api_java.webservices");

		jettyServerWrapper.start();
		jettyServerWrapper.join();
	}
}
