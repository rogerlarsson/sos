package sos.model.interfaces;

import IO.ManifestStream;
import sos.exceptions.*;
import sos.model.implementations.components.manifests.AssetManifest;
import sos.model.implementations.components.manifests.AtomManifest;
import sos.model.implementations.components.manifests.CompoundManifest;
import sos.model.implementations.identity.Session;
import sos.model.implementations.utils.Content;
import sos.model.implementations.utils.GUID;
import sos.model.implementations.utils.Location;
import sos.model.interfaces.components.Manifest;
import sos.model.interfaces.components.Metadata;
import sos.model.interfaces.identity.Identity;
import sos.model.interfaces.identity.IdentityToken;
import sos.model.interfaces.policies.Policy;

import java.util.Collection;

/**
 * This interface describes the set of allowed operations in the Sea of Stuff.
 * <br>
 * Client applications interact with the Sea of Stuff through this interface.
 * This interface abstracts the complexity of the data management in the Sea of Stuff,
 * such as data syncing, data retrieval from remote, de-duplication or data
 * redundancy.
 * <br>
 * The sea of stuff is a large collection of assets, compounds and atoms stored
 * across a collection of storage repositories.
 * The sea of stuff supports locatable persistent data via asset, compound and
 * atom GUIDs.
 *
 * <p>
 * The Sea of Stuff is divided into two logical sub-spaces: data and manifest space.
 * All manifests reside in the manifest space, while all other data is stored in the data space.
 * In reality, however, all data is stored in the data space.
 * </p>
 *
 * <p>
 * The sea of stuff is made of few entities that represent data (atoms), aggregations
 * of data (compounds), and allows these to be versioned and linked to other
 * entities - possibly metadata (assets). Atoms do exist in the sea of stuff
 * as sequence of bytes and are represented by an AtomManifest. Compounds and
 * assets, instead, are metadata information about aggregations, versions and
 * links. Therefore, compounds and assets exist only in the form of manifests -
 * CompoundManifest and AssetManifest.
 * </p>
 *
 * <p>
 * The operations defines in this interface are of 3 categories: <br>
 * 1. Manipulation of the sea of stuff - the operations change the state of the sea of stuff <br>
 * 2. Behaviour - describe how the operations in (1) behave
 * 3. XXX Local behaviours? session???
 * </p>
 *
 * <p>
 * Session and Policies:
 * </p>
 *
 * <p>
 * Metadata:
 * </p>
 *
 * <p>
 *     Assumptions:
 *     - manifests can be inspected easily and quickly
 *     - manifests can be stored efficiently
 *     - manifests can be represented in JSON.
 *     We use JSON for simplicity, but it could be any other format in theory.
 *
 * </p>
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface SeaOfStuff {

    /**
     * Register this identity for the current session.
     * Any operations following the registerIdentity operation will be associated with the
     * registered identity.
     *
     * @param identity
     * @return
     *
     * @see #unregisterIdentity(IdentityToken)
     * @see Session
     */
    IdentityToken registerIdentity(Identity identity);

    /**
     * Unregister this identity from the current session.
     *
     * @param identityToken
     * @throws UnknownIdentityException if the identityToken is unknown
     *
     * @see #registerIdentity(Identity)
     */
    void unregisterIdentity(IdentityToken identityToken) throws UnknownIdentityException;

    /**
     * Adds an atom to the Sea of Stuff.
     * The locations of the atom are used to generate a manifest.
     *
     * @param locations of the atom.
     * @return AtomManifest for the added atom.
     * @throws ManifestNotMadeException
     *
     * @see Manifest
     */
    AtomManifest addAtom(Collection<Location> locations) throws ManifestNotMadeException, ManifestSaveException;

    /**
     * Get an atom's data given an AtomManifest.
     *
     * @param atomManifest describing the atom to retrieve.
     * @return atom to retrieve in bytes.
     */
    byte[] getAtomContent(AtomManifest atomManifest);

    /**
     * Adds a CompoundManifest to the Sea of Stuff.
     *
     * @param contents of this compound.
     * @return CompoundManifest for the added compound.
     * @throws ManifestNotMadeException
     *
     * @see Manifest
     */
    CompoundManifest addCompound(Collection<Content> contents) throws ManifestNotMadeException, ManifestSaveException;

    /**
     * Adds an asset to the Sea of Stuff.
     * Note that an asset exists only in the manifest space.
     *
     * @param content of this asset.
     * @param prevs version of this asset.
     * @param metadata of this asset.
     * @return AssetManifest for the added asset.
     * @throws ManifestNotMadeException
     *
     */
    AssetManifest addAsset(Content content, Collection<GUID> prevs, GUID metadata) throws ManifestNotMadeException, ManifestSaveException;

    /**
     * Get the manifest that matches a given GUID.
     *
     * @param guid                  of the manifest.
     * @return Manifest             the manifest associated with the GUID.
     * @throws UnknownGUIDException if the GUID is not known within the currently
     *                              explorable Sea of Stuff.
     *
     * @see Manifest
     * @see GUID
     */
    Manifest getManifest(GUID guid) throws UnknownGUIDException;

    /**
     * Hash-based verification ensures that a file has not been corrupted by
     * comparing the data's hash value to a previously calculated value.
     * If these values match, the data is presumed to be unmodified.
     * Due to the nature of hash functions, hash collisions may result
     * in false positives, but the likelihood of collisions is
     * often negligible with random corruption. (https://en.wikipedia.org/wiki/File_verification)
     *
     * <p>
     * verifyManifest checks the integrity of the manifest's GUID against the
     * content of the manifest.
     * </p>
     *
     * @param manifest                      to be verified
     * @return <code>true</code>            if the GUID of the manifest matches
     *                                      the content referred by the manifest.
     * @throws ManifestVerificationFailedException
     *
     * @see Manifest
     * @see GUID
     */
    boolean verifyManifest(Manifest manifest) throws ManifestVerificationFailedException;

    /**
     * Set a policy for this current session. All operations in the sea of stuff
     * will obey this policy.
     *
     * To remove a policy from the current session, call {@link #unsetPolicy(Policy)}
     *
     * @param policy to be set.
     */
    void setPolicy(Policy policy);

    /**
     * Remove the specified policy from the current session.
     *
     * @param policy
     */
    void unsetPolicy(Policy policy);

    /**
     * Search the sea of stuff for manifests that match the specified metadata.
     *
     * @param metadata used for querying the sea of stuff.
     * @return a stream of manifests that match the query.
     */
    ManifestStream findManifests(Metadata metadata);
}
