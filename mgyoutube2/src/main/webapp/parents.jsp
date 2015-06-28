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
	
	function refreshRecentlySaved() {}
	function refreshRecentlyWatched() {}
	function refreshBlacklistedWords() {}
	
	function showChildAccountHistory(childAccount) {
		currentlySelectedChildAccount = childAccount;
		refreshSavedSearches(childAccount) ;
		refreshRecentlySaved() ;
		refreshRecentlyWatched() ;
		refreshBlacklistedWords();
	}
	
	function whenAssociateAccountSucceeds(data) {
		console.log("whenAssociateAccountSucceeds - refreshing associated accounts");
		refreshAssociatedAccounts();
	}
	
	function whenAssociateAccountFails() {
		alert("Unable to associate account.");
	}
	
	function doAssociateAccount( accountToAssociate, whenAccociateAccountSucceedsCallback, whenAccociateAccountFailsCallback) {
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
				console.log("refresh successful");
				whenRefreshAccociateAccountsSucceeds(data);
			},
			error: function() {
				console.log("refresh failed");
				whenRefreshAccociateAccountsFails();
			}
		});
	}
	
	<c:if test="${isLoggedIn}">
	$( document ).ready(function() {
		refreshAssociatedAccounts();
	});	
	</c:if>
	</script>
	<style>
	</style>
</head>
<body>
	
	<div id="banner">
		MG YouTube - For Parents
	</div>
	<c:choose>
		<c:when test="${isLoggedIn}">
			Hello <c:out value="${userEmail}" /> | <a href='<c:out value="${logoutUrl}" />'>Logout</a>
		</c:when>
		<c:otherwise>
			<a href="<c:out value='${loginUrl}' />">Login</a>
		</c:otherwise>
	</c:choose>
	<c:if test="${isLoggedIn}">
		<div id="associatedaccounts" style="width:35%;float:left;">
			<h2>Associated Accounts</h2>
			<div>
				<div><input type="text" id="newAccount" /></div>
				<div><button id="saveSearch" onClick="associateNewAcountClicked()">Associate New Account</button></div>
			</div>
			<div id="accountlist">
			</div>
		</div>
		
		<div id="accountdetails" style="width:62%;float:right;">
			<div id="recentlywatched">
				<h3>Recently Watched</h3>
				<ul>
					<li>video 1</li>
					<li>video 2</li>
				</ul>
			</div>
			<div id="recentsearches">
				<h3>Recently Searched</h3>
				<ul>
					<li>search 1</li>
					<li>search 2</li>
				</ul>
			</div>
			
			<div id="savedsearches">
				<h3>Saved Searches</h3>
				<div id="savedSearchList">
				</div>
			</div>
		</div>
	</c:if>
</body>
</html>