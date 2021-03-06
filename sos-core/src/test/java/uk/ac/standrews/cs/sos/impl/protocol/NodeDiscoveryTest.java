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

import org.mockserver.integration.ClientAndServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.ac.standrews.cs.castore.CastoreBuilder;
import uk.ac.standrews.cs.castore.CastoreFactory;
import uk.ac.standrews.cs.castore.exceptions.StorageException;
import uk.ac.standrews.cs.castore.interfaces.IFile;
import uk.ac.standrews.cs.castore.interfaces.IStorage;
import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.sos.SetUpTest;
import uk.ac.standrews.cs.sos.SettingsConfiguration;
import uk.ac.standrews.cs.sos.exceptions.SOSException;
import uk.ac.standrews.cs.sos.exceptions.db.DatabaseException;
import uk.ac.standrews.cs.sos.exceptions.node.NodeNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.node.NodeRegistrationException;
import uk.ac.standrews.cs.sos.exceptions.storage.DataStorageException;
import uk.ac.standrews.cs.sos.impl.database.DatabaseFactory;
import uk.ac.standrews.cs.sos.impl.database.DatabaseType;
import uk.ac.standrews.cs.sos.impl.datamodel.locations.sos.SOSURLProtocol;
import uk.ac.standrews.cs.sos.impl.node.LocalStorage;
import uk.ac.standrews.cs.sos.impl.node.SOSLocalNode;
import uk.ac.standrews.cs.sos.impl.services.SOSManifestsDataService;
import uk.ac.standrews.cs.sos.impl.services.SOSNodeDiscoveryService;
import uk.ac.standrews.cs.sos.interfaces.database.NodesDatabase;
import uk.ac.standrews.cs.sos.model.ManifestType;
import uk.ac.standrews.cs.sos.model.Node;
import uk.ac.standrews.cs.sos.services.ManifestsDataService;
import uk.ac.standrews.cs.utilities.crypto.CryptoException;
import uk.ac.standrews.cs.utilities.crypto.DigitalSignature;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static uk.ac.standrews.cs.sos.constants.Internals.DB_FILE;
import static uk.ac.standrews.cs.sos.constants.Internals.GUID_ALGORITHM;
import static uk.ac.standrews.cs.sos.constants.Paths.TEST_RESOURCES_PATH;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class NodeDiscoveryTest extends SetUpTest {

    private SOSNodeDiscoveryService nds;
    private Node localNode;
    private IGUID localNodeGUID = GUIDFactory.generateRandomGUID(GUID_ALGORITHM);

    private ClientAndServer mockServer;

    private static final String NODE_HOSTNAME = "localhost";
    private static final int NODE_PORT = 12345;

    private IGUID nodeFound;
    private IGUID nodeNotFound;

    @BeforeMethod
    public void setUp(Method testMethod) throws Exception {
        super.setUp(testMethod);
        SettingsConfiguration.Settings settings = new SettingsConfiguration(new File(TEST_RESOURCES_PATH + "configurations/node_discovery_test.json")).getSettingsObj();

        // Make sure that the DB path is clean
        localStorage.getNodeDirectory().remove(DB_FILE);

        NodesDatabase nodesDatabase;
        try {
            IFile dbFile = localStorage.createFile(localStorage.getNodeDirectory(), DB_FILE);
            DatabaseFactory.initInstance(dbFile);
            nodesDatabase = (NodesDatabase) DatabaseFactory.instance().getDatabase(DatabaseType.NODES);
        } catch (DatabaseException e) {
            throw new SOSException(e);
        }

        LocalStorage localStorage;
        try {
            CastoreBuilder castoreBuilder = settings.getStore().getCastoreBuilder();
            IStorage stor = CastoreFactory.createStorage(castoreBuilder);
            localStorage = new LocalStorage(stor);
        } catch (StorageException | DataStorageException e) {
            throw new SOSException(e);
        }

        localNode = mock(SOSLocalNode.class);
        SOSLocalNode.settings = settings;
        when(localNode.guid()).thenReturn(localNodeGUID);
        nds = new SOSNodeDiscoveryService(localNode, nodesDatabase);
        ManifestsDataService manifestsDataService = new SOSManifestsDataService(settings.getServices().getMds(), localStorage, nds);
        nds.setMDS(manifestsDataService);

        // MOCK SERVER SETUP
        nodeFound = GUIDFactory.generateRandomGUID(GUID_ALGORITHM);
        nodeNotFound = GUIDFactory.generateRandomGUID(GUID_ALGORITHM);

        mockServer = startClientAndServer(NODE_PORT);
        mockServer.dumpToLog();
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/sos/nds/node/guid/" + nodeFound.toMultiHash())
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(
                                        "{" +
                                                "    \"guid\": \"" + nodeFound.toMultiHash()  + "\"," +
                                                "    \"hostname\": \"localhost\"," +
                                                "    \"port\": 12345," +
                                                "    \"services\": {" +
                                                "        \"storage\": {" +
                                                "            \"exposed\": true" +
                                                "        }," +
                                                "        \"cms\": {" +
                                                "            \"exposed\": true" +
                                                "        }," +
                                                "        \"mds\": {" +
                                                "            \"exposed\": true" +
                                                "        }," +
                                                "        \"nds\": {" +
                                                "            \"exposed\": true" +
                                                "        }," +
                                                "        \"rms\": {" +
                                                "            \"exposed\": true" +
                                                "        }," +
                                                "        \"mms\": {" +
                                                "            \"exposed\": true" +
                                                "        }" +
                                                "    }" +
                                                "}"
                                )
                );

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/sos/nds/node/guid/" + nodeNotFound.toMultiHash())
                )
                .respond(
                        response()
                                .withStatusCode(400)
                );

        SOSURLProtocol.getInstance().register(null, null); // Local storage is not needed for this set of tests
    }

    @AfterMethod
    public void tearDown() {
        mockServer.stop();
    }

    @Test
    public void basicNodeDiscoveryTest() throws NodeNotFoundException {

        Node node = nds.getNode(localNodeGUID);
        assertEquals(node, localNode);
    }

    @Test (expectedExceptions = NodeNotFoundException.class)
    public void findNullNodeTest() throws NodeNotFoundException {

        nds.getNode(null);
    }

    @Test (expectedExceptions = NodeNotFoundException.class)
    public void findUnknownNodeTest() throws NodeNotFoundException {

        nds.getNode(GUIDFactory.generateRandomGUID(GUID_ALGORITHM));
    }

    @Test
    public void attemptToContactNDSNodeTest() throws NodeNotFoundException, NodeRegistrationException, CryptoException {

        Node ndsMock = mock(Node.class);
        when(ndsMock.getType()).thenReturn(ManifestType.NODE);
        when(ndsMock.isValid()).thenReturn(true);
        when(ndsMock.guid()).thenReturn(nodeFound);
        when(ndsMock.getSignatureCertificate()).thenReturn(DigitalSignature.generateKeys().getPublic());
        when(ndsMock.getHostAddress()).thenReturn(new InetSocketAddress(NODE_HOSTNAME, NODE_PORT));
        when(ndsMock.getIP()).thenReturn(NODE_HOSTNAME);
        when(ndsMock.isNDS()).thenReturn(true);
        nds.registerNode(ndsMock, true);

        Node node = nds.getNode(nodeFound);

        assertNotNull(node);
    }

    @Test (expectedExceptions = NodeNotFoundException.class)
    public void attemptToContactNDSNodeFailsTest() throws NodeNotFoundException, NodeRegistrationException, CryptoException {

        Node ndsMock = mock(Node.class);
        when(ndsMock.getType()).thenReturn(ManifestType.NODE);
        when(ndsMock.isValid()).thenReturn(true);
        when(ndsMock.guid()).thenReturn(GUIDFactory.generateRandomGUID(GUID_ALGORITHM));
        when(ndsMock.getSignatureCertificate()).thenReturn(DigitalSignature.generateKeys().getPublic());
        when(ndsMock.getHostAddress()).thenReturn(new InetSocketAddress(NODE_HOSTNAME, NODE_PORT));
        when(ndsMock.getIP()).thenReturn(NODE_HOSTNAME);
        when(ndsMock.isNDS()).thenReturn(true);
        nds.registerNode(ndsMock, true);

        nds.getNode(nodeNotFound);
    }
}