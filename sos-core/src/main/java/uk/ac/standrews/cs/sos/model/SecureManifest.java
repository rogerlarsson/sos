package uk.ac.standrews.cs.sos.model;

import uk.ac.standrews.cs.guid.IGUID;

import java.util.HashMap;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface SecureManifest {

    HashMap<IGUID, String> keysRoles();

    void setKeysRoles(HashMap<IGUID, String> keysRoles);
}