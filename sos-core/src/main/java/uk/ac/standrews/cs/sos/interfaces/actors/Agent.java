package uk.ac.standrews.cs.sos.interfaces.actors;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.exceptions.AtomNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotMadeException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestVerificationException;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataException;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataNotFoundException;
import uk.ac.standrews.cs.sos.interfaces.identity.Identity;
import uk.ac.standrews.cs.sos.interfaces.manifests.Asset;
import uk.ac.standrews.cs.sos.interfaces.manifests.Atom;
import uk.ac.standrews.cs.sos.interfaces.manifests.Compound;
import uk.ac.standrews.cs.sos.interfaces.manifests.Manifest;
import uk.ac.standrews.cs.sos.interfaces.metadata.SOSMetadata;
import uk.ac.standrews.cs.sos.model.manifests.CompoundType;
import uk.ac.standrews.cs.sos.model.manifests.Content;
import uk.ac.standrews.cs.sos.model.manifests.builders.AssetBuilder;
import uk.ac.standrews.cs.sos.model.manifests.builders.AtomBuilder;
import uk.ac.standrews.cs.storage.exceptions.StorageException;

import java.io.InputStream;
import java.util.Set;

/**
 * The Client is one of the node roles within the Sea of Stuff.
 *
 * The Client supports the following operations:
 * - pushing data/manifests to the SOS
 * - get data/manifests from the SOS
 * - find data in the SOS
 *
 * The behaviour of these operations depends on the policy used by this SOS instance.
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface Agent {

    /**
     * Adds data to the Sea of Stuff as an atom.
     * The atom is cached locally and replicated according to the policy used by this instance.
     *
     * @param atomBuilder for this atom
     * @return the added atom
     * @throws StorageException
     * @throws ManifestPersistException
     */
    Atom addAtom(AtomBuilder atomBuilder)
            throws StorageException, ManifestPersistException;

    /**
     * Adds a Compound to the Sea of Stuff.
     *
     * @param contents of this compound.
     * @return the added compound.
     * @throws ManifestNotMadeException
     * @throws ManifestPersistException
     *
     * @see Manifest
     *
     * TODO - use CompoundBuilder
     */
    Compound addCompound(CompoundType type, Set<Content> contents)
            throws ManifestNotMadeException, ManifestPersistException;

    /**
     * Adds a version of an asset to the Sea of Stuff.
     *
     * @param assetBuilder for this version
     * @return Version for the added asset.
     * @throws ManifestNotMadeException
     * @throws ManifestPersistException
     *
     */
    Asset addAsset(AssetBuilder assetBuilder) throws ManifestNotMadeException, ManifestPersistException;


    /**
     * Add a manifest to the sea of stuff.
     * If {@code recursive} is true, then manifests referenced from the one specified will also be added,
     * assuming that such manifests are available and reachable.
     *
     * @param manifest to add to the NodeManager
     * @param recursive if true adds the references manifests and data recursively.
     * @throws ManifestPersistException
     */
    void addManifest(Manifest manifest, boolean recursive) throws ManifestPersistException;

    /**
     * Get the data of an Atom.
     *
     * @param atom describing the atom to retrieve.
     * @return InputStream
     * @throws AtomNotFoundException
     */
    InputStream getAtomContent(Atom atom) throws AtomNotFoundException;

    /**
     * Get the manifest matching the given GUID.
     *
     * @param guid                  of the manifest.
     * @return Manifest             the manifest associated with the GUID.
     * @throws ManifestNotFoundException if the GUID is not known within the currently
     *                              explorable Sea of Stuff.
     *
     */
    Manifest getManifest(IGUID guid) throws ManifestNotFoundException;

    /**
     * Verify the integrity of the manifest's GUID against the
     * content of the manifest.
     *
     * Hash-based verification ensures that a file has not been corrupted by
     * comparing the data's hash value to a previously calculated value.
     * If these values match, the data is presumed to be unmodified.
     * Due to the nature of hash functions, hash collisions may result
     * in false positives, but the likelihood of collisions is
     * often negligible with random corruption. (https://en.wikipedia.org/wiki/File_verification)
     *
     * @param identity                      used to verify the manifest
     * @param manifest                      to be verified
     * @return <code>true</code>            if the GUID of the manifest matches
     *                                      the content referred by the manifest.
     * @throws ManifestVerificationException if the manifest could not be verified
     */
    boolean verifyManifest(Identity identity, Manifest manifest) throws ManifestVerificationException;

    /**
     * Generate and add metadata for this atom
     *
     * @param atom used to generate the metadata
     * @return the metadata generated
     * @throws MetadataException if the metadata could not be generated
     */
    SOSMetadata addMetadata(Atom atom) throws MetadataException;

    /**
     * Get the metadata mapped to the specified guid
     *
     * @param guid for the metadata
     * @return SOSMetadata mapped with the guid
     */
    SOSMetadata getMetadata(IGUID guid) throws MetadataNotFoundException;
}
