package sos.configurations;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class DefaultConfiguration implements SeaConfiguration {

    private static final String HOME = System.getProperty("user.home");
    private static final String MANIFESTS_LOCATION = HOME + "/sos/manifests/";
    private static final String PRIVATE_KEY_FILE = HOME + "/sos/keys/private.der";
    private static final String PUBLIC_KEY_FILE = HOME + "/sos/keys/public.der";

    // Suppresses default constructor, ensuring non-instantiability.
    public DefaultConfiguration() {}

    @Override
    public String getLocalManifestsLocation() {
        return MANIFESTS_LOCATION;
    }

    public String[] getIdentityPaths() {
        return new String[]{PRIVATE_KEY_FILE, PUBLIC_KEY_FILE};
    }
}
