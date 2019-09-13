package com.djpedesen.mgyoutube.api_java.webservices;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.djpedesen.mgyoutube.api_java.apimodel.UserCredential;

public class TestHelpers {

	@Before
	public void setup() {
	}

	@Test
	public void createUserCredFromJsonGoodObject() {
		final String userCredentialJson = "{'username':'john', 'password':'doe'}";
		final UserCredential userCred = Helpers.marshalUserCredentialFromJson(userCredentialJson);

		Assert.assertNotNull("json should parse to an object", userCred);
		Assert.assertEquals("username is wrong", "john", userCred.username);
		Assert.assertEquals("password is wrong", "doe", userCred.password);
	}

	@Test
	public void createUserCredFromJsonEmptyObject() {
		final String userCredentialJson = "{}";
		final UserCredential userCred = Helpers.marshalUserCredentialFromJson(userCredentialJson);

		Assert.assertNull("json should not parse to an object", userCred);
	}
}
