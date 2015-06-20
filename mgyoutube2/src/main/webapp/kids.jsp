<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<%
UserService userService= UserServiceFactory.getUserService();
User user = userService.getCurrentUser();
String loginUrl = userService.createLoginURL(request.getRequestURI()) ;
String logoutUrl = userService.createLogoutURL(request.getRequestURI()) ;

System.out.println("user:" + user) ;
if ( user != null ) {
	System.out.println("user.getEmail:" + user.getEmail() ) ;
	System.out.println("user.getUserId:" + user.getUserId() ) ;
	System.out.println("user.getAuthDomain:" + user.getAuthDomain() ) ;
	System.out.println("user.getNickname:" + user.getNickname() ) ;
}

pageContext.setAttribute("isLoggedIn", (user != null) );
pageContext.setAttribute("loginUrl", loginUrl);
pageContext.setAttribute("logoutUrl", logoutUrl);
%>
<!DOCTYPE html>
<html>
<head>
	<title>MG YouTube - Kids</title>
	<meta charset="UTF-8">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
	<script src="https://www.youtube.com/iframe_api"></script>
	
	<script>
	var player = null;
	var currentSearchWordsStr = null;
	var currentSearchResults = null;
	var savedSearches = [];
	
//	function showVideo( videoId ) {
//			var blockwidth = $("#videoblock").width();
//			var thewidth = Math.min( 854, blockwidth ) ;
//			var theheight =  510 * thewidth / 854 ;
//			
//			$('#videoblock iframe').remove();
//			//$('#videoblock').append("<iframe width='854' height='510' src='//www.youtube.com/embed/"+videoId+"?rel=0' frameborder='0' allowfullscreen></iframe>");
//			$('#videoblock').append("<iframe width='"+thewidth+"' height='"+theheight+"' src='https://www.youtube.com/embed/"+videoId+"?rel=0' frameborder='0' allowfullscreen></iframe>");
//		}
	function newshowVideo( videoId ) {
		$("#videoplayer").css("display", "block");

//		var blockwidth = $("#videoblock").width();
//			var thewidth = Math.min( 854, blockwidth ) ;
//			var theheight =  510 * thewidth / 854 ;
			var thewidth = 640 ;
			var theheight =  390;
			
		$('#videoplayer iframe').remove();
//		$('#videoplayer').append("<iframe width='"+thewidth+"' height='"+theheight+"' src='https://www.youtube.com/embed/"+videoId+"?rel=0' frameborder='0' allowfullscreen='1'></iframe>");
		$('#videoplayer').append("<iframe width='"+thewidth+"' height='"+theheight+"' src='https://www.youtube.com/embed/"+videoId+"' frameborder='0' allowfullscreen='1'></iframe>");
		
//		<iframe id="videoplayer" style="display: block;"                              src="https://www.youtube.com/embed/dTV1qoceV-U?"></iframe>
	}

	function showVideo( videoIndex ) {
		$("#videoplayer").css("display", "block");
		
		var video = currentSearchResults[videoIndex];
		
		//https://developers.google.com/youtube/iframe_api_reference
		if (player == null) {
			player = new YT.Player(
					'videoplayer', 
					{
						height: '390',
						width: '640',
//						videoId: theVideoId
						videoId: video.videoId
//						,
//						events: {
//							'onReady': onPlayerReady,
//							'onStateChange': onPlayerStateChange
//						}
					}
				);
		}
		else {
			player.loadVideoById(video.videoId);
		}
	}
	
	function whenSearchSucceeds(jsonVideoList) {
		$("#videoplayer").css("display", "none");

		currentSearchResults = jsonVideoList;
		var jsonMovieListStr = JSON.stringify(jsonVideoList);
		$("#searchresultsjson").html(jsonMovieListStr);
		$("#searchcontrols").css("display", "block");

		var arrayLength = jsonVideoList.length;
		var videoHtml = "";
		
		for (var i = 0; i < arrayLength; i++) {
			var video = jsonVideoList[i];
			var releasedDate = new Date( video.publishedAt.value ) ;
			theLink = "javascript:showVideo(\"" + i + "\")" ;
		
			videoHtml += "<div class='video'>";
			videoHtml += "<a href='"+theLink+"'><img class='poster' src='"+ video.thumbnailUrl +"'/></a>"
			//videoHtml += "<div class='videotitle'><a href='javascript:showVideo(\""+video.videoId+"\")'>" + video.title + "</a></div>";
			videoHtml += "<div class='videotitle'><a href='"+theLink+"'>" + video.title + "</a></div>";
			videoHtml += "<div class='videoby'>by " + video.channelTitle + "</div>";
			videoHtml += "<div class='videodate'>" + releasedDate.toDateString() + "</div>";
			videoHtml += "</div>";
		}
		
		$("#searchresults").html( videoHtml );
	}
	
	function whenSearchFails() {
		alert("Unable to get search results");
	}
	
	function onSearchClicked() {
		currentSearchWordsStr = $("#searchwords").val();
		
		if ( currentSearchWordsStr.length > 0 ) {
			console.info("searching:" + currentSearchWordsStr);
			
			doSearch(currentSearchWordsStr, whenSearchSucceeds, whenSearchFails);
		}
		else{
			alert("no text submitted for searching: " + currentSearchWordsStr );
		}
	}
	
	function savedSearchClicked(savedSearchWords) {
		currentSearchWordsStr = savedSearchWords;
		doSearch(currentSearchWordsStr, whenSearchSucceeds, whenSearchFails);
	}
	
	function doSearch( searchWords, whenSearchRequestIsSuccessful, whenSearchRequestFails ) {
		console.log("in doSearch");
		var lookupUrl = "/ws/youtube/search?terms=" + encodeURI(searchWords);
		console.info("lookupUrl:" + lookupUrl);

		//disableSearch();
		
		$.ajax({
			url: lookupUrl, 
			type: 'GET',
			success: function(data) {
				whenSearchRequestIsSuccessful( data ) ;
				//enableSearch();
			},
			error: function() {
				whenSearchRequestFails();
				//enableSearch();
			}
		});
	}
	
	function saveCurrentSearch() {
		var video = currentSearchResults[0];
		var aSearch = { searchTerms: currentSearchWordsStr, exampleVideo: video };
		savedSearches.push( aSearch );
		
		refreshSavedSearches();
	}
	function removeCurrentSearch() {
		// remove from the list
	
		refreshSavedSearches();
	}
	
	function refreshSavedSearches() {
		var arrayLength = savedSearches.length;
		var theHtml = "";
		
		for (var i = 0; i < arrayLength; i++) {
			aSearch = savedSearches[i];
			theLink = "javascript:savedSearchClicked(\"" +aSearch.searchTerms + "\")" ;
			
			theHtml += "<div class='savedsearch'>";
			theHtml += "<div class='searchterms'><a href='"+theLink+"'>"+ aSearch.searchTerms + "</a></div>";
			theHtml += "<a href='"+theLink+"'><img class='poster' src='"+ aSearch.exampleVideo.thumbnailUrl +"'/></a>";
			theHtml += "</div>";
		}

		$("#savedsearcheslist").html( theHtml );
	}
	</script>
	<style>
	/*
		#cc181e  red for key highlights
		#168ed6  blue for link text (underline on hover
		#7676a0  light grey for basic text
		#fff 	white for most backgrounds
		#f1f1f1  very light grey for internal box
		
		basic video box in a list
				video image to left, Title over "by" user over date "," views over truncated description
		basic video box on front page
				video image on top over Title over "by" user over views "bullet" date
	*/
		
		#searchresults { position:relative; overflow:scroll;}
		#searchresults .video { width:250px; float:left; height:300px;overflow:clipped; margin:1em;}
		#searchresults .video .poster { width:100%; }
		#searchcontrols {display:none;} 
		
		#searchresultsjson { clear:both; font-size:small; color:grey; display:none; }
		#videoplayer { display:none; width:640px; height:400px; position:relative; left:50%; margin-left:-320px;}
		
		#usernav { width: 20%; float:left;}
		#videos { width: 78%; float:left;}
	</style>
</head>
<body>
	<div id="banner">
		MG YouTube - Kids
		<div id="searchbox">
			<input type="text" id="searchwords"/>
			<input type="button" value="Search" onclick="onSearchClicked()"/>
		</div>
		<c:choose>
			<c:when test="${isLoggedIn}">
		    	Hello <% user.getUserId(); %> <a href='<c:out value="${logoutUrl}" />'>Logout</a>
			</c:when>
			<c:otherwise>
				<a href="<c:out value='${loginUrl}' />">Login</a>
			</c:otherwise>
		</c:choose>
	</div>
	<div id="usernav">
		<div id="savedsearches">
			<h3>Saved Searches</h3>
			<div id="savedsearcheslist"></div>
		</div>
		<div id="whitelistedchannels">
			<h3>Approved Channels</h3>
			<div id="whitelistedchannelslist"></div>
		</div>
	</div>
	<div id="videos">
		<div id="videoplayer"></div>
		<div id="searchcontrols">
			<button id="saveSearch" onClick="saveCurrentSearch()">Save</button>
			<!-- 
			<button id="removeSearch" onClick="removeCurrentSearch()">Remove</button>
			 -->
		</div>
		<div id="searchresults">
		</div>
	</div>
	<div id="searchresultsjson"></div>
</body>
</html>