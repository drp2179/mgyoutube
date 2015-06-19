package com.djpedersen.mgyoutube.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class YouTubeProperties {

	public String apiKey;

	public YouTubeProperties() throws IOException {
		final InputStream resourceAsStream = YouTubeProperties.class.getClassLoader().getResourceAsStream(
				"secrets/youtube.properties");
		final Properties properties = new Properties();
		properties.load(resourceAsStream);

		apiKey = (String) properties.get("api.key");
	}
}
