/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module core.
 *
 * core is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * core is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with core. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.sos.impl.database;

import uk.ac.standrews.cs.castore.interfaces.IFile;
import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.guid.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.exceptions.db.DatabaseConnectionException;
import uk.ac.standrews.cs.sos.exceptions.db.DatabaseException;
import uk.ac.standrews.cs.sos.impl.context.directory.ContextVersionInfo;
import uk.ac.standrews.cs.sos.interfaces.context.ContextsContentsDirectory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ContextsContentsDirectoryDatabase extends AbstractDatabase implements ContextsContentsDirectory {

    private static final String SQL_CHECK_CONTEXTS_TABLE_EXISTS = "SELECT name FROM sqlite_master WHERE type=\'table\' and name=\'contexts\'";
    private static final String SQL_CREATE_CONTEXTS_TABLE = "CREATE TABLE `contexts` " +
            "(`context_id`        VARCHAR , " +
            "`version_id`         VARCHAR , " +
            "`pred_result`        BOOLEAN NOT NULL , " +
            "`timestamp`          INTEGER NOT NULL , " + // will be a long
            "`policy_satisfied`   BOOLEAN NOT NULL , " +
            "`evict`              BOOLEAN DEFAULT 0 , " + // FALSE
            "PRIMARY KEY (`context_id`, `version_id`) )";


    private static final String SQL_ADD_ENTRY = "INSERT OR REPLACE INTO contexts " +
            "(context_id, version_id, pred_result, timestamp, policy_satisfied) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_GET_ENTRIES = "SELECT version_id, pred_result, timestamp, policy_satisfied " +
            "FROM contexts";

    private static final String SQL_GET_NOT_EVICTED_ENTRIES = "SELECT version_id, pred_result, timestamp, policy_satisfied " +
            "FROM contexts WHERE evict == 0";

    private static final String SQL_GET_ENTRY = "SELECT pred_result, timestamp, policy_satisfied " +
            "FROM contexts WHERE context_id=? and version_id=?";

    private static final String SQL_EVICT_ENTRY = "UPDATE contexts SET evict=1 WHERE context_id=? version_id=?";

    private static final String SQL_DELETE_ENTRY = "DELETE from contexts WHERE context_id=? version_id=?";

    private static final String SQL_DELETE_ENTRIES = "DELETE from contexts WHERE context_id=?";

    ContextsContentsDirectoryDatabase(IFile dbFile) throws DatabaseException {
        super(dbFile);

        try (Connection connection = getSQLiteConnection()) {

            boolean tableExists = executeQuery(connection, SQL_CHECK_CONTEXTS_TABLE_EXISTS);
            if (!tableExists) {
                executeUpdate(connection, SQL_CREATE_CONTEXTS_TABLE);
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void addOrUpdateEntry(IGUID contextInvariant, IGUID version, ContextVersionInfo contextVersionInfo) {

        try (Connection connection = getSQLiteConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_ADD_ENTRY)) {

            preparedStatement.setString(1, contextInvariant.toMultiHash());
            preparedStatement.setString(2, version.toMultiHash());
            preparedStatement.setBoolean(3, contextVersionInfo.predicateResult);
            preparedStatement.setLong(4, contextVersionInfo.timestamp.getEpochSecond());
            preparedStatement.setBoolean(5, contextVersionInfo.policySatisfied);

            preparedStatement.execute();

        } catch (SQLException | DatabaseConnectionException ignored) { }
    }

    @Override
    public void evict(IGUID context, IGUID version) {

        try (Connection connection = getSQLiteConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_EVICT_ENTRY)) {

            preparedStatement.setString(1, context.toMultiHash());
            preparedStatement.setString(2, version.toMultiHash());

            preparedStatement.executeUpdate();

        } catch (SQLException | DatabaseConnectionException ignored) { }
    }

    @Override
    public void delete(IGUID context, IGUID version) {

        try (Connection connection = getSQLiteConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_ENTRY)) {

            preparedStatement.setString(1, context.toMultiHash());
            preparedStatement.setString(2, version.toMultiHash());

            preparedStatement.executeUpdate();

        } catch (SQLException | DatabaseConnectionException ignored) { }
    }

    @Override
    public void delete(IGUID context) {

        try (Connection connection = getSQLiteConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_ENTRIES)) {

            preparedStatement.setString(1, context.toMultiHash());

            preparedStatement.executeUpdate();

        } catch (SQLException | DatabaseConnectionException ignored) { }
    }

    @Override
    public ContextVersionInfo getEntry(IGUID context, IGUID version) {

        try (Connection connection = getSQLiteConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_ENTRY)) {

            preparedStatement.setString(1, context.toMultiHash());
            preparedStatement.setString(2, version.toMultiHash());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                    ContextVersionInfo contextVersionInfo = new ContextVersionInfo();
                    contextVersionInfo.predicateResult = resultSet.getBoolean(1);
                    contextVersionInfo.timestamp = Instant.ofEpochSecond(resultSet.getLong(2));
                    contextVersionInfo.policySatisfied = resultSet.getBoolean(3);

                    return contextVersionInfo;

                }
            }

        } catch (SQLException | DatabaseConnectionException ignored) { }

        return new ContextVersionInfo();
    }

    @Override
    public boolean entryExists(IGUID context, IGUID version) {

        try (Connection connection = getSQLiteConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_ENTRY)) {

            preparedStatement.setString(1, context.toMultiHash());
            preparedStatement.setString(2, version.toMultiHash());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException | DatabaseConnectionException e) {

            return false;
        }

    }

    @Override
    public Map<IGUID, ContextVersionInfo> getContentsThatPassedPredicateTestRows(IGUID context, boolean includeEvicted) {

        HashMap<IGUID, ContextVersionInfo> contents = new LinkedHashMap<>();

        try (Connection connection = getSQLiteConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(includeEvicted ? SQL_GET_ENTRIES : SQL_GET_NOT_EVICTED_ENTRIES)) {

            if (!includeEvicted) preparedStatement.setString(1, context.toMultiHash());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {

                    try {
                        IGUID version = GUIDFactory.recreateGUID(resultSet.getString(1));
                        ContextVersionInfo contextVersionInfo = new ContextVersionInfo();
                        contextVersionInfo.predicateResult = resultSet.getBoolean(2);
                        contextVersionInfo.timestamp = Instant.ofEpochSecond(resultSet.getLong(3));
                        contextVersionInfo.policySatisfied = resultSet.getBoolean(4);

                        contents.put(version, contextVersionInfo);

                    } catch (GUIDGenerationException ignored) { /* SKIP - DO NOTHING */ }

                }
            }

            return contents;

        } catch (SQLException | DatabaseConnectionException ignored) { }

        return contents;

    }

    @Override
    public void clear() {
        // DO NOTHING
    }

}
