package uk.ac.standrews.cs.sos.filesystem;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.ac.standrews.cs.GUIDFactory;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.fs.exceptions.FileSystemCreationException;
import uk.ac.standrews.cs.fs.interfaces.IFileSystem;
import uk.ac.standrews.cs.sos.exceptions.manifest.HEADNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotMadeException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.interfaces.manifests.Asset;
import uk.ac.standrews.cs.sos.interfaces.manifests.Compound;
import uk.ac.standrews.cs.sos.interfaces.sos.Agent;
import uk.ac.standrews.cs.sos.model.manifests.Content;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSFileSystemFactoryTest {

    private SOS_LOG SOS_LOG = new SOS_LOG(GUIDFactory.generateRandomGUID());

    @BeforeMethod
    public void setUp() {

    }

    @Test (expectedExceptions = FileSystemCreationException.class)
    public void makeFileSystemWithGUIDTest() throws FileSystemCreationException {
        IGUID guid = GUIDFactory.generateRandomGUID();
        SOSFileSystemFactory fileSystemFactory = new SOSFileSystemFactory(guid);

        fileSystemFactory.makeFileSystem();
    }

    @Test
    public void makeFileSystemTest() throws FileSystemCreationException, HEADNotFoundException, ManifestNotMadeException, ManifestNotFoundException, ManifestPersistException {
        IGUID guid = GUIDFactory.generateRandomGUID();
        IGUID versionGUID = GUIDFactory.generateRandomGUID();
        Agent mockAgent = mockAgent(guid, versionGUID);

        SOSFileSystemFactory fileSystemFactory = new SOSFileSystemFactory(mockAgent, guid);
        IFileSystem fileSystem = fileSystemFactory.makeFileSystem();

        assertNotNull(fileSystem);
        assertEquals(versionGUID, fileSystem.getRootId());
    }

    private Agent mockAgent(IGUID guid, IGUID versionGUID) throws ManifestPersistException, ManifestNotMadeException, ManifestNotFoundException, HEADNotFoundException  {
        Agent mockAgent = mock(Agent.class);
        Compound mockRootFolder = mock(Compound.class);
        Asset mockRootAsset = mock(Asset.class);

        IGUID contentsGUID = GUIDFactory.generateRandomGUID();

        when(mockAgent.addCompound(any(), anyCollectionOf(Content.class))).thenReturn(mockRootFolder);
        when(mockAgent.addVersion(any())).thenReturn(mockRootAsset);

        when(mockRootFolder.getContents()).thenReturn(Collections.emptyList());
        when(mockRootAsset.getInvariantGUID()).thenReturn(guid);
        when(mockRootAsset.getVersionGUID()).thenReturn(versionGUID);
        when(mockRootAsset.getContentGUID()).thenReturn(contentsGUID);

        when(mockAgent.getManifest(contentsGUID)).thenReturn(mockRootFolder);
        when(mockAgent.getHEAD(guid)).thenReturn(mockRootAsset);

        return mockAgent;
    }
}