package uk.ac.standrews.cs.sos.node;

import org.testng.annotations.Test;
import uk.ac.standrews.cs.sos.interfaces.storage.SOSFile;

import static org.testng.Assert.*;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ConfigTest {

    private final static String TEST_PATH = "test";

    @Test(priority=0)
    public void dbAuthNullByDefaultTest() {
        assertNull(Config.db_hostname);
        assertNull(Config.db_username);
        assertNull(Config.db_password);
    }

    @Test(priority=1)
    public void initDefaultDBTest() {
        Config.initDatabaseInfo();

        assertEquals(Config.db_type, Config.DB_TYPE_SQLITE);
        assertNotNull(Config.DB_DUMP_FILE);
    }

    @Test(priority=2)
    public void initCustomDBTest() {
        Config.db_path = TEST_PATH;
        Config.initDatabaseInfo();

        assertEquals(Config.db_type, Config.DB_TYPE_SQLITE);
        assertNotNull(Config.DB_DUMP_FILE);
        assertEquals(Config.DB_DUMP_FILE.getParent().getName(), TEST_PATH);
    }

    @Test(priority=3)
    public void DBDumpFileChangesOnInitialisationTest() {
        Config.initDatabaseInfo();

        assertEquals(Config.db_type, Config.DB_TYPE_SQLITE);
        assertNotNull(Config.DB_DUMP_FILE);
        SOSFile actual = Config.DB_DUMP_FILE;

        // Update path, but do not initialise
        Config.db_path = TEST_PATH;
        assertEquals(actual, Config.DB_DUMP_FILE);

        // Initialise and verify that dump file has changed
        Config.initDatabaseInfo();
        assertNotEquals(actual, Config.DB_DUMP_FILE);
    }
}