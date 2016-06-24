package uk.ac.standrews.cs.sos.node;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import uk.ac.standrews.cs.sos.interfaces.storage.SOSDirectory;
import uk.ac.standrews.cs.sos.interfaces.storage.SOSFile;
import uk.ac.standrews.cs.sos.model.storage.FileBased.FileBasedDirectory;
import uk.ac.standrews.cs.sos.model.storage.FileBased.FileBasedFile;

/**
 * This class contains all information to configure this SOS node.
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
@DatabaseTable(tableName = "configuration")
public class Config {

    // no-args constructor needed for ORMLite
    public Config() {}
    @DatabaseField(generatedId = true)
    private int id; // Must be provided for ORMLite

    /**
     * DEFAULT CONFIG VALUES
     */
    private static final String HOME = System.getProperty("user.home") + "/";
    private static final String SOS_ROOT = HOME + "sos/";
    private static final SOSDirectory ROOT_DIRECTORY_DEFAULT = new FileBasedDirectory(HOME + "sos");
    private static final String DEFAULT_ROOT_NAME = "";

    private static final String TEST_DATA_DIRECTORY_NAME = "test_data";
    private static final String DATA_DIRECTORY_NAME = "data";
    private static final String MANIFESTS_DIRECTORY_NAME = "manifests";
    private static final String INDEX_DIRECTORY_NAME = "index";
    private static final String KEYS_DIRECTORY_NAME = "keys";
    private static final String DATABASE_DIRECTORY_NAME_DEFAULT = "db";

    private static final String NODE_FILE = "node.txt";
    private static final String PRIVATE_KEY_FILE = "private.der";
    private static final String PUBLIC_KEY_FILE = "public.der";
    private static final String DB_DUMP_FILE_NAME_DEFAULT = "dump.db";



    public static SOSDirectory STORAGE_DIRECTORY;
    public static SOSDirectory TEST_DATA_DIRECTORY;
    public static SOSDirectory DATA_DIRECTORY;
    public static SOSDirectory MANIFEST_DIRECTORY;
    private static SOSDirectory INDEX_DIRECTORY;
    private static SOSDirectory KEYS_DIRECTORY;
    public static SOSDirectory DB_DIRECTORY;
    public static SOSFile DB_DUMP_FILE;

    private static SOSDirectory root = ROOT_DIRECTORY_DEFAULT;
    public static SOSDirectory storage_root = ROOT_DIRECTORY_DEFAULT;

    // Database
    public final static String DB_TYPE_SQLITE = "sqlite";
    public final static String DB_TYPE_MYSQL = "mysql";

    // Initialise with default values
    public static String db_type = DB_TYPE_SQLITE;
    public static String db_path = DATABASE_DIRECTORY_NAME_DEFAULT;
    public static String db_hostname;
    public static String db_username;
    public static String db_password;

    // Storage
    public final static String S_TYPE_LOCAL = "local";
    public final static String S_TYPE_NETWORK = "network";
    public final static String S_TYPE_AWS_S3 = "aws_s3";

    @DatabaseField(canBeNull = true)
    public String s_type;
    @DatabaseField(canBeNull = true)
    public String s_hostname; // Will be used if storage is over the network
    @DatabaseField(canBeNull = true)
    public String s_location; // This is the folder where we store internal properties of system (e.g. manifests, etc)
    public String s_username;
    public String s_password;
    public String s_access_key;
    public String s_secret_key;

    public static void initDatabase() {
        DB_DIRECTORY = new FileBasedDirectory(root, db_path);
        DB_DUMP_FILE = new FileBasedFile(DB_DIRECTORY, DB_DUMP_FILE_NAME_DEFAULT);
    }

    // FIXME - this might not be a file based directory!!!!!
    public static void initStorageRelativeDirectories() {
        TEST_DATA_DIRECTORY = new FileBasedDirectory(storage_root, TEST_DATA_DIRECTORY_NAME);
        DATA_DIRECTORY = new FileBasedDirectory(storage_root, DATA_DIRECTORY_NAME);
        MANIFEST_DIRECTORY = new FileBasedDirectory(storage_root, MANIFESTS_DIRECTORY_NAME);
    }

}
