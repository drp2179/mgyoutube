package com.djpedesen.mgyoutube.api_java.webservices;

import com.djpedesen.mgyoutube.api_java.modules.DefaultUserModuleImpl;
import com.djpedesen.mgyoutube.api_java.modules.UserModule;
import com.djpedesen.mgyoutube.api_java.repos.UserDataRepo;

public class ModuleRepoRegistry {
	private static UserModule theUserModule = null;
	private static UserDataRepo theUserDataRepo = null;

	public static UserModule getUserModule() {
		if (theUserModule == null) {
			theUserModule = new DefaultUserModuleImpl(getUserDataRepo());
		}
		return theUserModule;
	}

	public static void setUserModule(final UserModule userModule) {
		theUserModule = userModule;
	}

	public static UserDataRepo getUserDataRepo() {
		return theUserDataRepo;
	}

	public static void setUserDataRepo(final UserDataRepo userDataRepo) {
		theUserDataRepo = userDataRepo;
	}
}
