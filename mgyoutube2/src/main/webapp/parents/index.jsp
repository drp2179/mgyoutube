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
	<title>MG YouTube - Parents</title>
	<meta charset="UTF-8">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
	
	<script>
	
	var currentlySelectedChildAccount = null;
	
	function whenDeleteSavedSearchesSucceeds() {
		refreshSavedSearches(currentlySelectedChildAccount);
	}
	function whenDeleteSavedSearchesFails() {
		alerts("unable to delete saved search");
	}
	
	function deleteSavedSearch( id ) {
		var lookupUrl = "/ws/youtube/savedsearches/" + id;
		console.info("deleteUrl:" + lookupUrl);
		
		$.ajax({
			url: lookupUrl, 
			type: 'DELETE',
			success: function(data) {
				console.log("delete successful");
				whenDeleteSavedSearchesSucceeds();
			},
			error: function() {
				console.log("delete failed");
				whenDeleteSavedSearchesFails();
			}
		});
	}
	
	function whenRefreshSavedSearchesSucceeds( jsonSavedSearchesList ) {
		console.log("whenRefreshSavedSearchesSucceeds:" + jsonSavedSearchesList);
		var arrayLength = jsonSavedSearchesList.length;
		console.log("arrayLength:" + arrayLength);
		var savedSearchListHtml = "<div>";
		
		for (var i = 0; i < arrayLength; i++) {
			var savedSearch = jsonSavedSearchesList[i];
			savedSearchListHtml += "<div style='border:1px solid black;'>";
			savedSearchListHtml += "<div><img src='"+savedSearch.thumbnailUrl+"' style='width:100px;'/>";
			savedSearchListHtml += "<div>" + savedSearch.searchTerms + "</div>";
			savedSearchListHtml += "<div><a href='javascript:deleteSavedSearch(\""+savedSearch.id+"\");' style='font-size:small;'>delete</a></div>";
			savedSearchListHtml += "</div>";
		}
		
		savedSearchListHtml += "</div>";
		
		$("#savedSearchList").html( savedSearchListHtml );
	}
	
	function whenRefreshSavedSearchesFails() {
		alert("unable to refresh saved searches");	
	}

	function refreshSavedSearches(childAccount) {
		var lookupUrl = "/ws/youtube/savedsearches/child/" + childAccount;
		console.info("getUrl:" + lookupUrl);
		
		$.ajax({
			url: lookupUrl, 
			type: 'GET',
			success: function(data) {
				console.log("refresh successful");
				whenRefreshSavedSearchesSucceeds(data);
			},
			error: function() {
				console.log("refresh failed");
				whenRefreshSavedSearchesFails();
			}
		});
	}
	
	function refreshRecentlySaved() {
		
	}

	function whenRefreshRecentlyWatchedSucceeds(jsonRecentlyWatchedList) {
		console.log("whenRefreshRecentlyWatchedSucceeds:" + jsonRecentlyWatchedList);
		var arrayLength = jsonRecentlyWatchedList.length;
		console.log("arrayLength:" + arrayLength);
		var savedSearchListHtml = "<div>";
		
		for (var i = 0; i < arrayLength; i++) {
			var watched = jsonRecentlyWatchedList[i];
			savedSearchListHtml += "<div style='border:1px solid black;'>";
			savedSearchListHtml += "<div><img src='"+watched.thumbnailUrl+"' style='width:100px;'/>";
			savedSearchListHtml += "<div>" + watched.videoTitle + "</div>";
			savedSearchListHtml += "<div>" + watched.timestamp + "</div>";
			savedSearchListHtml += "<div><a href='javascript:blockVideo(\""+watched.videoId+"\");' style='font-size:small;'>block</a></div>";
			savedSearchListHtml += "</div>";
		}
		
		savedSearchListHtml += "</div>";
		
		$("#recentwatchlist").html( savedSearchListHtml );
	}
	
	function whenRefreshRecentlyWatchedFails() {
		alert("Unable to refresh recently watched");
	} 
	
	function refreshRecentlyWatched(childAccount) {
		console.log("refreshRecentlyWatched:" + childAccount);
		var lookupUrl = "/ws/youtube/watched/" + childAccount;
		console.info("getUrl:" + lookupUrl);
		
		$.ajax({
			url: lookupUrl, 
			type: 'GET',
			success: function(data) {
				console.log("refreshRecentlyWatched successful");
				whenRefreshRecentlyWatchedSucceeds(data);
			},
			error: function(data, errorStatus, thrownException) {
				console.log("refreshRecentlyWatched failed, " +errorStatus + ", " + thrownException);
				whenRefreshRecentlyWatchedFails();
			}
		});
	}
	
	function showChildAccountHistory(childAccount) {
		currentlySelectedChildAccount = childAccount;
		refreshSavedSearches(childAccount) ;
		refreshBlacklistedWords(); // no child account because its for the parents
		refreshRecentlySaved() ;
		refreshRecentlyWatched(childAccount) ;
	}
	
	function whenAssociateAccountSucceeds(data) {
		console.log("whenAssociateAccountSucceeds - refreshing associated accounts");
		refreshAssociatedAccounts();
	}
	
	function whenAssociateAccountFails() {
		alert("Unable to associate account.");
	}
	
	function doAssociateAccount( accountToAssociate, whenAccociateAccountSucceedsCallback, whenAccociateAccountFailsCallback) {
		console.log("doAssociateAccount:" + accountToAssociate);
		var lookupUrl = "/ws/accounts/associated/";
		console.info("putUrl:" + lookupUrl);
		
		var thePutData = { childAccount: accountToAssociate };

		$.ajax({
			url: lookupUrl, 
			type: 'PUT',
			data: thePutData,
			success: function(data) {
				whenAccociateAccountSucceedsCallback(data);
			},
			error: function() {
				whenAccociateAccountFailsCallback();
			}
		});
	}
	
	function associateNewAcountClicked() {
		var newAccountToAccociate = $("#newAccount").val();
		
		if ( newAccountToAccociate.length > 0 ) {
			doAssociateAccount(newAccountToAccociate, whenAssociateAccountSucceeds, whenAssociateAccountFails);
		}
		else{
			alert("no account provided to associate.") ;
		}
	}
	
	function whenRefreshAccociateAccountsSucceeds( jsonArrayOfAccounts) {
		console.log("whenRefreshAccociateAccountsSucceeds:" + jsonArrayOfAccounts);
		var arrayLength = jsonArrayOfAccounts.length;
		console.log("arrayLength:" + arrayLength);
		var associatedAccountsHtml = "<ul>";
		
		for (var i = 0; i < arrayLength; i++) {
			associatedAccountsHtml += "<li><a href='javascript:showChildAccountHistory(\"" + jsonArrayOfAccounts[i]+"\");'>" + jsonArrayOfAccounts[i] +"</a></li>";
		}
		
		associatedAccountsHtml += "</ul>";
		
		$("#accountlist").html( associatedAccountsHtml );
	}
	
	function whenRefreshAccociateAccountsFails() {
		alert("unable to refresh associated accounts");
	}
	
	function refreshAssociatedAccounts() {
		var lookupUrl = "/ws/accounts/associated/";
		console.info("getUrl:" + lookupUrl);
		
		$.ajax({
			url: lookupUrl, 
			type: 'GET',
			success: function(data) {
				console.log("refresh associated accounts successful");
				whenRefreshAccociateAccountsSucceeds(data);
			},
			error: function() {
				console.log("refresh failed");
				whenRefreshAccociateAccountsFails();
			}
		});
	}
	
	function whenDeleteBlacklistedWordSucceeds() {
		refreshBlacklistedWords();
	}
	function whenDeleteBlacklistedWordFails() {
		alerts("unable to delete saved search");
	}
	
	function deleteBlacklistedWord( wordToRemove ) {
		var lookupUrl = "/ws/blacklists/words/" + wordToRemove;
		console.info("deleteUrl:" + lookupUrl);
		
		$.ajax({
			url: lookupUrl, 
			type: 'DELETE',
			success: function(data) {
				console.log("delete blacklisted word successful");
				whenDeleteBlacklistedWordSucceeds();
			},
			error: function() {
				console.log("delete blacklisted word failed");
				whenDeleteBlacklistedWordFails();
			}
		});
	}
	
	function whenRefreshBlacklistedWordsSucceeds ( jsonBlacklistedWordList ) {
		console.log("whenRefreshBlacklistedWordsSucceeds:" + jsonBlacklistedWordList);
		var arrayLength = jsonBlacklistedWordList.length;
		console.log("arrayLength:" + arrayLength);
		var blacklistedWordsHtml = "<ul>";
		
		for (var i = 0; i < arrayLength; i++) {
			blacklistedWordsHtml += "<li>"+ jsonBlacklistedWordList[i] + "<a href='javascript:deleteBlacklistedWord(\"" + jsonBlacklistedWordList[i]+"\");'>(delete)</a></li>";
		}
		
		blacklistedWordsHtml += "</ul>";
		
		$("#blacklistedwordslist").html( blacklistedWordsHtml );
		$("#newBlacklistWord").val("");
	}
	
	function whenRefreshBlacklistedWordsFails () {
		alert("unable to refresh blacklisted words");
	}
	
	function refreshBlacklistedWords() {
		var lookupUrl = "/ws/blacklists/words/";
		console.info("getUrl:" + lookupUrl);
		
		$.ajax({
			url: lookupUrl, 
			type: 'GET',
			success: function(data) {
				console.log("refresh blacklist successful");
				whenRefreshBlacklistedWordsSucceeds(data);
			},
			error: function() {
				console.log("refresh blacklist failed");
				whenRefreshBlacklistedWordsFails();
			}
		});
	}
	
	function whenBlacklistWordSucceedsCallback( data ) {
		refreshBlacklistedWords();
	}
	
	function whenBlacklistWordFailsCallback() {
		alert("unable to blacklist the word");
	}
	
	function doBlacklistWord( wordToBlacklist ){
		var lookupUrl = "/ws/blacklists/words/"+wordToBlacklist;
		console.info("putUrl:" + lookupUrl);
		
//		var thePutData = { blackword: accountToAssociate };

		$.ajax({
			url: lookupUrl, 
			type: 'PUT',
//			data: thePutData,
			success: function(data) {
				whenBlacklistWordSucceedsCallback(data);
			},
			error: function() {
				whenBlacklistWordFailsCallback();
			}
		});
	}
	
	function blacklistNewWordClicked() {
		var newBlacklistedWord = $("#newBlacklistWord").val();
		
		if ( newBlacklistedWord.length > 0 ) {
			doBlacklistWord(newBlacklistedWord);
		}
		else{
			alert("no word provided to black list.") ;
		}
	}
	
	<c:if test="${isLoggedIn}">
	$( document ).ready(function() {
		refreshAssociatedAccounts();
	});	
	</c:if>
	</script>
	<style>
		body { background-color:white;}
	</style>
</head>
<body>
	<div id="banner" style="background-color:#f1f1f1;">
		<div>
			MG YouTube - For Parents
		</div>
		<div>
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
	<c:if test="${isLoggedIn}">
		<div id="associatedaccounts" style="width:35%;float:left;background-color:#f1f1f1;">
			<h2>Associated Accounts</h2>
			<div>
				<div><input type="text" id="newAccount" /></div>
				<div><button id="saveSearch" onClick="associateNewAcountClicked()">Associate New Account</button></div>
			</div>
			<div id="accountlist">
			</div>
		</div>
		
		<div id="accountdetails" style="width:62%;float:right;background-color:#f1f1f1;">
			<div id="recentlywatched">
				<h3>Recently Watched</h3>
				<div id="recentwatchlist">
				</div>
			</div>
			<div id="recentsearches">
				<h3>Recently Searched</h3>
				<div id="recentsearchlist">
				</div>
			</div>
			
			<div id="savedsearches">
				<h3>Saved Searches</h3>
				<div id="savedSearchList">
				</div>
			</div>

			<div id="blacklistedwords">
				<h3>Blacklisted Words</h3>
				<div>
					<div><input type="text" id="newBlacklistWord" /></div>
					<div><button id="blackListWordButton" onClick="blacklistNewWordClicked()">Add to Blacklist</button></div>
				</div>
				<div id="blacklistedwordslist">
				</div>
			</div>
		</div>
	</c:if>
</body>
</html>