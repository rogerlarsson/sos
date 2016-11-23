package uk.ac.standrews.cs.sos.actors.Client.replication;

import org.testng.annotations.BeforeMethod;
import uk.ac.standrews.cs.sos.SetUpTest;
import uk.ac.standrews.cs.sos.configuration.SOSConfiguration;
import uk.ac.standrews.cs.sos.exceptions.configuration.SOSConfigurationException;
import uk.ac.standrews.cs.sos.interfaces.sos.Agent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ClientReplicationTest extends SetUpTest {

    protected Agent agent;

    private static final String TEST_RESOURCES_PATH = "src/test/resources/";
    private static final String MOCK_PROPERTIES =
            "{\n" +
                    "    \"node\" : {\n" +
                    "        \"guid\" : \"3c9bfd93ab9a6e2ed501fc583685088cca66bac2\"\n" +
                    "        \"port\" : 8080\n" +
                    "        \"hostname\" : \"\"\n" +
                    "        \"is\" : {\n" +
                    "            \"agent\" : true\n" +
                    "            \"storage\" : false\n" +
                    "            \"dds\" : false\n" +
                    "            \"nds\" : false\n" +
                    "            \"mcs\" : false\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    \"db\" : {\n" +
                    "        \"type\" : \"sqlite\"\n" +
                    "        \"path\" : \"~/sos/db/dump.db\"\n" +
                    "    }\n" +
                    "\n" +
                    "    \"storage\" : {\n" +
                    "        \"type\" : \"local\"\n" +
                    "        \"location\" : \"~/sos/\"\n" +
                    "    }\n" +
                    "\n" +
                    "    \"keys\" : {\n" +
                    "        \"folder\" : \"~/sos/keys/\"\n" +
                    "    }\n" +
                    "\n" +
                    "    \"policy\" : {\n" +
                    "        \"replication\" : {\n" +
                    "            \"factor\" : 1\n" +
                    "        }\n" +
                    "        \"manifest\" : {\n" +
                    "            \"locally\" : true\n" +
                    "            \"remotely\" : false\n" +
                    "            \"replication\" : 0\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    \"bootstrap\" : [\n" +
                    "        {\n" +
                    "            \"guid\" : \"6b67f67f31908dd0e574699f163eda2cc117f7f4\"\n" + // NOTE: this will work only if LORNA is running.
                    "            \"port\" : 8080\n" +
                    "            \"hostname\" : \"cs-wifi-174.cs.st-andrews.ac.uk\"\n" +
                    "            \"is\" : {\n" +
                    "                \"agent\" : false\n" +
                    "                \"storage\" : true\n" +
                    "                \"dds\" : false\n" +
                    "                \"nds\" : false\n" +
                    "                \"mcs\" : false\n" +
                    "            }\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";

    @Override
    @BeforeMethod
    public void setUp(Method testMethod) throws Exception {
        super.setUp(testMethod);

        agent = localSOSNode.getAgent();
    }

    @Override
    protected void createConfiguration() throws SOSConfigurationException, IOException {
        File file = new File(TEST_RESOURCES_PATH + "config-storage.conf");
        Files.write(file.toPath(), MOCK_PROPERTIES.getBytes());

        configuration = new SOSConfiguration(file);
    }

}