package com.djpedesen.mgyoutube.api_java.webservices;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.djpedesen.mgyoutube.api_java.apimodel.User;
import com.djpedesen.mgyoutube.api_java.apimodel.UserCredential;
import com.djpedesen.mgyoutube.api_java.modules.UserModule;

public class TestChildrensWebService {

	private UserModule authUserModule;
	private ChildrenWebService childrenWebService;

	@Before
	public void setup() {
		authUserModule = Mockito.mock(UserModule.class);
		ModuleRepoRegistry.setUserModule(authUserModule);

		childrenWebService = new ChildrenWebService();
	}

	@Test
	public void basicAuthChildUser() {
		final User user = new User();
		user.username = "dan";
		final String userCredentialJson = "{'username':'dan', 'password':'pass'}";
		Mockito.when(authUserModule.authUser(Mockito.any(UserCredential.class))).thenReturn(user);
		final Response response = childrenWebService.authChildUser(userCredentialJson);

		Assert.assertEquals(200, response.getStatus());
	}

}
