<!DOCTYPE html>
<html>
<head>
	<title>Test</title>
	<meta charset="UTF-8">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
	
	<script>
	
	function showVideo( videoId ) {
		window.scrollTo(0, 0);
		recordMovieShown( videoIndex ) ;

		$("#singlevideo").css("display", "block");

		var thewidth = 640;
		var theheight = 390;

		$('#videoplayer iframe').remove();
		$('#videoplayer').append("<iframe width='"+thewidth+"' height='"+theheight+"' src='https://www.youtube.com/embed/"+videoId+"?rel=0' frameborder='0' allowfullscreen></iframe>");
	}
	
	function oldShowVideo( videoIndex ) {
		window.scrollTo(0, 0);

		$("#singlevideo").css("display", "block");
		
		recordMovieShown( videoIndex ) ;
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
	
	</script>
	<style>
	</style>
</head>
<body>
	
	<div id="singlevideo" style="">
		<div id="videoplayer"></div>
	</div>
	<ul>
		<li><a href="javascript:showVideo('nNszif3eDTs');">Video 1</a></li>
		<li><a href="javascript:showVideo('CT3rJHs82A8');">Video 2</a></li>
		<li><a href="javascript:showVideo('C_Xy_pD_Or8');">Video 3</a></li>
	</ul>
	
	
</body>
</html>