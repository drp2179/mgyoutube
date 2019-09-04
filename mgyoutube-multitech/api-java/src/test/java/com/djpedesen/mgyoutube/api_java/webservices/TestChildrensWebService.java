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
	public void createUserCredFromJsonGoodObject() {
		final String userCredentialJson = "{'username':'john', 'password':'doe'}";
		final UserCredential userCred = ChildrenWebService.marshalUserCredentialFromJson(userCredentialJson);

		Assert.assertNotNull("json should parse to an object", userCred);
		Assert.assertEquals("username is wrong", "john", userCred.username);
		Assert.assertEquals("password is wrong", "doe", userCred.password);
	}

	@Test
	public void createUserCredFromJsonEmptyObject() {
		final String userCredentialJson = "{}";
		final UserCredential userCred = ChildrenWebService.marshalUserCredentialFromJson(userCredentialJson);

		Assert.assertNull("json should not parse to an object", userCred);
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
