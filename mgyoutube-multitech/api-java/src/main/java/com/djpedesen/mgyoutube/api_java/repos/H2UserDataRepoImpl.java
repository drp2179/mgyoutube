package com.djpedesen.mgyoutube.api_java.repos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.djpedesen.mgyoutube.api_java.apimodel.User;

public class H2UserDataRepoImpl implements UserDataRepo {

	public static final String USERS_DROP = "DROP TABLE IF EXISTS users";
	public static final String USERS_CREATE = "CREATE TABLE users (userid IDENTITY NOT NULL PRIMARY KEY, username VARCHAR2(128), password VARCHAR2(62), isparent BOOLEAN)";
	public static final String USERS_QUERY_BY_USERNAME = "SELECT * FROM users WHERE username = ?";
	public static final String USERS_QUERY_BY_ID = "SELECT * FROM users WHERE userid = ?";
	public static final String USERS_INSERT = "INSERT INTO users (username, password, isparent) VALUES (?,?,?)";
	public static final String USERS_DELETE_BY_USERID = "DELETE FROM users WHERE userid = ?";
	public static final String USERS_UPDATE = "UPDATE users SET username = ?, password = ?, isparent = ? WHERE userid = ?";

	public static final String PARENT_CHILDREN_DROP = "DROP TABLE IF EXISTS parentchildren";
	public static final String PARENT_CHILD_CREATE = "CREATE TABLE parentchildren (parentUserId VARCHAR2(128), childUserId VARCHAR2(128))";
	public static final String PARENT_CHILDREN_INSERT = "INSERT INTO parentchildren (parentUserId, childUserId) VALUES (?,?)";
	public static final String PARENT_CHILDREN_QUERY_BY_PARENT = "SELECT childUserId FROM parentchildren WHERE parentUserId = ?";

	private final String connectionString;

	public H2UserDataRepoImpl(final String connectionString) throws ClassNotFoundException {
		Class.forName("org.h2.Driver");
		this.connectionString = connectionString;
	}

	protected Connection getConnection() throws SQLException {
		return DriverManager.getConnection(connectionString);
	}

	@Override
	public void repositoryStartup() throws Exception {
		try (final Connection connection = getConnection()) {
			final PreparedStatement userDropStatement = connection.prepareStatement(USERS_DROP);
			userDropStatement.execute();

			final PreparedStatement parentChildrenDropStatement = connection.prepareStatement(PARENT_CHILDREN_DROP);
			parentChildrenDropStatement.execute();

			final PreparedStatement usersCreateStatement = connection.prepareStatement(USERS_CREATE);
			usersCreateStatement.execute();

			final PreparedStatement parentChildCreateStatement = connection.prepareStatement(PARENT_CHILD_CREATE);
			parentChildCreateStatement.execute();
		}
	}

	@Override
	public User getUserByUsername(final String username) throws Exception {

		try (final Connection connection = getConnection()) {
			return getUserByUsername(connection, username);
		}
	}

	protected User getUserByUsername(final Connection connection, final String username) throws Exception {

		final PreparedStatement statement = connection.prepareStatement(USERS_QUERY_BY_USERNAME);

		statement.setString(1, username);

		try (final ResultSet resultSet = statement.executeQuery();) {
			if (resultSet.next()) {
				final User user = createUserFromResultSet(resultSet);
				return user;
			}
			return null;
		}
	}

	protected User getUserById(final Connection connection, final String userId) throws Exception {

		final PreparedStatement statement = connection.prepareStatement(USERS_QUERY_BY_ID);

		statement.setString(1, userId);

		try (final ResultSet resultSet = statement.executeQuery();) {
			if (resultSet.next()) {
				final User user = createUserFromResultSet(resultSet);
				return user;
			}
			return null;
		}
	}

	@Override
	public User addUser(final User user) throws Exception {
		try (final Connection connection = getConnection()) {
			final PreparedStatement statement = connection.prepareStatement(USERS_INSERT);

			statement.setString(1, user.username);
			statement.setString(2, user.password);
			statement.setBoolean(3, user.isParent);

			statement.executeUpdate();

			return this.getUserByUsername(connection, user.username);
		}
	}

	@Override
	public void removeUser(final User user) throws Exception {
		try (final Connection connection = getConnection()) {
			final PreparedStatement statement = connection.prepareStatement(USERS_DELETE_BY_USERID);

			statement.setString(1, user.userId);

			statement.executeUpdate();
		}
	}

	@Override
	public User replaceUser(final String userId, final User user) throws Exception {
		try (final Connection connection = getConnection()) {
			final PreparedStatement statement = connection.prepareStatement(USERS_UPDATE);

			statement.setString(1, user.username);
			statement.setString(2, user.password);
			statement.setBoolean(3, user.isParent);
			statement.setString(4, user.userId);

			statement.executeUpdate();

			return this.getUserByUsername(connection, user.username);
		}
	}

	@Override
	public void addChildToParent(final String parentUserId, final String childUserId) throws Exception {
		try (final Connection connection = getConnection()) {
			final PreparedStatement statement = connection.prepareStatement(PARENT_CHILDREN_INSERT);

			statement.setString(1, parentUserId);
			statement.setString(2, childUserId);

			statement.executeUpdate();
		}
	}

	@Override
	public List<User> getChildrenForParent(final String parentUserId) throws Exception {
		final List<User> children = new ArrayList<>();
		try (final Connection connection = getConnection()) {
			final PreparedStatement statement = connection.prepareStatement(PARENT_CHILDREN_QUERY_BY_PARENT);

			statement.setString(1, parentUserId);

			try (final ResultSet resultSet = statement.executeQuery();) {
				while (resultSet.next()) {
					final String childUserId = resultSet.getString(1);
					final User childUser = this.getUserById(connection, childUserId);
					children.add(childUser);
				}
			}

		}
		return children;
	}

	private User createUserFromResultSet(final ResultSet resultSet) throws SQLException {
		final User user = new User();
		user.userId = resultSet.getString("userid");
		user.username = resultSet.getString("username");
		user.password = resultSet.getString("password");
		user.isParent = resultSet.getBoolean("isparent");
		return user;
	}

}
