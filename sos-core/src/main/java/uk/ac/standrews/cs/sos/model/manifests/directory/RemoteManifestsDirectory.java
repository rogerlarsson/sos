package uk.ac.standrews.cs.sos.model.manifests.directory;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.LEVEL;
import uk.ac.standrews.cs.sos.actors.protocol.FetchManifest;
import uk.ac.standrews.cs.sos.actors.protocol.ManifestReplication;
import uk.ac.standrews.cs.sos.exceptions.manifest.HEADNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.HEADNotSetException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.exceptions.node.NodeNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.protocol.SOSProtocolException;
import uk.ac.standrews.cs.sos.exceptions.protocol.SOSURLException;
import uk.ac.standrews.cs.sos.interfaces.actors.DDS;
import uk.ac.standrews.cs.sos.interfaces.actors.NDS;
import uk.ac.standrews.cs.sos.interfaces.manifests.Asset;
import uk.ac.standrews.cs.sos.interfaces.manifests.Manifest;
import uk.ac.standrews.cs.sos.interfaces.manifests.ManifestsDirectory;
import uk.ac.standrews.cs.sos.interfaces.node.Node;
import uk.ac.standrews.cs.sos.interfaces.policy.ManifestPolicy;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * The remote manifest directory allows the node to replicate manifests to other nodes in the SOS
 * as well as finding manifests in the rest of the SOS
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class RemoteManifestsDirectory implements ManifestsDirectory {

    private ManifestPolicy manifestPolicy;
    private DDSIndex ddsIndex;
    private NDS nds;
    private DDS dds;

    public RemoteManifestsDirectory(ManifestPolicy manifestPolicy, DDSIndex ddsIndex, NDS nds, DDS dds) {
        this.manifestPolicy = manifestPolicy;
        this.ddsIndex = ddsIndex;
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

        Manifest retval = null;

        Set<IGUID> guids = ddsIndex.getDDSRefs(guid); // TODO - attempt other nodes too?
        if (guids == null) {
            throw new ManifestNotFoundException("Unable to find manifest because there are no known DDS nodes");
        }

        for(IGUID g:guids) {
            try {
                Node node = nds.getNode(g);
                retval = FetchManifest.Fetch(node, guid);

                if (retval != null) {
                    break;
                }
            } catch (NodeNotFoundException | SOSURLException | IOException e) {
                SOS_LOG.log(LEVEL.WARN, "A problem occurred while attempting to fetch a manifest with GUID " + guid + " from Node with GUID " + g);
            }

        }
        
        if (retval == null) {
            throw new ManifestNotFoundException("Unable to find manifest in other known DDS nodes");    
        }
        
        return retval;
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
