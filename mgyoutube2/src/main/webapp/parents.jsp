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
		<div id="associatedaccounts">
		<h2>Associated Accounts</h2>
		<a href="">Associate New Account...</a>
			<ul>
				<li><a href="">abc@xyz.com</a></li>
			</ul>
		</div>
		
		<div id="accountdetails">
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
				<ul>
					<li>search terms 1</li>
					<li>search terms 2</li>
				</ul>
			</div>
		</div>
	</c:if>
</body>
</html>