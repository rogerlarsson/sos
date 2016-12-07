package uk.ac.standrews.cs.sos.actors.Client.replication;

import org.mockserver.integration.ClientAndServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import uk.ac.standrews.cs.GUIDFactory;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.SetUpTest;
import uk.ac.standrews.cs.sos.actors.protocol.SOSConstants;
import uk.ac.standrews.cs.sos.configuration.SOSConfiguration;
import uk.ac.standrews.cs.sos.exceptions.configuration.SOSConfigurationException;
import uk.ac.standrews.cs.sos.interfaces.actors.Agent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ClientReplicationTest extends SetUpTest {

    protected Agent agent;

    private ClientAndServer mockServer;
    private static final int MOCK_SERVER_PORT = 8110;

    protected static final String TEST_DATA = "test-data";
    private static final String NODE_ID = "22aafd93ab9a6e2ed501fc583685088cca66bac2";

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
                    "            \"guid\" : \"" + NODE_ID + "\"\n" +
                    "            \"port\" : " + MOCK_SERVER_PORT + "\n" +
                    "            \"hostname\" : \"localhost\"\n" +
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

        IGUID testGUID = GUIDFactory.generateGUID(TEST_DATA);

        mockServer = startClientAndServer(MOCK_SERVER_PORT);
        mockServer.dumpToLog();
        mockServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/storage/stream")
                                .withBody(TEST_DATA)
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withBody(
                                        "{\n" +
                                                "    \"" + SOSConstants.MANIFEST + "\" : \n" +
                                                "    {\n" +
                                                "        \"Type\" : \"Atom\",\n" +
                                                "        \"ContentGUID\" : \"" + testGUID + "\",\n" +
                                                "        \"Locations\" : \n" +
                                                "        [\n" +
                                                "              {\n" +
                                                "                \"Type\" : \"persistent\",\n" +
                                                "                \"Location\" : \"sos://" + NODE_ID + "/" + testGUID + "\"\n" +
                                                "            } \n" +
                                                "        ]\n" +
                                                "    }\n" +
                                                "}"
                                )
                );

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/storage/data/guid/" + testGUID)
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withBody(TEST_DATA)
                );
    }

    @AfterMethod
    public void tearDown() {
        mockServer.stop();
    }

    @Override
    protected void createConfiguration() throws SOSConfigurationException, IOException {
        File file = new File(TEST_RESOURCES_PATH + "config-storage.conf");
        Files.write(file.toPath(), MOCK_PROPERTIES.getBytes());

        configuration = new SOSConfiguration(file);
    }

}
