package com.djpedesen.mgyoutube.api_java;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatastoresProperties {
	public String couchConnectionString;
	public String couchUsername;
	public String couchPassword;
	public String h2ConnectionString;
	public String mongoConnectionString;
	public String mongoDatabaseName;

	public DatastoresProperties() throws IOException {
		final InputStream resourceAsStream = DatastoresProperties.class.getClassLoader()
				.getResourceAsStream("secrets/datastores.properties");
		final Properties properties = new Properties();
		properties.load(resourceAsStream);

		this.couchConnectionString = (String) properties.get("couchbase.connectionstring");
		this.couchUsername = (String) properties.get("couchbase.username");
		this.couchPassword = (String) properties.get("couchbase.password");

		this.h2ConnectionString = (String) properties.get("h2.connectionstring");

		this.mongoConnectionString = (String) properties.get("mongodb.connectionstring");
		this.mongoDatabaseName = (String) properties.get("mongodb.databasename");
	}
}
