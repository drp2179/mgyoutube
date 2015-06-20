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
System.out.println("user.getEmail:" + user.getEmail() ) ;
System.out.println("user.getUserId:" + user.getUserId() ) ;
System.out.println("user.getAuthDomain:" + user.getAuthDomain() ) ;
System.out.println("user.getNickname:" + user.getNickname() ) ;

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
		MG YouTube - Parents
	</div>
	<c:choose>
		<c:when test="${isLoggedIn}">
	    	Hello <% user.getUserId(); %> <a href='<c:out value="${logoutUrl}" />'>Logout</a>
		</c:when>
		<c:otherwise>
			<a href="<c:out value='${loginUrl}' />">Login</a>
		</c:otherwise>
	</c:choose>
</body>
</html>