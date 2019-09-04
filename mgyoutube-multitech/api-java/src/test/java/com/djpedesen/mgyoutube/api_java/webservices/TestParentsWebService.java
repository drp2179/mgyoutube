package com.djpedesen.mgyoutube.api_java.webservices;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.djpedesen.mgyoutube.api_java.apimodel.User;
import com.djpedesen.mgyoutube.api_java.apimodel.UserCredential;
import com.djpedesen.mgyoutube.api_java.modules.UserModule;

public class TestParentsWebService {

	private UserModule userModule;
	private ParentsWebService parentsWebService;

	@Before
	public void setup() {
		userModule = Mockito.mock(UserModule.class);
		ModuleRepoRegistry.setUserModule(userModule);

		parentsWebService = new ParentsWebService();
	}

	@Test
	public void createUserCredFromJsonGoodObject() {
		final String userCredentialJson = "{'username':'john', 'password':'doe'}";
		final UserCredential userCred = ParentsWebService.marshalUserCredentialFromJson(userCredentialJson);

		Assert.assertNotNull("json should parse to an object", userCred);
		Assert.assertEquals("username is wrong", "john", userCred.username);
		Assert.assertEquals("password is wrong", "doe", userCred.password);
	}

	@Test
	public void createUserCredFromJsonEmptyObject() {
		final String userCredentialJson = "{}";
		final UserCredential userCred = ParentsWebService.marshalUserCredentialFromJson(userCredentialJson);

		Assert.assertNull("json should not parse to an object", userCred);
	}

	@Test
	public void basicAuthParentUser() {
		final User user = new User();
		user.username = "dan";
		user.isParent = true;
		final String userCredentialJson = "{'username':'dan', 'password':'pass'}";
		Mockito.when(userModule.authUser(Mockito.any(UserCredential.class))).thenReturn(user);
		final Response response = parentsWebService.authParentUser(userCredentialJson);

		Assert.assertEquals(200, response.getStatus());
	}

}
