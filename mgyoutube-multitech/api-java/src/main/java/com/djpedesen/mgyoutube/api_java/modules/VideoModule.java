package com.djpedesen.mgyoutube.api_java.modules;

import java.io.IOException;
import java.util.List;

import com.djpedesen.mgyoutube.api_java.apimodel.Video;

public interface VideoModule {

	List<Video> search(String searchTerms) throws IOException;
}
