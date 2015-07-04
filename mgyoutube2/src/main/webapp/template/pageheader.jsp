<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
