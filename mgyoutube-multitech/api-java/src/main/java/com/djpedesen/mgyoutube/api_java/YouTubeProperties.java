package com.djpedesen.mgyoutube.api_java;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class YouTubeProperties {
	public String apiKey;
	public String applicationName;

	public YouTubeProperties() throws IOException {
		final InputStream resourceAsStream = YouTubeProperties.class.getClassLoader()
				.getResourceAsStream("secrets/youtube.properties");
		final Properties properties = new Properties();
		properties.load(resourceAsStream);

		this.apiKey = (String) properties.get("api.key");
		this.applicationName = (String) properties.get("api.applicationname");
	}
}
