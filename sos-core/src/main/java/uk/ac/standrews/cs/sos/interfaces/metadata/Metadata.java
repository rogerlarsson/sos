package uk.ac.standrews.cs.sos.interfaces.metadata;

import java.util.Collection;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface Metadata {

    Collection<Metadatum> getMetadatum();

    String toString();
}
