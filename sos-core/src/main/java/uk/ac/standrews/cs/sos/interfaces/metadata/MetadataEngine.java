package uk.ac.standrews.cs.sos.interfaces.metadata;

import uk.ac.standrews.cs.castore.data.Data;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataException;
import uk.ac.standrews.cs.sos.model.Metadata;
import uk.ac.standrews.cs.sos.model.Role;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface MetadataEngine {

    /**
     * Process the given data and generates metadata from it.
     *
     * @param data from which to extract the metadata
     * @param role to sign and/or protect the generated metadata
     * @param encrypt if metadata should be encrypted
     * @return the metadata
     * @throws MetadataException if the metadata could not be generated
     */
    Metadata processData(Data data, Role role, boolean encrypt) throws MetadataException;
}
