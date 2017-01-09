package uk.ac.standrews.cs.sos.model.manifests.directory;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.actors.protocol.ManifestReplication;
import uk.ac.standrews.cs.sos.exceptions.manifest.HEADNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.HEADNotSetException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.exceptions.protocol.SOSProtocolException;
import uk.ac.standrews.cs.sos.interfaces.actors.DDS;
import uk.ac.standrews.cs.sos.interfaces.actors.NDS;
import uk.ac.standrews.cs.sos.interfaces.manifests.Asset;
import uk.ac.standrews.cs.sos.interfaces.manifests.Manifest;
import uk.ac.standrews.cs.sos.interfaces.manifests.ManifestsDirectory;
import uk.ac.standrews.cs.sos.interfaces.node.Node;
import uk.ac.standrews.cs.sos.interfaces.policy.ManifestPolicy;

import java.util.Iterator;

/**
 * The remote manifest directory allows the node to replicate manifests to other nodes in the SOS
 * as well as finding manifests in the rest of the SOS
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class RemoteManifestsDirectory implements ManifestsDirectory {

    private ManifestPolicy manifestPolicy;
    private NDS nds;
    private DDS dds;

    public RemoteManifestsDirectory(ManifestPolicy manifestPolicy, NDS nds, DDS dds) {
        this.manifestPolicy = manifestPolicy;
        this.nds = nds;
        this.dds = dds;
    }

    @Override
    public void addManifest(Manifest manifest) throws ManifestPersistException {

        boolean replicate = manifestPolicy.storeManifestsRemotely();
        if (replicate) {
            Iterator<Node> nodes = nds.getStorageNodesIterator();
            int replicationFactor = manifestPolicy.getReplicationFactor();

            try {
                ManifestReplication.Replicate(manifest, nodes, replicationFactor, dds);
            } catch (SOSProtocolException e) {
                throw new ManifestPersistException("Unable to persist node to remote nodes");
            }
        }

    }

    @Override
    public void addManifestDDSMapping(IGUID manifestGUID, IGUID ddsNodeGUID) {
        throw new NotImplementedException();
    }

    @Override
    public Manifest findManifest(IGUID guid) throws ManifestNotFoundException {

        // See if we know dds nodes already (using ddsIndex)

        // Contact knwon DDS nodes
        // Ask such nodes about manifest with given guid
        // TODO - write protocol to find manifests

        throw new ManifestNotFoundException("remote directory - findManifest not implemented yet");
    }

    @Override
    public Asset getHEAD(IGUID invariant) throws HEADNotFoundException {
        throw new NotImplementedException();
    }

    @Override
    public void setHEAD(IGUID version) throws HEADNotSetException {
        throw new NotImplementedException();
    }

    @Override
    public void flush() {}

}
