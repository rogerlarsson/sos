package uk.ac.standrews.cs.sos.node.SOSImpl;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestVerificationFailedException;
import uk.ac.standrews.cs.sos.exceptions.storage.DataStorageException;
import uk.ac.standrews.cs.sos.exceptions.storage.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.storage.ManifestPersistException;
import uk.ac.standrews.cs.sos.interfaces.identity.Identity;
import uk.ac.standrews.cs.sos.interfaces.locations.Location;
import uk.ac.standrews.cs.sos.interfaces.manifests.Manifest;
import uk.ac.standrews.cs.sos.model.Configuration;
import uk.ac.standrews.cs.sos.model.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.model.manifests.ManifestsManager;
import uk.ac.standrews.cs.sos.model.storage.StorageHelper;

import java.io.InputStream;
import java.util.Collection;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSStorage extends SOSCommon {

    public SOSStorage(Configuration configuration, ManifestsManager manifestsManager, Identity identity) {
        super(configuration, manifestsManager, identity);
    }

    @Override
    public void addManifest(Manifest manifest, boolean recursive) throws ManifestPersistException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Manifest getManifest(IGUID guid) throws ManifestNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean verifyManifest(Identity identity, Manifest manifest) throws ManifestVerificationFailedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<IGUID> findManifestByType(String type) throws ManifestNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<IGUID> findManifestByLabel(String label) throws ManifestNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<IGUID> findVersions(IGUID invariant) throws ManifestNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected IGUID store(Location location, Collection<LocationBundle> bundles) throws DataStorageException {
        return StorageHelper.persistAtomAndUpdateLocationBundles(configuration, location, bundles); // NOTE - this might undo the cache locations!
    }

    @Override
    protected IGUID store(InputStream inputStream, Collection<LocationBundle> bundles) throws DataStorageException {
        return StorageHelper.persistAtomAndUpdateLocationBundles(configuration, inputStream, bundles);
    }
}
