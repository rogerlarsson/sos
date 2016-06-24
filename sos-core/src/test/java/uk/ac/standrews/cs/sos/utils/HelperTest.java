package uk.ac.standrews.cs.sos.utils;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import uk.ac.standrews.cs.sos.exceptions.db.DatabasePersistenceException;
import uk.ac.standrews.cs.sos.interfaces.locations.Location;
import uk.ac.standrews.cs.sos.interfaces.storage.SOSDirectory;
import uk.ac.standrews.cs.sos.interfaces.storage.SOSFile;
import uk.ac.standrews.cs.sos.model.Configuration;
import uk.ac.standrews.cs.sos.model.locations.URILocation;
import uk.ac.standrews.cs.sos.model.storage.FileBased.FileBasedFile;
import uk.ac.standrews.cs.sos.node.Config;
import uk.ac.standrews.cs.sos.node.SQLDB;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class HelperTest {

    public static InputStream StringToInputStream(String input) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    }

    private static String localURItoPath(Location location) throws URISyntaxException {
        return location.getURI().getPath();
    }

    public static Location createDummyDataFile(Configuration configuration) throws FileNotFoundException, URISyntaxException {
        return createDummyDataFile(configuration, "testData.txt");
    }

    public static Location createDummyDataFile(Configuration configuration, String filename) throws FileNotFoundException, URISyntaxException {
        return createDummyDataFile(configuration.getTestDataDirectory(), filename);
    }

    public static Location createDummyDataFile(SOSDirectory sosParent, String filename) throws FileNotFoundException, URISyntaxException {
        SOSFile sosFile = new FileBasedFile(sosParent, filename);

        File file = sosFile.toFile();
        File parent = file.getParentFile();
        if(!parent.exists() && !parent.mkdirs()){
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("The first line");
            writer.println("The second line");
        }

        return new URILocation("file://"+sosFile.getPathname());
    }

    public static void appendToFile(Location location, String text) throws URISyntaxException, IOException {

        try (PrintWriter writer = new PrintWriter(
                new FileOutputStream(
                new File(HelperTest.localURItoPath(location)), true))) {
            writer.append(text);
        }
    }

    public static String InputStreamToString(InputStream stream) throws IOException {
        return IOUtils.toString(stream);
    }

    public static void DeletePath(SOSDirectory directory) throws IOException {
        File dir = new File(directory.getPathname());

        if (dir.exists()) {
            FileUtils.cleanDirectory(dir);
        }
    }

    public static void CreateDBTestDump() throws DatabasePersistenceException, SQLException {
        Config.db_type = Config.DB_TYPE_SQLITE;
        Config.initDatabaseInfo();

        ConnectionSource connection = SQLDB.getSQLConnection();
        TableUtils.createTableIfNotExists(connection, Config.class);

        Config config = new Config();
        config.s_type = Config.S_TYPE_LOCAL;
        Dao<Config, String> nodesDAO = DaoManager.createDao(connection, Config.class);
        nodesDAO.create((Config) config);
    }

}