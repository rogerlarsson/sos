package uk.ac.standrews.cs.sos.actors.protocol;

import org.mockserver.integration.ClientAndServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.ac.standrews.cs.GUIDFactory;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.actors.protocol.tasks.FetchMetadata;
import uk.ac.standrews.cs.sos.exceptions.protocol.SOSProtocolException;
import uk.ac.standrews.cs.sos.exceptions.protocol.SOSURLException;
import uk.ac.standrews.cs.sos.interfaces.model.Metadata;
import uk.ac.standrews.cs.sos.interfaces.node.Node;
import uk.ac.standrews.cs.sos.model.locations.sos.SOSURLProtocol;
import uk.ac.standrews.cs.sos.node.SOSNode;
import uk.ac.standrews.cs.sos.tasks.TasksQueue;

import java.io.IOException;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class FetchMetadataTest {

    private ClientAndServer mockServer;
    private static final int MOCK_SERVER_PORT = 10005;

    private static final String GUID_METADATA = "02f80108b23125787b8bccc2b80ec623e2dffcd6";
    private static final String TEST_METADATA =
            " {\n" +
                    " \t\"GUID\": \"02f80108b23125787b8bccc2b80ec623e2dffcd6\",\n" +
                    " \t\"Properties\": {\n" +
                    " \t\t\"X-Parsed-By\": \"org.apache.tika.parser.DefaultParser\",\n" +
                    " \t\t\"Content-Encoding\": \"null\",\n" +
                    " \t\t\"Size\": \"26\",\n" +
                    " \t\t\"Timestamp\": \"1484736105\",\n" +
                    " \t\t\"Content-Type\": \"text/plain; charset=ISO-8859-1\"\n" +
                    " \t}\n" +
                    " }";

    @BeforeMethod
    public void setUp() throws SOSProtocolException, GUIDGenerationException {
        IGUID testGUID = GUIDFactory.recreateGUID(GUID_METADATA);

        mockServer = startClientAndServer(MOCK_SERVER_PORT);
        mockServer.dumpToLog();
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/mms/metadata/guid/" + testGUID.toString())
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(TEST_METADATA)
                );

        SOSURLProtocol.getInstance().register(null); // Local storage is not needed for this set of tests
    }

    @AfterMethod
    public void tearDown() {
        mockServer.stop();
    }

    @Test
    public void basicMetadataFetchTest() throws IOException, GUIDGenerationException, SOSURLException {

        Node node = new SOSNode(GUIDFactory.generateRandomGUID(),
                "localhost", MOCK_SERVER_PORT,
                false, false, false, false, true);

        IGUID testGUID = GUIDFactory.recreateGUID(GUID_METADATA);

        FetchMetadata fetchMetadata = new FetchMetadata(node, testGUID);
        TasksQueue.instance().performSyncTask(fetchMetadata);

        Metadata metadata = fetchMetadata.getMetadata();
        assertNotNull(metadata);
        assertEquals(metadata.guid(), testGUID);
    }
}
