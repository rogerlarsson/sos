package uk.ac.standrews.cs.sos.model;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.utilities.Pair;

import java.util.Set;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface SecureManifest extends Manifest {

    // [encrypted key, role guid]
    Set<Pair<String, IGUID>> keysRoles();

}