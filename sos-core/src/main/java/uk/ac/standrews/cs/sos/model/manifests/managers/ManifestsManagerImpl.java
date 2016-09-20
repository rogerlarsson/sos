package uk.ac.standrews.cs.sos.model.manifests.managers;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.interfaces.index.Index;
import uk.ac.standrews.cs.sos.interfaces.manifests.Manifest;
import uk.ac.standrews.cs.sos.interfaces.manifests.Version;
import uk.ac.standrews.cs.sos.interfaces.manifests.managers.ManifestsManager;
import uk.ac.standrews.cs.sos.interfaces.policy.PolicyManager;
import uk.ac.standrews.cs.sos.model.storage.InternalStorage;
import uk.ac.standrews.cs.sos.node.NodeManager;

import java.util.Collection;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ManifestsManagerImpl implements ManifestsManager {

    private PolicyManager policyManager;

    private LocalManifestsManager local;
    private RemoteManifestsManager remote;

    public ManifestsManagerImpl(PolicyManager policyManager, InternalStorage internalStorage, Index index, NodeManager nodeManager) {
        this.policyManager = policyManager;

        local = new LocalManifestsManager(internalStorage, index);
        remote = new RemoteManifestsManager(nodeManager);
    }

    @Override
    public void addManifest(Manifest manifest) throws ManifestPersistException {
        local.addManifest(manifest);
    }

    @Override
    public Manifest findManifest(IGUID guid) throws ManifestNotFoundException {
        return local.findManifest(guid);
    }

    @Override
    public Version getLatest(IGUID guid) throws ManifestNotFoundException {
        // local.findLatestVersion();
        // not sure when to look for remote too
        return null;
    }

    public Collection<IGUID> findManifestsByType(String type) throws ManifestNotFoundException {
        return local.findManifestsByType(type);
    }

    public Collection<IGUID> findVersions(IGUID guid) throws ManifestNotFoundException {
        return local.findVersions(guid);
    }

    public Collection<IGUID> findManifestsThatMatchLabel(String label) throws ManifestNotFoundException {
        return local.findManifestsThatMatchLabel(label);
    }
}