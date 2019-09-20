package com.djpedesen.mgyoutube.api_java.repos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class H2SearchesDataRepoImpl implements SearchesDataRepo {

	public static final String SEARCHES_QUERY_PHRASES_BY_PARENT = "SELECT phrase FROM searches WHERE parentUserId = ?";
	public static final String SEARCHES_INSERT = "INSERT INTO searches (parentUserId, phrase) VALUES( $, $)";
	public static final String SEARCHES_DROP = "DROP TABLE IF EXISTS searches";
	public static final String SEARCHES_CREATE = "CREATE TABLE searches (parentUserId VARCHAR2(64), phrase VARCHAR2(256))";
	public static final String SEARCHES_DELETE_PHRASE_FOR_PARENT = "DELETE FROM searches WHERE parentUserId = ? AND phrase = ?";

	private final String connectionString;

	public H2SearchesDataRepoImpl(final String connectionString) throws ClassNotFoundException {
		Class.forName("org.h2.Driver");
		this.connectionString = connectionString;
	}

	protected Connection getConnection() throws SQLException {
		return DriverManager.getConnection(this.connectionString);
	}

	@Override
	public void repositoryStartup() throws Exception {
		try (final Connection connection = getConnection()) {
			final PreparedStatement dropStatement = connection.prepareStatement(SEARCHES_DROP);
			dropStatement.execute();

			final PreparedStatement createStatement = connection.prepareStatement(SEARCHES_CREATE);
			createStatement.execute();
		}
	}

	@Override
	public List<String> getSearchesForParentUser(final String parentUserId) throws Exception {
		final List<String> searches = new ArrayList<>();

		try (final Connection connection = getConnection()) {
			final PreparedStatement statement = connection.prepareStatement(SEARCHES_QUERY_PHRASES_BY_PARENT);

			statement.setString(1, parentUserId);

			try (final ResultSet resultSet = statement.executeQuery();) {
				while (resultSet.next()) {
					final String phrase = resultSet.getString(1);
					searches.add(phrase);
				}
			}
		}

		return searches;
	}

	@Override
	public void addSearchToParentUser(final String parentUserId, final String searchPhrase) throws Exception {
		try (final Connection connection = getConnection()) {
			final PreparedStatement statement = connection.prepareStatement(SEARCHES_INSERT);

			statement.setString(1, parentUserId);
			statement.setString(2, searchPhrase);

			statement.executeUpdate();
		}
	}

	@Override
	public void removeSearchFromParentUser(final String parentUserId, final String searchPhrase) throws Exception {
		try (final Connection connection = getConnection()) {
			final PreparedStatement statement = connection.prepareStatement(SEARCHES_DELETE_PHRASE_FOR_PARENT);

			statement.setString(1, parentUserId);
			statement.setString(2, searchPhrase);

			statement.executeUpdate();
		}
	}
}
