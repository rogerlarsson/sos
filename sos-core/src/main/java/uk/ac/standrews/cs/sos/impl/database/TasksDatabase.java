package uk.ac.standrews.cs.sos.impl.database;

import uk.ac.standrews.cs.sos.exceptions.db.DatabaseException;
import uk.ac.standrews.cs.sos.interfaces.database.Database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class TasksDatabase extends AbstractDatabase implements Database {

    private final static String SQL_CHECK_TASKS_TABLE_EXISTS = "SELECT name FROM sqlite_master WHERE type=\'table\' and name=\'tasks\'";
    private final static String SQL_CREATE_TASKS_TABLE = "CREATE TABLE `tasks` " +
            "(`DB_taskid`       INTEGER , " +
            "PRIMARY KEY (`DB_taskid`) )";

    public TasksDatabase(String path) throws DatabaseException {
        super(path);

        try (Connection connection = getSQLiteConnection()) {

            boolean tableExists = executeQuery(connection, SQL_CHECK_TASKS_TABLE_EXISTS);
            if (!tableExists) {
                executeUpdate(connection, SQL_CREATE_TASKS_TABLE);
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

}
