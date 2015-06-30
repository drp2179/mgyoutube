<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<%
final UserService userService= UserServiceFactory.getUserService();
final User user = userService.getCurrentUser();
final String loginUrl = userService.createLoginURL(request.getRequestURI()) ;
final String logoutUrl = userService.createLogoutURL(request.getRequestURI()) ;

if ( user != null ) {
//	System.out.println("user.getEmail:" + user.getEmail() ) ;
//	System.out.println("user.getUserId:" + user.getUserId() ) ;
//	System.out.println("user.getAuthDomain:" + user.getAuthDomain() ) ;
//	System.out.println("user.getNickname:" + user.getNickname() ) ;

	pageContext.setAttribute("user", user);
	pageContext.setAttribute("userEmail", user.getEmail());
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
	var videosCollapsedWidth = 300 ;
	var userNavWidth = 200 ;
	var browserPadding = 5*2 ;
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
		var thewidth = 640;
		var theheight = 390;
			
		$('#videoplayer iframe').remove();
//		$('#videoplayer').append("<iframe width='"+thewidth+"' height='"+theheight+"' src='https://www.youtube.com/embed/"+videoId+"?rel=0' frameborder='0' allowfullscreen='1'></iframe>");
		$('#videoplayer').append("<iframe width='"+thewidth+"' height='"+theheight+"' src='//www.youtube.com/embed/"+videoId+"?rel=0?&autoplay=0' frameborder='0' allowfullscreen='1'></iframe>");
		
//		<iframe id="videoplayer" style="display: block;"                              src="https://www.youtube.com/embed/dTV1qoceV-U?"></iframe>
	}

	function showVideo( videoIndex ) {
		var viewportWidth = document.documentElement.clientWidth;
		var videoWidth = viewportWidth - userNavWidth - videosCollapsedWidth - browserPadding -10;

		$("#singlevideo").css("display", "block");
		$("#singlevideo").width(videoWidth).css("left","200px");
		//$("#videoplayer").css("left","50%").css("margin-left", (videoWidth*-1/2));
		$("#videoplayer").css("left", (videoWidth - 640)/2);
		$("#videos").width(videosCollapsedWidth);
		
		var video = currentSearchResults[videoIndex];
		
		//https://developers.google.com/youtube/iframe_api_reference
		if (player == null) {
			player = new YT.Player(
					'videoplayer', 
					{
						height: '390',
						width: '640',
						rel: '0',
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
			//player.loadVideoById(video.videoId);
			player.cueVideoById(video.videoId);
		}
	}
	
	function whenSearchSucceeds(jsonVideoList) {
		var viewportWidth = document.documentElement.clientWidth;
		var videosWidth = viewportWidth - userNavWidth - browserPadding ;
		
		$("#singlevideo").css("display", "none");
		$("#videos").width( videosWidth );

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
			/*videoHtml += "<div class='videotitle'><a href='javascript:showVideo(\""+video.videoId+"\")'>" + video.title + "</a></div>";*/
			videoHtml += "<div class='videotitle'><a href='"+theLink+"'>" + video.title + "</a></div>";
			videoHtml += "<div class='videoby'>by " + video.channelTitle + "</div>";
			videoHtml += "<div class='videodate'>" + releasedDate.toDateString() + "</div>";
			videoHtml += "</div>";
		}
		
		$("#searchresults").html( videoHtml );
		$("#searchwords").val("");
	}
	
	function whenSearchFails() {
		alert("Unable to get search results");
	}
	
	function onSearchClicked() {
		currentSearchWordsStr = $("#searchwords").val();
		
		if ( currentSearchWordsStr.length > 0 ) {
			console.info("searching:" + currentSearchWordsStr);
			
			doSearch(currentSearchWordsStr, whenSearchSucceeds, whenSearchFails, true);
		}
		else{
			alert("no text submitted for searching: " + currentSearchWordsStr );
		}
	}
	
	function savedSearchClicked(savedSearchWords) {
		currentSearchWordsStr = savedSearchWords;
		doSearch(currentSearchWordsStr, whenSearchSucceeds, whenSearchFails, false);
	}
	
	function doSearch( searchWords, whenSearchRequestIsSuccessful, whenSearchRequestFails, allowSearchSave ) {
		console.log("in doSearch");
		var lookupUrl = "/ws/youtube/search?terms=" + encodeURI(searchWords);
		console.info("lookupUrl:" + lookupUrl);
		
		if ( allowSearchSave ) {
			$("#saveSearch").show();
		}
		else {
			$("#saveSearch").hide();
		}

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

		var lookupUrl = "/ws/youtube/search/save";
		console.info("lookupUrl:" + lookupUrl);
		
		var thePostData = { searchTerms: currentSearchWordsStr, thumbnailUrl: video.thumbnailUrl};

		$.ajax({
			url: lookupUrl, 
			type: 'POST',
			data: thePostData,
			success: function(data) {
				refreshSavedSearches();
			},
			error: function() {
				alert("Unable to save search");
			}
		});
	}
	function removeCurrentSearch() {
		// 
		// TODO: remove from the list
		//
	
		refreshSavedSearches();
	}
	
	function whenSavedSearchRequestIsSuccessful(savedSearchJsonList) {
		var arrayLength = savedSearchJsonList.length;
		var theHtml = "";

		for (var i = 0; i < arrayLength; i++) {
			aSearch = savedSearchJsonList[i];

			theLink = "javascript:savedSearchClicked(\"" +aSearch.searchTerms + "\")" ;
			
			theHtml += "<div class='savedsearch'>";
			theHtml += "<div class='searchterms'><a href='"+theLink+"'>"+ aSearch.searchTerms + "</a></div>";
			theHtml += "<a href='"+theLink+"'><img class='poster' src='"+ aSearch.thumbnailUrl +"'/></a>";
			theHtml += "</div>";
		}

		$("#savedsearcheslist").html( theHtml );
	}
	
	function whenSavedSearchRequestFails() {
		alert("Unable to fetch saved searches");
	}
	
	function refreshSavedSearches() {
		console.log("in refreshSavedSearches");
		var lookupUrl = "/ws/youtube/savedsearches";
		console.info("lookupUrl:" + lookupUrl);

		$.ajax({
			url: lookupUrl, 
			type: 'GET',
			success: function(data) {
				whenSavedSearchRequestIsSuccessful( data ) ;
			},
			error: function() {
				whenSavedSearchRequestFails();
			}
		});
	}

	function adjustViewport() {
		//var bannerHeight = $("#banner").height();
		//var viewportHeight = document.documentElement.clientHeight;
		//$("#videos").height(viewportHeight-bannerHeight-browserPadding);
	}
	//$("#searchwords").keyup(function (e) {
	//	console.info("keyCode:" + e.keyCode);
	//   if (e.keyCode == 13) {
	//    	onSearchClicked();
	//    }
	//});
	
	<c:if test="${isLoggedIn}">
		$( document ).ready(function() {
			adjustViewport();
			refreshSavedSearches();
		});	
	</c:if>
	<c:if test="not ${isLoggedIn}">
	$( document ).ready(function() {
		adjustViewport();
	});	
</c:if>
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
		body { margin:0;padding:5px; background-color:#7676a0}
		#searchresults { position:relative;}
		#searchresults .video { float:left; height:300px;width:300px;overflow:clipped; margin:5px;}
		#searchresults .video .poster { width:290px; }
		#searchcontrols {display:none;} 
		
		#searchresultsjson { clear:both; display:none; }
		
		#banner {background-color:white;padding:5px;margin:-5px;margin-bottom:0;}
		
		/*
		#videoplayer { display:none; width:640px; height:400px; position:relative; left:50%; margin-left:-320px;}
		*/
	</style>
</head>
<body>
	<div id="banner">
		MG YouTube - For Kids
		<div id="searchbox">
			<input type="text" id="searchwords"/>
			<input type="button" value="Search" onclick="onSearchClicked()"/>
		</div>
		<div style="text-align:right;font-size:small;">
		<c:choose>
			<c:when test="${isLoggedIn}">
		    	Hello <c:out value="${userEmail}" /> | <a href='<c:out value="${logoutUrl}" />'>Logout</a>
			</c:when>
			<c:otherwise>
				<a href="<c:out value='${loginUrl}' />">Login</a>
			</c:otherwise>
		</c:choose>
		</div>
	</div>
	<div id="maincontent" style="position:relative;padding-left:200px;">
		<div id="usernav" style="position:relative;left:-200px;width:200px;background-color:white;padding-left:5px;padding-right:5px;">
			<div id="savedsearches">
				<h3>Saved Searches</h3>
				<div id="savedsearcheslist"></div>
			</div>
			<div id="whitelistedchannels">
				<h3>Approved Channels</h3>
				<div id="whitelistedchannelslist"></div>
			</div>
			<div id="personalplaylists">
				<h3>Personal Playlists</h3>
				<div id="personalplaylistslist"></div>
			</div>
		</div>
		<div id="singlevideo" style="position:absolute;top:0;">
			<div id="videoplayer"></div>
		</div>
		<div id="videos" style="position:absolute;top:0;right:0;background-color:white;">
			<div id="searchcontrols">
				<button id="saveSearch" onClick="saveCurrentSearch()">Save Search</button>
				<!-- 
				<button id="removeSearch" onClick="removeCurrentSearch()">Remove</button>
				 -->
			</div>
			<div id="searchresults">
			</div>
		</div>
	</div>
	<div id="searchresultsjson"></div>
</body>
</html>