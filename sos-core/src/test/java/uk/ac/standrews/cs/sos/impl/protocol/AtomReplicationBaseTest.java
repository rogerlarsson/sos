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
package uk.ac.standrews.cs.sos.impl.protocol;

import com.adobe.xmp.impl.Base64;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Delay;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import uk.ac.standrews.cs.castore.data.Data;
import uk.ac.standrews.cs.castore.data.StringData;
import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.guid.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.SettingsConfiguration;
import uk.ac.standrews.cs.sos.exceptions.ConfigurationException;
import uk.ac.standrews.cs.sos.exceptions.SOSException;
import uk.ac.standrews.cs.sos.exceptions.node.NodeNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.protocol.SOSProtocolException;
import uk.ac.standrews.cs.sos.impl.datamodel.locations.bundles.BundleTypes;
import uk.ac.standrews.cs.sos.impl.datamodel.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.impl.datamodel.locations.sos.SOSURLProtocol;
import uk.ac.standrews.cs.sos.impl.node.NodesCollectionImpl;
import uk.ac.standrews.cs.sos.impl.node.SOSLocalNode;
import uk.ac.standrews.cs.sos.impl.node.SOSNode;
import uk.ac.standrews.cs.sos.impl.protocol.tasks.AtomReplication;
import uk.ac.standrews.cs.sos.model.Node;
import uk.ac.standrews.cs.sos.model.NodesCollection;
import uk.ac.standrews.cs.sos.services.NodeDiscoveryService;
import uk.ac.standrews.cs.sos.services.StorageService;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;
import static uk.ac.standrews.cs.sos.constants.Internals.GUID_ALGORITHM;
import static uk.ac.standrews.cs.sos.constants.Paths.TEST_RESOURCES_PATH;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class AtomReplicationBaseTest extends ProtocolTest {

    private ClientAndServer mockServer;
    private ClientAndServer mockServerTwin;
    private static final int MOCK_SERVER_PORT = 10001;
    private static final int MOCK_TWIN_SERVER_PORT = 10002;

    private static final String TEST_DATA = "test-data";
    private static final String TEST_DATA_HASH = "SHA256_16_a186000422feab857329c684e9fe91412b1a5db084100b37a98cfc95b62aa867";

    private static final String TEST_DATA_TIMEOUT = "test-data-timeout";
    private static final String TEST_DATA_TIMEOUT_HASH = "SHA256_16_4ff7dd2940c8da777d9ba76e7f5ef4e994934c4d8ea255f8e975b72e176705ec";

    // This is the exact body request. Would be good if we have a JSON matcher method, so this string does not have to be exact, but simply an equivalent JSON obj of what we expect
    private static final String BASIC_REQUEST = "" +
            "{\n"+
            "  \"metadata\" : {\n"+
            "    \"replicationFactor\" : 0,\n"+
            "    \"replicationNodes\" : {\n"+
            "      \"type\" : \"ANY\",\n"+
            "      \"refs\" : [ ]\n"+
            "    },\n"+
            "    \"protectedData\" : false\n"+
            "  },\n"+
            "  \"data\" : \"{DATA}\",\n"+
            "  \"guid\" : \"{DATA_HASH}\"\n" +
            "}";

    private static final String NODE_ID = "SHA256_16_0000a025d7d3b2cf782da0ef24423181fdd4096091bd8cc18b18c3aab9cb00a4";
    private static final String TWIN_NODE_ID = "SHA256_16_bbbba025d7d3b2cf782da0ef24423181fdd4096091bd8cc18b18c3aab9cb00a4";

    private NodeDiscoveryService mockNodeDiscoveryService;

    @BeforeMethod
    public void setUp() throws SOSException, GUIDGenerationException, ConfigurationException, IOException {
        super.setUp();

        SettingsConfiguration.Settings settings = new SettingsConfiguration(new File(TEST_RESOURCES_PATH + "configurations/data_replication_test.json")).getSettingsObj();
        SOSLocalNode.settings = settings;

        new SOS_LOG(GUIDFactory.generateRandomGUID(GUID_ALGORITHM));

        IGUID testGUID = GUIDFactory.generateGUID(GUID_ALGORITHM, TEST_DATA);

        mockServer = startClientAndServer(MOCK_SERVER_PORT);
        mockServer.dumpToLog();
        mockServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/sos/storage/atom")
                                .withBody(BASIC_REQUEST
                                        .replace("{DATA}", Base64.encode(TEST_DATA))
                                        .replace("{DATA_HASH}", TEST_DATA_HASH))
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withBody(
                                        "    {\n" +
                                                "        \"type\" : \"Atom\",\n" +
                                                "        \"guid\" : \"" + testGUID.toMultiHash() + "\",\n" +
                                                "        \"locations\" : \n" +
                                                "        [\n" +
                                                "              {\n" +
                                                "                \"type\" : \"persistent\",\n" +
                                                "                \"location\" : \"sos://" + NODE_ID + "/" + testGUID.toMultiHash() + "\"\n" +
                                                "            } \n" +
                                                "        ]\n" +
                                                "    }\n"
                                )
                );

        // Request with delayed response
        mockServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/sos/storage/atom")
                                .withBody(BASIC_REQUEST
                                        .replace("{DATA}", Base64.encode(TEST_DATA_TIMEOUT))
                                        .replace("{DATA_HASH}", TEST_DATA_TIMEOUT_HASH))
                )
                .respond(
                        response()
                                .withStatusCode(500)
                                .withDelay(Delay.delay(TimeUnit.SECONDS, 40))
                );

        mockServerTwin = startClientAndServer(MOCK_TWIN_SERVER_PORT);
        mockServerTwin.dumpToLog();
        mockServerTwin
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/sos/storage/atom")
                                .withBody(BASIC_REQUEST
                                        .replace("{DATA}", Base64.encode(TEST_DATA))
                                        .replace("{DATA_HASH}", TEST_DATA_HASH))
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withBody(
                                        "    {\n" +
                                                "        \"type\" : \"Atom\",\n" +
                                                "        \"guid\" : \"" + testGUID.toMultiHash() + "\",\n" +
                                                "        \"locations\" : \n" +
                                                "        [\n" +
                                                "              {\n" +
                                                "                \"type\" : \"persistent\",\n" +
                                                "                \"location\" : \"sos://" + TWIN_NODE_ID + "/" + testGUID.toMultiHash() + "\"\n" +
                                                "            } \n" +
                                                "        ]\n" +
                                                "    }\n"
                                )
                );

        SOSURLProtocol.getInstance().register(null, null); // Local storage is not needed for this set of tests

        mockNodeDiscoveryService = mock(NodeDiscoveryService.class);
    }

    @AfterMethod
    public void tearDown() throws InterruptedException {
        mockServer.stop();
        mockServerTwin.stop();

        // Let the mockServer stop cleanly (it takes some time)
        Thread.sleep(2000);
    }

    void basicMockServerTest(boolean isSequential) throws GUIDGenerationException, SOSProtocolException, NodeNotFoundException {
        System.out.println("---> Sequential: " + isSequential);

        Node node = new SOSNode(GUIDFactory.generateRandomGUID(GUID_ALGORITHM), mockD_PublicKey,
                "localhost", MOCK_SERVER_PORT,
                false, true, false, false, false, false, false, false);
        when(mockNodeDiscoveryService.getNode(node.guid())).thenReturn(node);

        Set<IGUID> nodes = new HashSet<>();
        nodes.add(node.guid());
        NodesCollection nodesCollection = new NodesCollectionImpl(nodes);

        StorageService storageService = localSOSNode.getStorageService();

        IGUID testGUID = GUIDFactory.generateGUID(GUID_ALGORITHM, TEST_DATA);
        Data data = new StringData(TEST_DATA);
        AtomReplication replicationTask = new AtomReplication(testGUID, data, nodesCollection, 1, storageService,
                mockNodeDiscoveryService, false, false, isSequential);
        TasksQueue.instance().performSyncTask(replicationTask);
        assertEquals(replicationTask.getState(), TaskState.SUCCESSFUL);

        Iterator<LocationBundle> it = storageService.findLocations(testGUID).iterator();
        assertTrue(it.hasNext());

        LocationBundle locationBundle = it.next();
        assertEquals(locationBundle.getType(), BundleTypes.PERSISTENT);
        assertEquals(locationBundle.getLocation().toString(), "sos://" + NODE_ID + "/" + testGUID.toMultiHash());
    }

    void replicateToNoStorageNodeTest(boolean isSequential) throws GUIDGenerationException, SOSProtocolException, NodeNotFoundException {
        System.out.println("---> Sequential: " + isSequential);

        Node node = new SOSNode(GUIDFactory.generateRandomGUID(GUID_ALGORITHM), mockD_PublicKey,
                "localhost", MOCK_SERVER_PORT,
                false, false, false, false, false, false, false, false);
        when(mockNodeDiscoveryService.getNode(node.guid())).thenReturn(node);

        Set<IGUID> nodes = new HashSet<>();
        nodes.add(node.guid());
        NodesCollection nodesCollection = new NodesCollectionImpl(nodes);

        StorageService storageService = localSOSNode.getStorageService();

        IGUID testGUID = GUIDFactory.generateGUID(GUID_ALGORITHM, TEST_DATA);
        Data data = new StringData(TEST_DATA);
        AtomReplication replicationTask = new AtomReplication(testGUID, data, nodesCollection, 1, storageService,
                mockNodeDiscoveryService, false, false, isSequential);
        TasksQueue.instance().performSyncTask(replicationTask);
        assertEquals(replicationTask.getState(), TaskState.UNSUCCESSFUL);

        Iterator<LocationBundle> it = storageService.findLocations(testGUID).iterator();
        assertFalse(it.hasNext()); // Data has not been replicated, because we the node is not a storage one
    }

    void replicateOnlyOnceTest(boolean isSequential) throws GUIDGenerationException, SOSProtocolException, NodeNotFoundException {
        System.out.println("---> Sequential: " + isSequential);

        Node node = new SOSNode(GUIDFactory.generateRandomGUID(GUID_ALGORITHM), mockD_PublicKey,
                "localhost", MOCK_SERVER_PORT,
                false, false, false, false, false, false, false, false); // Won't replicate to non-storage
        when(mockNodeDiscoveryService.getNode(node.guid())).thenReturn(node);

        Node storageNode = new SOSNode(GUIDFactory.generateRandomGUID(GUID_ALGORITHM), mockD_PublicKey,
                "localhost", MOCK_SERVER_PORT,
                false, true, false, false, false, false, false, false);
        when(mockNodeDiscoveryService.getNode(storageNode.guid())).thenReturn(storageNode);

        Set<IGUID> nodes = new HashSet<>();
        nodes.add(node.guid());
        nodes.add(storageNode.guid());
        NodesCollection nodesCollection = new NodesCollectionImpl(nodes);

        StorageService storageService = localSOSNode.getStorageService();

        IGUID testGUID = GUIDFactory.generateGUID(GUID_ALGORITHM, TEST_DATA);
        Data data = new StringData(TEST_DATA);
        AtomReplication replicationTask = new AtomReplication(testGUID, data, nodesCollection, 2, storageService,
                mockNodeDiscoveryService, false, false, isSequential);
        TasksQueue.instance().performSyncTask(replicationTask);
        assertEquals(replicationTask.getState(), TaskState.UNSUCCESSFUL);

        Iterator<LocationBundle> it = storageService.findLocations(testGUID).iterator();
        assertTrue(it.hasNext());

        LocationBundle locationBundle = it.next();
        assertEquals(locationBundle.getType(), BundleTypes.PERSISTENT);
        assertEquals(locationBundle.getLocation().toString(), "sos://" + NODE_ID + "/" + testGUID.toMultiHash());

        assertFalse(it.hasNext());
    }

    void replicateOnlyOnceSecondTest(boolean isSequential) throws GUIDGenerationException, SOSProtocolException, NodeNotFoundException {
        System.out.println("---> Sequential: " + isSequential);

        Node node = new SOSNode(GUIDFactory.generateRandomGUID(GUID_ALGORITHM), mockD_PublicKey,
                "localhost", MOCK_SERVER_PORT,
                false, false, false, false, false, false, false, false); // Won't replicate to non-storage
        when(mockNodeDiscoveryService.getNode(node.guid())).thenReturn(node);

        Node storageNode = new SOSNode(GUIDFactory.generateRandomGUID(GUID_ALGORITHM), mockD_PublicKey,
                "localhost", MOCK_SERVER_PORT,
                false, true, false, false, false, false, false, false);
        when(mockNodeDiscoveryService.getNode(storageNode.guid())).thenReturn(storageNode);

        Node anotherNode = new SOSNode(GUIDFactory.generateRandomGUID(GUID_ALGORITHM), mockD_PublicKey,
                "localhost", MOCK_SERVER_PORT,
                true, false, true, true, true, false, false, false); // Won't replicate to non-storage
        when(mockNodeDiscoveryService.getNode(anotherNode.guid())).thenReturn(anotherNode);

        Set<IGUID> nodes = new HashSet<>();
        nodes.add(node.guid());
        nodes.add(storageNode.guid());
        nodes.add(anotherNode.guid());
        NodesCollection nodesCollection = new NodesCollectionImpl(nodes);

        StorageService storageService = localSOSNode.getStorageService();

        IGUID testGUID = GUIDFactory.generateGUID(GUID_ALGORITHM, TEST_DATA);
        Data data = new StringData(TEST_DATA);
        AtomReplication replicationTask = new AtomReplication(testGUID, data, nodesCollection, 3, storageService,
                mockNodeDiscoveryService, false, false, isSequential);
        TasksQueue.instance().performSyncTask(replicationTask);
        assertEquals(replicationTask.getState(), TaskState.UNSUCCESSFUL);

        Iterator<LocationBundle> it = storageService.findLocations(testGUID).iterator();
        assertTrue(it.hasNext());

        LocationBundle locationBundle = it.next();
        assertEquals(locationBundle.getType(), BundleTypes.PERSISTENT);
        assertEquals(locationBundle.getLocation().toString(), "sos://" + NODE_ID + "/" + testGUID.toMultiHash());

        assertFalse(it.hasNext());
    }

    void replicateToSameNodeTwiceTest(boolean isSequential) throws GUIDGenerationException, SOSProtocolException, NodeNotFoundException {
        System.out.println("---> Sequential: " + isSequential);

        Node storageNode = new SOSNode(GUIDFactory.generateRandomGUID(GUID_ALGORITHM), mockD_PublicKey,
                "localhost", MOCK_SERVER_PORT,
                false, true, false, false, false, false, false, false);
        when(mockNodeDiscoveryService.getNode(storageNode.guid())).thenReturn(storageNode);

        // Will have different GUID to get around the nodes Set. However, they will both return the same HTTP response (see mock server config for MOCK_SERVER_POST)
        Node twinStorageNode = new SOSNode(GUIDFactory.generateRandomGUID(GUID_ALGORITHM), mockD_PublicKey,
                "localhost", MOCK_SERVER_PORT,
                false, true, false, false, false, false, false, false);
        when(mockNodeDiscoveryService.getNode(twinStorageNode.guid())).thenReturn(twinStorageNode);

        Set<IGUID> nodes = new HashSet<>();
        nodes.add(storageNode.guid());
        nodes.add(twinStorageNode.guid());
        NodesCollection nodesCollection = new NodesCollectionImpl(nodes);

        StorageService storageService = localSOSNode.getStorageService();

        IGUID testGUID = GUIDFactory.generateGUID(GUID_ALGORITHM, TEST_DATA);
        Data data = new StringData(TEST_DATA);
        AtomReplication replicationTask = new AtomReplication(testGUID, data, nodesCollection, 2, storageService,
                mockNodeDiscoveryService, false, false, isSequential); // TODO - test for rep factor 1
        TasksQueue.instance().performSyncTask(replicationTask);

        Iterator<LocationBundle> it = storageService.findLocations(testGUID).iterator();
        assertTrue(it.hasNext());

        LocationBundle locationBundle = it.next();
        assertEquals(locationBundle.getType(), BundleTypes.PERSISTENT);
        assertEquals(locationBundle.getLocation().toString(), "sos://" + NODE_ID + "/" + testGUID.toMultiHash());

        assertFalse(it.hasNext());
    }

    void replicateSameDataTwiceTest(boolean isSequential) throws GUIDGenerationException, SOSProtocolException, NodeNotFoundException {
        System.out.println("---> Sequential: " + isSequential);

        Node storageNode = new SOSNode(GUIDFactory.recreateGUID(NODE_ID), mockD_PublicKey,
                "localhost", MOCK_SERVER_PORT,
                false, true, false, false, false, false, false, false);
        when(mockNodeDiscoveryService.getNode(storageNode.guid())).thenReturn(storageNode);

        Node twinStorageNode = new SOSNode(GUIDFactory.recreateGUID(TWIN_NODE_ID), mockD_PublicKey,
                "localhost", MOCK_TWIN_SERVER_PORT,
                false, true, false, false, false, false, false, false);
        when(mockNodeDiscoveryService.getNode(twinStorageNode.guid())).thenReturn(twinStorageNode);

        Set<IGUID> nodes = new HashSet<>();
        nodes.add(storageNode.guid());
        nodes.add(twinStorageNode.guid());
        NodesCollection nodesCollection = new NodesCollectionImpl(nodes);

        StorageService storageService = localSOSNode.getStorageService();

        IGUID testGUID = GUIDFactory.generateGUID(GUID_ALGORITHM, TEST_DATA);
        Data data = new StringData(TEST_DATA);
        AtomReplication replicationTask = new AtomReplication(testGUID, data, nodesCollection, 2, storageService,
                mockNodeDiscoveryService, false, false, isSequential);
        TasksQueue.instance().performSyncTask(replicationTask);
        assertEquals(replicationTask.getState(), TaskState.SUCCESSFUL);

        Iterator<LocationBundle> it = storageService.findLocations(testGUID).iterator();
        assertTrue(it.hasNext());
        LocationBundle locationBundle = it.next();
        assertEquals(locationBundle.getType(), BundleTypes.PERSISTENT);

        assertTrue(it.hasNext());
        locationBundle = it.next();
        assertEquals(locationBundle.getType(), BundleTypes.PERSISTENT);

        assertFalse(it.hasNext());
    }

    void basicTimeoutMockServerTest(boolean isSequential) throws GUIDGenerationException, SOSProtocolException, NodeNotFoundException {
        System.out.println("---> Sequential: " + isSequential);

        Node node = new SOSNode(GUIDFactory.generateRandomGUID(GUID_ALGORITHM), mockD_PublicKey,
                "localhost", MOCK_SERVER_PORT,
                false, true, false, false, false, false, false, false);
        when(mockNodeDiscoveryService.getNode(node.guid())).thenReturn(node);

        Set<IGUID> nodes = new HashSet<>();
        nodes.add(node.guid());
        NodesCollection nodesCollection = new NodesCollectionImpl(nodes);

        StorageService storageService = localSOSNode.getStorageService();

        IGUID testGUID = GUIDFactory.generateGUID(GUID_ALGORITHM, TEST_DATA_TIMEOUT);
        Data data = new StringData(TEST_DATA_TIMEOUT);
        AtomReplication replicationTask = new AtomReplication(testGUID, data, nodesCollection, 1, storageService,
                mockNodeDiscoveryService, false, false, isSequential);
        TasksQueue.instance().performSyncTask(replicationTask);

        assertEquals(replicationTask.getState(), TaskState.ERROR);
    }

}
