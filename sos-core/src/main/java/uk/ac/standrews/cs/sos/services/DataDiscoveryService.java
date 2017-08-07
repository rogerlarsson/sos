package uk.ac.standrews.cs.sos.services;

import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.sos.exceptions.manifest.HEADNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.exceptions.manifest.TIPNotFoundException;
import uk.ac.standrews.cs.sos.model.Manifest;
import uk.ac.standrews.cs.sos.model.NodesCollection;
import uk.ac.standrews.cs.sos.model.Version;

import java.util.Set;

/**
 * Data Discovery Service
 *
 * The DDS takes care of:
 * - managing the manifests in the SOS
 * - track where the data is and help nodes to find the data
 *
 * TODO - pass param to methods so that it is possible to restrict the scope:
 * e.g. get manifest from this node, vs from all the nodes in the world
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface DataDiscoveryService {

    /**
     * Add a manifest to the local sea of stuff.
     *
     * @param manifest to add to the sea of stuff
     * @throws ManifestPersistException if the manifest could not be added correctly
     */
    void addManifest(Manifest manifest) throws ManifestPersistException;

    /**
     * Adds a manifest to the specified nodes using the replication factor as an AT_LEAST restriction
     *
     * @param manifest to be added
     * @param nodes where to add the manifest
     * @param replication suggested replication factor for the manifest
     * @throws ManifestPersistException if the manifest could not be added correctly
     */
    void addManifest(Manifest manifest, NodesCollection nodes, int replication) throws ManifestPersistException;

    /**
     * Get the manifest that matches a given GUID.
     *
     * @param guid                  of the manifest.
     * @return Manifest             the manifest associated with the GUID.
     * @throws ManifestNotFoundException if the GUID is not known within the currently
     *                              explorable Sea of Stuff.
     */
    Manifest getManifest(IGUID guid) throws ManifestNotFoundException;

    /**
     * Map the GUID of a manifest with the GUID of a DDS node.
     * This mapping will be used when trying to get the manifest via #getManifest(guid)
     *
     * TODO - rename to addManifestLocation?
     *
     * @param manifest for which to add a DDSnode ref
     * @param ddsNode the DDS node ref
     */
    void addManifestDDSMapping(IGUID manifest, IGUID ddsNode);

    /**
     * Get all known versions to this DDS node
     *
     * @return list of DDS versions's invariants
     */
    Set<IGUID> getAllAssets();

    /**
     * Get all the tips for the given invariant.
     * The tips are the leaves in the DAG of the asset.
     *
     * @param invariant for which to get the tips
     * @return the tips of the asset identified by the invariant
     */
    Set<IGUID> getTips(IGUID invariant) throws TIPNotFoundException;

    /**
     * Get the HEAD version for an asset.
     * The HEAD does not need to match one of the TIPS.
     *
     * The HEAD is the version of the asset that is currently active for this node.
     *
     * @param invariant for which to get the head
     * @return the reference of the HEAD version
     */
    IGUID getHead(IGUID invariant) throws HEADNotFoundException;

    /**
     * Set the specified version as the HEAD for its asset
     *
     * @param version the version to set as HEAD for its asset
     */
    void setHead(Version version);

    /**
     * Flushes the in-memory caches and indices into disk
     */
    void flush();

}
