package configurations.identity;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class IdentityConfiguration {

    // Suppresses default constructor, ensuring non-instantiability.
    private IdentityConfiguration() {}

    public static final String ALGORITHM = "RSA";
    public static final int KEY_SIZE = 1024; // in bytes

    // XXX This is OS dependent
    public static final String PRIVATE_KEY_FILE = "keys/private.key";
    public static final String PUBLIC_KEY_FILE = "keys/public.key";

    // @see http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
