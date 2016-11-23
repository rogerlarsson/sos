package uk.ac.standrews.cs.sos.interfaces.manifests;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.exceptions.manifest.HEADNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.HEADNotSetException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface ManifestsDirectory {

    void addManifest(Manifest manifest) throws ManifestPersistException;

    Manifest findManifest(IGUID guid) throws ManifestNotFoundException;

    Asset getHEAD(IGUID invariant) throws HEADNotFoundException;

    void setHEAD(IGUID version) throws HEADNotSetException;

    void flush();
}
