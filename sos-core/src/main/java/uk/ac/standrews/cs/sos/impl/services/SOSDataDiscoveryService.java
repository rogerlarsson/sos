package uk.ac.standrews.cs.sos.impl.services;

import uk.ac.standrews.cs.castore.interfaces.IDirectory;
import uk.ac.standrews.cs.castore.interfaces.IFile;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.logger.LEVEL;
import uk.ac.standrews.cs.sos.SettingsConfiguration;
import uk.ac.standrews.cs.sos.exceptions.manifest.HEADNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.exceptions.manifest.TIPNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.node.NodesCollectionException;
import uk.ac.standrews.cs.sos.exceptions.storage.DataStorageException;
import uk.ac.standrews.cs.sos.impl.NodesCollectionImpl;
import uk.ac.standrews.cs.sos.impl.manifests.directory.DDSIndex;
import uk.ac.standrews.cs.sos.impl.manifests.directory.LocalManifestsDirectory;
import uk.ac.standrews.cs.sos.impl.manifests.directory.ManifestsCacheImpl;
import uk.ac.standrews.cs.sos.impl.manifests.directory.RemoteManifestsDirectory;
import uk.ac.standrews.cs.sos.impl.node.LocalStorage;
import uk.ac.standrews.cs.sos.interfaces.manifests.ManifestsCache;
import uk.ac.standrews.cs.sos.model.Manifest;
import uk.ac.standrews.cs.sos.model.ManifestType;
import uk.ac.standrews.cs.sos.model.NodesCollection;
import uk.ac.standrews.cs.sos.model.Version;
import uk.ac.standrews.cs.sos.services.DataDiscoveryService;
import uk.ac.standrews.cs.sos.services.NodeDiscoveryService;
import uk.ac.standrews.cs.sos.utils.Persistence;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import java.io.IOException;
import java.util.Set;

import static uk.ac.standrews.cs.sos.constants.Internals.CACHE_FILE;
import static uk.ac.standrews.cs.sos.constants.Internals.DDS_INDEX_FILE;
import static uk.ac.standrews.cs.sos.model.NodesCollection.TYPE.LOCAL;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSDataDiscoveryService implements DataDiscoveryService {

    private SettingsConfiguration.Settings.AdvanceServicesSettings.DDSSettings ddsSettings;

    // We have three layers for storing and getting manifests:
    // 1. memory cache
    // 2. local disk
    // 3. remote nodes (i.e. other Data Discovery services)
    private ManifestsCache inMemoryCache;
    private LocalManifestsDirectory local;
    private RemoteManifestsDirectory remote;

    // Uses to store/get manifests into the local disk
    private LocalStorage localStorage;

    // Maps ManifestGUID --> [ DDS Nodes that might have it ]
    private DDSIndex ddsIndex;

    public SOSDataDiscoveryService(SettingsConfiguration.Settings.AdvanceServicesSettings.DDSSettings ddsSettings,
                                   LocalStorage localStorage, NodeDiscoveryService nodeDiscoveryService) {

        this.ddsSettings = ddsSettings;

        this.localStorage = localStorage;

        loadOrCreateCache();
        loadOrCreateDDSIndex();

        local = new LocalManifestsDirectory(localStorage);
        remote = new RemoteManifestsDirectory(ddsIndex, nodeDiscoveryService, this);
    }

    @Override
    public void addManifest(Manifest manifest) throws ManifestPersistException {
        inMemoryCache.addManifest(manifest);
        local.addManifest(manifest);

        // Add/Update TIP
        ManifestType manifestType = manifest.getType();
        if (manifestType.equals(ManifestType.VERSION)) {

            Version version = (Version) manifest;
            inMemoryCache.advanceTip(version);
        }
    }

    @Override
    public void addManifest(Manifest manifest, NodesCollection nodes, int replication) throws ManifestPersistException {
        addManifest(manifest);

        // TODO -
        int replicationFactor = (replication - 1) <= ddsSettings.getMaxReplication() ? (replication - 1) : ddsSettings.getMaxReplication();
        if (replicationFactor > 0) {
            remote.addManifest(manifest, nodes, replication); // TODO will apply in async mode
        }

        // TODO - IF MANIFEST IS VERSION - should notify nodes that have PREVIOUS versions. See notebook at page 92
    }

    @Override
    public void addManifestDDSMapping(IGUID manifest, IGUID ddsNode) {
        ddsIndex.addEntry(manifest, ddsNode);
    }

    @Override
    public Manifest getManifest(IGUID guid) throws ManifestNotFoundException {

        try {
            return getManifest(new NodesCollectionImpl(LOCAL), guid);
        } catch (NodesCollectionException e) {
            throw new ManifestNotFoundException("Manifest not found");
        }
    }

    @Override
    public Manifest getManifest(NodesCollection nodes, IGUID guid) throws ManifestNotFoundException {

        if (guid == null || guid.isInvalid()) {
            throw new ManifestNotFoundException("GUID was invalid");
        }

        try {
            Manifest manifest = inMemoryCache.findManifest(guid);
            if (manifest == null) {

                manifest = local.findManifest(guid);
                if (manifest != null) {
                    inMemoryCache.addManifest(manifest);
                }
            }

            if (manifest == null && !nodes.type().equals(LOCAL)) {

                manifest = remote.findManifest(guid, nodes);
                if (manifest != null) {
                    inMemoryCache.addManifest(manifest);
                    local.addManifest(manifest);
                }
            }

            if (manifest == null) {
                throw new ManifestNotFoundException("Unable to find manifest in inMemoryCache, local, remote. GUID: " + guid.toShortString());
            }

            return manifest;
        } catch (ManifestPersistException e) {
            SOS_LOG.log(LEVEL.ERROR, "DDS - Unable to persist manifest to cache/local");
        }

        throw new ManifestNotFoundException("Manifest not found");
    }

    @Override
    public Set<IGUID> getAllAssets() {

        // NOTE - returning only the ones from the inMemoryCache for the moment
        return inMemoryCache.getAllAssets();
    }

    @Override
    public Set<IGUID> getAllAssets(NodesCollection nodesCollection) {

        // TODO - this requires some HTTP request to other nodes
        return null;
    }

    @Override
    public Set<IGUID> getTips(IGUID invariant) throws TIPNotFoundException {

        return inMemoryCache.getTips(invariant);
    }

    @Override
    public IGUID getHead(IGUID invariant) throws HEADNotFoundException {

        return inMemoryCache.getHead(invariant);
    }

    @Override
    public void setHead(Version version) {

        inMemoryCache.setHead(version);
    }

    @Override
    public void flush() {

        try {
            IDirectory cacheDir = localStorage.getNodeDirectory();

            IFile cacheFile = localStorage.createFile(cacheDir, CACHE_FILE);
            Persistence.Persist(inMemoryCache, cacheFile);

            IFile ddsIndexFile = localStorage.createFile(cacheDir, DDS_INDEX_FILE);
            Persistence.Persist(ddsIndex, ddsIndexFile);

        } catch (DataStorageException | IOException e) {
            SOS_LOG.log(LEVEL.ERROR, "Unable to persist the DDS inMemoryCache and/or index");
        }
    }

    private void loadOrCreateCache() {
        try {
            IDirectory cacheDir = localStorage.getNodeDirectory();
            IFile file = localStorage.createFile(cacheDir, CACHE_FILE);
            if (file.exists()) {
                inMemoryCache = ManifestsCacheImpl.load(localStorage, file, localStorage.getManifestsDirectory());
            }
        } catch (DataStorageException | ClassNotFoundException | IOException e) {
            SOS_LOG.log(LEVEL.ERROR, "Unable to load the DDS inMemoryCache");
        }

        if (inMemoryCache == null) {
            inMemoryCache = new ManifestsCacheImpl();
        }
    }

    private void loadOrCreateDDSIndex() {
        try {
            IDirectory cacheDir = localStorage.getNodeDirectory();
            IFile file = localStorage.createFile(cacheDir, DDS_INDEX_FILE);
            if (file.exists()) {
                ddsIndex = (DDSIndex) Persistence.Load(file);
            }
        } catch (DataStorageException | ClassNotFoundException | IOException e) {
            SOS_LOG.log(LEVEL.ERROR, "Unable to load the DDS index");
        }

        if (ddsIndex == null) {
            ddsIndex = new DDSIndex();
        }
    }

}
