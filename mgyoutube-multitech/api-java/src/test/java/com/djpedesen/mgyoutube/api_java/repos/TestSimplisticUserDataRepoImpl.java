package com.djpedesen.mgyoutube.api_java.repos;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.djpedesen.mgyoutube.api_java.apimodel.User;

public class TestSimplisticUserDataRepoImpl {

	private SimplisticUserDataRepoImpl repo;

	@Before
	public void setup() {
		repo = new SimplisticUserDataRepoImpl();
	}

	@Test
	public void unknownParentReturnsEmptyList() {
		final long parentUserId = 999;
		final List<User> childrenForParent = repo.getChildrenForParent(parentUserId);
		Assert.assertEquals(0, childrenForParent.size());
	}

	@Test
	public void canAddChildToParent() {
		final User parentSrc = new User();
		parentSrc.username = "parent";
		parentSrc.isParent = true;

		final User parentUser = repo.addUser(parentSrc);

		final User childSrc = new User();
		childSrc.username = "child";
		childSrc.isParent = false;

		final User childUser = repo.addUser(childSrc);

		repo.addChildToParent(parentUser.userId, childUser.userId);

		final List<User> childrenForParent = repo.getChildrenForParent(parentUser.userId);
		Assert.assertEquals(1, childrenForParent.size());
	}
}
