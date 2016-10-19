package uk.ac.standrews.cs.sos.interfaces.sos;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.interfaces.metadata.SOSMetadata;

import java.io.InputStream;

/**
 * Metadata Computation Service
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface MCS extends SeaOfStuff {

    SOSMetadata addMetadata(InputStream inputStream);

    SOSMetadata getMetadata(IGUID guid);
}
