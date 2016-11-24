package uk.ac.standrews.cs.sos.actors;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.actors.protocol.DDSNotificationInfo;
import uk.ac.standrews.cs.sos.actors.protocol.Replication;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.interfaces.actors.DDS;
import uk.ac.standrews.cs.sos.interfaces.actors.NDS;
import uk.ac.standrews.cs.sos.interfaces.actors.Storage;
import uk.ac.standrews.cs.sos.interfaces.locations.Location;
import uk.ac.standrews.cs.sos.interfaces.manifests.Atom;
import uk.ac.standrews.cs.sos.interfaces.manifests.Manifest;
import uk.ac.standrews.cs.sos.interfaces.node.Node;
import uk.ac.standrews.cs.sos.interfaces.policy.ReplicationPolicy;
import uk.ac.standrews.cs.sos.model.locations.LocationUtility;
import uk.ac.standrews.cs.sos.model.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.model.locations.bundles.ProvenanceLocationBundle;
import uk.ac.standrews.cs.sos.model.manifests.AtomManifest;
import uk.ac.standrews.cs.sos.model.manifests.ManifestFactory;
import uk.ac.standrews.cs.sos.model.manifests.ManifestType;
import uk.ac.standrews.cs.sos.model.manifests.atom.AtomStorage;
import uk.ac.standrews.cs.sos.model.manifests.builders.AtomBuilder;
import uk.ac.standrews.cs.sos.storage.LocalStorage;
import uk.ac.standrews.cs.storage.exceptions.StorageException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSStorage implements Storage {

    private ReplicationPolicy replicationPolicy;

    private NDS nds;
    private DDS dds;

    private AtomStorage atomStorage;

    public SOSStorage(Node node, LocalStorage storage, ReplicationPolicy replicationPolicy, NDS nds, DDS dds) {

        this.replicationPolicy = replicationPolicy;

        this.nds = nds;
        this.dds = dds;

        atomStorage = new AtomStorage(node.getNodeGUID(), storage);
    }

    @Override
    public Atom addAtom(AtomBuilder atomBuilder, boolean persist, DDSNotificationInfo ddsNotificationInfo) throws StorageException, ManifestPersistException {
        Collection<LocationBundle> bundles = new ArrayList<>();

        IGUID guid = addAtom(atomBuilder, bundles, persist);

        AtomManifest manifest = ManifestFactory.createAtomManifest(guid, bundles);
        dds.addManifest(manifest, false);

        // Run asynchronously
        replicateData(manifest, bundles);

        // Let the caller do this?
        // TODO - send manifest to DDS
        notifyDDS(ddsNotificationInfo, manifest);

        return manifest;
    }

    private void notifyDDS(DDSNotificationInfo ddsNotificationInfo, AtomManifest manifest) {

        if (ddsNotificationInfo.notifyDDSNodes()) {
            Set<Node> ddsNodes = new HashSet<>(); // HashSet does not preserve order

            if (ddsNotificationInfo.useDefaultDDSNodes()) {
                ddsNodes.addAll(nds.getDDSNodes());
            }

            if (ddsNotificationInfo.useSuggestedDDSNodes()) {
                ddsNodes.addAll(ddsNotificationInfo.getSuggestedDDSNodes());
            }

            Replication.ReplicateManifest(manifest, ddsNodes);

        }

    }

    /**
     * Return an InputStream for the given Atom.
     * The caller should ensure that the stream is closed.
     *
     * @param atom describing the atom to retrieve.
     * @return data referenced by the atom
     */
    @Override
    public InputStream getAtomContent(Atom atom) {
        InputStream dataStream = null;

        Iterator<LocationBundle> it = atomStorage.getLocationsIterator(atom.guid());
        while(it.hasNext()) {
            LocationBundle locationBundle = it.next();

            Location location = locationBundle.getLocation();
            dataStream = LocationUtility.getInputStreamFromLocation(location);

            if (dataStream != null) {
                break;
            }
        }

        return dataStream;
    }

    @Override
    public InputStream getAtomContent(IGUID guid) {
        try {
            Manifest manifest = dds.getManifest(guid);

            if (manifest.getManifestType() == ManifestType.ATOM) {
                Atom atom = (Atom) manifest;
                return getAtomContent(atom);
            }
        } catch (ManifestNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void flush() {
        atomStorage.flush();
    }

    private IGUID addAtom(AtomBuilder atomBuilder, Collection<LocationBundle> bundles, boolean persist) throws StorageException {
        IGUID guid;
        if (atomBuilder.isLocation()) {
            guid = addAtomByLocation(atomBuilder, bundles, persist);
        } else if (atomBuilder.isInputStream()) {
            guid = addAtomByStream(atomBuilder, bundles, persist);
        } else {
            throw new StorageException("AtomBuilder has not been set correctly");
        }

        return guid;
    }

    private IGUID addAtomByLocation(AtomBuilder atomBuilder, Collection<LocationBundle> bundles, boolean persist) throws StorageException {
        Location location = atomBuilder.getLocation();
        bundles.add(new ProvenanceLocationBundle(location));
        return store(location, bundles, persist);
    }

    private IGUID addAtomByStream(AtomBuilder atomBuilder, Collection<LocationBundle> bundles, boolean persist) throws StorageException {
        InputStream inputStream = atomBuilder.getInputStream();
        return store(inputStream, bundles, persist);
    }

    private IGUID store(Location location, Collection<LocationBundle> bundles, boolean persist) throws StorageException {
        if (persist) {
            return atomStorage.persistAtomAndUpdateLocationBundles(location, bundles); // FIXME - this should undo the cache locations(and indeX)
        } else {
            return atomStorage.cacheAtomAndUpdateLocationBundles(location, bundles);
        }
    }

    private IGUID store(InputStream inputStream, Collection<LocationBundle> bundles, boolean persist) throws StorageException {
        if (persist) {
            return atomStorage.persistAtomAndUpdateLocationBundles(inputStream, bundles);
        } else {
            return atomStorage.cacheAtomAndUpdateLocationBundles(inputStream, bundles);
        }
    }

    // FIXME - do some serious refactoring here
    // TODO - move this method in a Replication class
    private void replicateData(AtomManifest manifest, Collection<LocationBundle> bundles) {

        try (InputStream atomContent = getAtomContent(manifest)) {
            if (replicationPolicy.getReplicationFactor() > 0) {

                Runnable replicator = () -> {
                    Iterator<Node> storageNodes = nds.getStorageNodes().iterator();
                    // NOTE: contact NDS for storage nodes: NDS_GET_NODE by role

                    if (storageNodes.hasNext()) {
                        Node replicaNode = storageNodes.next();
                        try {
                            atomStorage.persistAtomToRemote(replicaNode, atomContent, bundles);
                        } catch (StorageException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        dds.addManifest(manifest, false); // Will update
                    } catch (ManifestPersistException e) {
                        e.printStackTrace();
                    }
                };

                replicator.run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replicateData(Atom atom) {
        if (replicationPolicy.getReplicationFactor() > 0) {
            try (InputStream atomContent = getAtomContent(atom)) {

                Collection<Node> storageNodes = nds.getStorageNodes();
                Replication.ReplicateData(atomContent, (Set<Node>) storageNodes); // FIXME - avoid casting

                // TODO - get data back from replication
                // TODO - async operation

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
