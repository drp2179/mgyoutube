// package com.djpedesen.mgyoutube.api_java.utils.h2;
//
// import java.sql.SQLException;
//
// import org.h2.tools.Server;
//
// public class H2ServerWrapper {
// public static final int DEFAULT_PORT = 8080;
//
// private final Server server;
// // private final int port;
//
// public H2ServerWrapper() throws SQLException {
// this(DEFAULT_PORT);
// }
//
// public H2ServerWrapper(final int port) throws SQLException {
// String args = null;
// // this.port = port;
// this.server = Server.createTcpServer(args);
// }
//
// public void start() throws Exception {
// // final ServerConnector connector = new ServerConnector(this.server);
// // connector.setPort(this.port);
// // this.server.setConnectors(new Connector[] { connector });
// this.server.start();
// }
//
// public void stop() throws Exception {
// this.server.stop();
// }
//
// }
