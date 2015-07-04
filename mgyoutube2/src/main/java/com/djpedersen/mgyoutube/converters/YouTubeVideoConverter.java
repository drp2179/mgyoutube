package com.djpedersen.mgyoutube.converters;

import java.util.List;

import com.djpedersen.mgyoutube.entities.YouTubeVideo;
import com.google.gson.Gson;

public class YouTubeVideoConverter {
	private static final Gson gson = new Gson();

	public String youTubeVideoToJson(final YouTubeVideo video) {
		final String json = gson.toJson(video);
		return json;
	}

	public String youTubeVideoListToJson(final List<YouTubeVideo> listOfVideos) {
		final StringBuffer sb = new StringBuffer();

		sb.append("[");

		if (listOfVideos != null) {
			// final Gson gson = getGsonWithDateHandling();
			boolean firstTimeThrough = true;
			for (YouTubeVideo video : listOfVideos) {
				if (!firstTimeThrough) {
					sb.append(",");
				}

				final String json = youTubeVideoToJson(video);
				sb.append(json);
				firstTimeThrough = false;
			}
		}

		sb.append("]");

		return sb.toString();
	}
}
