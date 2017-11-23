package uk.ac.standrews.cs.sos.services;

import uk.ac.standrews.cs.castore.data.Data;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.sos.exceptions.ServiceException;
import uk.ac.standrews.cs.sos.impl.datamodel.builders.AtomBuilder;
import uk.ac.standrews.cs.sos.impl.datamodel.builders.CompoundBuilder;
import uk.ac.standrews.cs.sos.impl.datamodel.builders.VersionBuilder;
import uk.ac.standrews.cs.sos.model.*;

/**
 * The Agent is one of the node roles within the Sea of Stuff.
 *
 * End-users interact with the SOS via the Agent.
 *
 * The Agent supports the following operations:
 * - pushing data/manifests to the SOS
 * - get data/manifests from the SOS
 * - find data in the SOS
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface Agent extends Service {

    /**
     * Adds data to the Sea of Stuff as an atom.
     *
     * @param atomBuilder for this atom
     * @return the added atom
     * @throws ServiceException if the atom could not be added
     *
     * @apiNote the data will not processed through contexts. Contexts operate over assets.
     */
    Atom addAtom(AtomBuilder atomBuilder) throws ServiceException;

    /**
     * Adds a Compound to the Sea of Stuff.
     *
     * @param compoundBuilder for this compound.
     * @return the added compound.
     * @throws ServiceException
     *
     * @see Manifest
     *
     * @deprecated - use addCollection(VersionBuilder)?
     */
    Compound addCompound(CompoundBuilder compoundBuilder) throws ServiceException;

    /**
     * Add a secure compound to the SOS
     *
     * @param compoundBuilder
     * @return
     * @throws ServiceException
     */
    SecureCompound addSecureCompound(CompoundBuilder compoundBuilder) throws ServiceException;

    /**
     * Adds a version of an asset to the Sea of Stuff.
     *
     * @param versionBuilder for this version
     * @return Version for the added asset.
     * @throws ServiceException
     *
     */
    Version addVersion(VersionBuilder versionBuilder) throws ServiceException;

    Version addData(VersionBuilder versionBuilder) throws ServiceException;
    Data getData(Version version) throws ServiceException;
    Version addCollection(VersionBuilder versionBuilder) throws ServiceException;

    /**
     * Get the data of an Atom.
     *
     * @param atom describing the atom to retrieve.
     * @return InputStream
     * @throws ServiceException
     */
    Data getAtomContent(Atom atom) throws ServiceException;

    /**
     * Get data given the guid of the atom
     *
     * @param atomGUID
     * @return
     * @throws ServiceException
     */
    Data getData(IGUID atomGUID) throws ServiceException;

    /**
     * Get the manifest matching the given GUID.
     *
     * @param guid                  of the manifest.
     * @return Manifest             the manifest associated with the GUID.
     * @throws ServiceException if the GUID is not known within the currently
     *                              explorable Sea of Stuff.
     *
     */
    Manifest getManifest(IGUID guid) throws ServiceException;
    Manifest getManifest(NodesCollection nodesCollection, IGUID guid) throws ServiceException;

    /**
     * Generate and add metadata for this atom
     *
     * @param data used to generate the metadata
     * @return the metadata generated
     * @throws ServiceException if the metadata could not be generated
     */
    Metadata addMetadata(Data data) throws ServiceException;

    // TODO - add secure metadata method

    /**
     * Get the metadata of version
     *
     * @param version
     * @return SOSMetadata mapped with the version
     */
    Metadata getMetadata(Version version) throws ServiceException;

    Metadata getMetadata(NodesCollection nodesCollection, IGUID guid) throws ServiceException;

    /**
     * Verify the manifest signature against the given role.
     *
     * @param role                          used to verify the manifest
     * @param manifest                      to be verified
     * @return <code>true</code>            if the GUID of the manifest matches
     *                                      the content referred by the manifest.
     * @throws ServiceException if the manifest could not be verified
     */
    boolean verifyManifestSignature(Role role, Manifest manifest) throws ServiceException;

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
     * @param manifest                      to be verified
     * @return <code>true</code>            if the GUID of the manifest matches
     *                                      the content referred by the manifest.
     */
    boolean verifyManifestIntegrity(Manifest manifest) throws ServiceException;

    /**
     * Get the propery value for the given manifest matching GUID
     * The manifest MUST be a version manifest
     *
     * TODO - do not return a simple object, but something custom-made (MetaObject)
     *
     * @param guid
     * @param property
     * @return
     * @throws ServiceException
     */
    Object getMetaProperty(IGUID guid, String property) throws ServiceException;
}
