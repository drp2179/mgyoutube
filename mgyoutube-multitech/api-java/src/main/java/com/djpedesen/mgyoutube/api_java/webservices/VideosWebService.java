package com.djpedesen.mgyoutube.api_java.webservices;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.djpedesen.mgyoutube.api_java.apimodel.Video;
import com.djpedesen.mgyoutube.api_java.modules.VideoModule;
import com.google.gson.Gson;

@Path("/videos")
public class VideosWebService {
	private static final Gson GSON = new Gson();

	private VideoModule videosModule;

	public VideosWebService() {
		System.out.println("creating VideosWebService");
		this.videosModule = ModuleRepoRegistry.getVideoModule();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response videosSearch(@QueryParam("search") final String searchTerms) throws IOException {
		System.out.println("videosSearch: searchTerms='" + searchTerms + "'");

		if (StringUtils.isBlank(searchTerms)) {
			System.out.println("search term are blank failing as 400");
			return Response.status(400).build();
		}

		// TODO: need to sanitize payload input before using
		final String sanitizedSearchTerms = searchTerms;

		final List<Video> videos = videosModule.search(sanitizedSearchTerms);
		System.out.println("videosModule.search: videos=" + videos);

		final String responseJson = GSON.toJson(videos);

		System.out.println("videosSearch: searchTerms=" + sanitizedSearchTerms + " returning OK, " + responseJson);
		return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
	}

}
