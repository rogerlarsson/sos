package uk.ac.standrews.cs.sos.impl.datamodel.directory;

import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.logger.LEVEL;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.exceptions.node.NodeNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.node.NodesCollectionException;
import uk.ac.standrews.cs.sos.exceptions.protocol.SOSProtocolException;
import uk.ac.standrews.cs.sos.impl.node.NodesCollectionImpl;
import uk.ac.standrews.cs.sos.impl.protocol.TasksQueue;
import uk.ac.standrews.cs.sos.impl.protocol.tasks.FetchManifest;
import uk.ac.standrews.cs.sos.impl.protocol.tasks.FetchVersions;
import uk.ac.standrews.cs.sos.impl.protocol.tasks.ManifestReplication;
import uk.ac.standrews.cs.sos.interfaces.manifests.ManifestsDirectory;
import uk.ac.standrews.cs.sos.interfaces.node.NodeType;
import uk.ac.standrews.cs.sos.model.Manifest;
import uk.ac.standrews.cs.sos.model.Node;
import uk.ac.standrews.cs.sos.model.NodesCollection;
import uk.ac.standrews.cs.sos.model.NodesCollectionType;
import uk.ac.standrews.cs.sos.services.ManifestsDataService;
import uk.ac.standrews.cs.sos.services.NodeDiscoveryService;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.ac.standrews.cs.sos.constants.Internals.REPLICATION_FACTOR_MULTIPLIER;
import static uk.ac.standrews.cs.sos.impl.services.SOSNodeDiscoveryService.NO_LIMIT;

/**
 * The remote manifest directory allows the node to replicate manifests to other nodes in the SOS
 * as well as finding manifests in the rest of the SOS
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class RemoteManifestsDirectory extends AbstractManifestsDirectory implements ManifestsDirectory {

    private DDSIndex ddsIndex;
    private NodeDiscoveryService nodeDiscoveryService;
    private ManifestsDataService manifestsDataService;

    public RemoteManifestsDirectory(DDSIndex ddsIndex, NodeDiscoveryService nodeDiscoveryService, ManifestsDataService manifestsDataService) {
        this.ddsIndex = ddsIndex;
        this.nodeDiscoveryService = nodeDiscoveryService;
        this.manifestsDataService = manifestsDataService;
    }

    /**
     * Async operation
     *
     * @param manifest
     * @throws ManifestPersistException
     */
    @Override
    public void addManifest(Manifest manifest) throws ManifestPersistException {

        try {
            NodesCollection replicationNode = nodeDiscoveryService.filterNodesCollection(new NodesCollectionImpl(NodesCollectionType.ANY), NodeType.DDS, 1);
            ManifestReplication replicationTask = new ManifestReplication(manifest, replicationNode, 1, nodeDiscoveryService, manifestsDataService);
            TasksQueue.instance().performAsyncTask(replicationTask);
        } catch (SOSProtocolException | NodesCollectionException e) {
            throw new ManifestPersistException("Unable to persist node to remote nodes");
        }

    }

    public void addManifest(Manifest manifest, NodesCollection nodesCollection, int replicationFactor) throws ManifestPersistException {

        NodesCollection replicationNodes = nodeDiscoveryService.filterNodesCollection(nodesCollection, NodeType.DDS, replicationFactor * REPLICATION_FACTOR_MULTIPLIER);
        try {
            // The replication task takes care of replicating the manifest and updating the ManifestDDSMapping if the replication is successful
            ManifestReplication replicationTask = new ManifestReplication(manifest, replicationNodes, replicationFactor, nodeDiscoveryService, manifestsDataService);
            TasksQueue.instance().performAsyncTask(replicationTask);
        } catch (SOSProtocolException e) {
            throw new ManifestPersistException("Unable to persist node to remote nodes");
        }
    }

    @Override
    public Manifest findManifest(IGUID guid) throws ManifestNotFoundException {

        try {
            return findManifest(new NodesCollectionImpl(NodesCollectionType.ANY), guid);
        } catch (NodesCollectionException e) {
            return null;
        }

    }

    public Manifest findManifest(NodesCollection nodesCollection, IGUID guid) throws ManifestNotFoundException {

        Set<IGUID> ddsGUIDsToCheck = null;
        try {
            ddsGUIDsToCheck = getDDSNodes(nodesCollection, guid);
        } catch (NodeNotFoundException e) {
            throw new ManifestNotFoundException("Unable to find manifest because there are no known DDS nodes");
        }

        for(IGUID ddsGUID : ddsGUIDsToCheck) {

            try {
                Node node = nodeDiscoveryService.getNode(ddsGUID);

                FetchManifest fetchManifest = new FetchManifest(node, guid); // FIXME - use different end-points for context, metadata, etc
                TasksQueue.instance().performSyncTask(fetchManifest);

                Manifest manifest = fetchManifest.getManifest();
                if (manifest == null) {
                    throw new ManifestNotFoundException("Unable to fetch manifest");
                }

                return manifest;

            } catch (NodeNotFoundException | IOException e) {
                SOS_LOG.log(LEVEL.WARN, "A problem occurred while attempting to fetch a manifest with GUID " + guid .toMultiHash()+ " from Node with GUID " + ddsGUID.toMultiHash());
            }

        }

        throw new ManifestNotFoundException("Unable to find manifest in other known DDS nodes");
    }

    public Set<IGUID> getVersions(IGUID invariant) {

        try {
            return getVersions(new NodesCollectionImpl(NodesCollectionType.ANY), invariant);
        } catch (NodesCollectionException e) {
            return new LinkedHashSet<>();
        }
    }

    public Set<IGUID> getVersions(NodesCollection nodesCollection, IGUID invariant) {


        Set<IGUID> ddsGUIDsToCheck;
        try {
            ddsGUIDsToCheck = getDDSNodes(nodesCollection, invariant);
        } catch (NodeNotFoundException e) {
            return new LinkedHashSet<>();
        }

        Set<IGUID> versionRefs = new LinkedHashSet<>();
        for(IGUID ddsGUID : ddsGUIDsToCheck) {

            try {
                Node node = nodeDiscoveryService.getNode(ddsGUID);

                FetchVersions fetchVersions = new FetchVersions(node, invariant);
                TasksQueue.instance().performSyncTask(fetchVersions);

                versionRefs.addAll(fetchVersions.getVersions());

            } catch (NodeNotFoundException | IOException e) {
                SOS_LOG.log(LEVEL.WARN, "A problem occurred while attempting to fetch versions for invariant " + invariant.toMultiHash()+ " from Node with GUID " + ddsGUID.toMultiHash());
            }

        }

        return versionRefs;
    }

    @Override
    public void flush() {}

    private Set<IGUID> getDDSNodes(NodesCollection nodesCollection, IGUID guid) throws NodeNotFoundException {

        Set<IGUID> ddsGUIDsToCheck;
        if (nodesCollection.type().equals(NodesCollectionType.SPECIFIED)) {
            NodesCollection ddsNodesOnly = nodeDiscoveryService.filterNodesCollection(nodesCollection, NodeType.DDS, NO_LIMIT);
            ddsGUIDsToCheck = ddsNodesOnly.nodesRefs();
        } else if (nodesCollection.type().equals(NodesCollectionType.ANY)){
            // Get DDS nodes where we know the entity could be
            ddsGUIDsToCheck = nodeDiscoveryService.getNodes(NodeType.DDS).stream()
                    .map(Node::getNodeGUID)
                    .collect(Collectors.toSet());
        } else {
            ddsGUIDsToCheck = ddsIndex.getDDSRefs(guid);
        }

        if (ddsGUIDsToCheck == null) {

            // Simply check any node
            ddsGUIDsToCheck = nodeDiscoveryService.getNodes(NodeType.DDS).stream() // FIXME - this call can be improved
                    .map(Node::getNodeGUID)
                    .collect(Collectors.toSet());
        }

        if (ddsGUIDsToCheck == null) {
            throw new NodeNotFoundException("Unable to find manifest because there are no known DDS nodes");
        }

        return ddsGUIDsToCheck;

    }

}