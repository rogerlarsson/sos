package uk.ac.standrews.cs.sos.SOSImpl;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.exceptions.identity.DecryptionException;
import uk.ac.standrews.cs.sos.exceptions.location.SourceLocationException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotMadeException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestVerificationFailedException;
import uk.ac.standrews.cs.sos.interfaces.identity.Identity;
import uk.ac.standrews.cs.sos.interfaces.locations.Location;
import uk.ac.standrews.cs.sos.interfaces.manifests.Atom;
import uk.ac.standrews.cs.sos.interfaces.manifests.Compound;
import uk.ac.standrews.cs.sos.interfaces.manifests.Manifest;
import uk.ac.standrews.cs.sos.interfaces.manifests.Version;
import uk.ac.standrews.cs.sos.interfaces.node.Node;
import uk.ac.standrews.cs.sos.interfaces.sos.Client;
import uk.ac.standrews.cs.sos.model.locations.LocationUtility;
import uk.ac.standrews.cs.sos.model.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.model.locations.bundles.ProvenanceLocationBundle;
import uk.ac.standrews.cs.sos.model.manifests.*;
import uk.ac.standrews.cs.sos.model.manifests.atom.AtomStorage;
import uk.ac.standrews.cs.sos.model.storage.InternalStorage;
import uk.ac.standrews.cs.storage.exceptions.StorageException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation class for the SeaOfStuff interface.
 * The purpose of this class is to delegate jobs to the appropriate manifests
 * of the sea of stuff.
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSClient implements Client {

    protected InternalStorage storage;
    protected Identity identity;
    protected ManifestsManager manifestsManager;

    private AtomStorage atomStorage;

    public SOSClient(Node node, InternalStorage storage, ManifestsManager manifestsManager,
                     Identity identity) {

        this.storage = storage;
        this.manifestsManager = manifestsManager;
        this.identity = identity;

        atomStorage = new AtomStorage(node.getNodeGUID(), storage);
    }

    @Override
    public Identity getIdentity() {
        return this.identity;
    }

    @Override
    public Atom addAtom(Location location) throws StorageException, ManifestPersistException {

        Collection<LocationBundle> bundles = new ArrayList<>();
        bundles.add(new ProvenanceLocationBundle(location));

        IGUID guid = store(location, bundles);
        AtomManifest manifest = ManifestFactory.createAtomManifest(guid, bundles);
        manifestsManager.addManifest(manifest);

        return manifest;
    }

    @Override
    public Atom addAtom(InputStream inputStream)
            throws ManifestPersistException, StorageException {

        Collection<LocationBundle> bundles = new ArrayList<>();
        IGUID guid = store(inputStream, bundles);
        AtomManifest manifest = ManifestFactory.createAtomManifest(guid, bundles);
        manifestsManager.addManifest(manifest);

        return manifest;
    }

    @Override
    public Compound addCompound(CompoundType type, Collection<Content> contents)
            throws ManifestNotMadeException, ManifestPersistException {

        CompoundManifest manifest = ManifestFactory.createCompoundManifest(type, contents, identity);
        manifestsManager.addManifest(manifest);

        return manifest;
    }

    @Override
    public Version addVersion(IGUID content,
                              IGUID invariant,
                              Collection<IGUID> prevs,
                              Collection<IGUID> metadata)
            throws ManifestNotMadeException, ManifestPersistException {

        VersionManifest manifest = ManifestFactory.createVersionManifest(content, invariant, prevs, metadata, identity);
        manifestsManager.addManifest(manifest);

        return manifest;
    }

    /**
     * Return an InputStream for the given Atom.
     * The caller should ensure that the stream is closed.
     *
     * @param atom describing the atom to retrieve.
     * @return
     */
    @Override
    public InputStream getAtomContent(Atom atom) {
        InputStream dataStream = null;
        Collection<LocationBundle> locations = atom.getLocations();
        for(LocationBundle location:locations) {

            try {
                dataStream = LocationUtility.getInputStreamFromLocation(location.getLocation());
            } catch (SourceLocationException e) {
                continue;
            }

            if (dataStream != null) {
                break;
            }
        }

        return dataStream;
    }

    @Override
    public void addManifest(Manifest manifest, boolean recursive) throws ManifestPersistException {
        manifestsManager.addManifest(manifest);

        // TODO - recursively look for other manifests to add to the NodeManager
        if (recursive) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Manifest getManifest(IGUID guid) throws ManifestNotFoundException {
        return manifestsManager.findManifest(guid);
    }

    @Override
    public boolean verifyManifest(Identity identity, Manifest manifest) throws ManifestVerificationFailedException {
        boolean ret;
        try {
            ret = manifest.verify(identity);
        } catch (GUIDGenerationException | DecryptionException e) {
            throw new ManifestVerificationFailedException("Manifest verification failed", e);
        }

        return ret;
    }

    @Override
    public Collection<IGUID> findManifestByType(String type) throws ManifestNotFoundException {
        return manifestsManager.findManifestsByType(type);
    }

    @Override
    public Collection<IGUID> findManifestByLabel(String label) throws ManifestNotFoundException {
        return manifestsManager.findManifestsThatMatchLabel(label);
    }

    @Override
    public Collection<IGUID> findVersions(IGUID invariant) throws ManifestNotFoundException {
        return manifestsManager.findVersions(invariant);
    }

    protected IGUID store(Location location, Collection<LocationBundle> bundles) throws StorageException {
        return atomStorage.cacheAtomAndUpdateLocationBundles(location, bundles);
    }

    protected IGUID store(InputStream inputStream, Collection<LocationBundle> bundles) throws StorageException {
        return atomStorage.cacheAtomAndUpdateLocationBundles(inputStream, bundles);
    }
}