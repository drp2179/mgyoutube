package com.djpedesen.mgyoutube.api_java.utils.jetty;

public class JettyShutdownRunnable implements Runnable {

	private final JettyServerWrapper server;

	public JettyShutdownRunnable(final JettyServerWrapper server) {
		this.server = server;
	}

	@Override
	public void run() {
		if (this.server != null) {
			try {
				this.server.stop();
			} catch (Throwable thrown) {
				// TODO log the failure here
				thrown.printStackTrace(System.out);
			}
		}
	}

}
