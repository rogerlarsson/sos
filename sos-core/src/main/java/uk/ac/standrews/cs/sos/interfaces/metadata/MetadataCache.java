package uk.ac.standrews.cs.sos.interfaces.metadata;

import uk.ac.standrews.cs.IGUID;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface MetadataCache {

    void addMetadata(SOSMetadata metadata);

    SOSMetadata getMetadata(IGUID guid); // throws ManifestsCacheMissException;

    void persist();
}
