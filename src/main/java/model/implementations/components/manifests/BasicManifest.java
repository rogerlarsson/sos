package model.implementations.components.manifests;

import model.exceptions.GuidGenerationException;
import model.implementations.utils.GUID;
import model.implementations.utils.GUIDsha1;
import model.interfaces.components.Manifest;
import org.apache.xmlbeans.impl.common.ReaderInputStream;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The BasicManifest defines the base implementation for all other manifests.
 * This class implements some of the methods that can be generalised across all
 * other types of manifests. Manifests extending the BasicManifest MUST provide
 * implementations for the abstract methods defined in this class.
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public abstract class BasicManifest implements Manifest {

    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");

    private GUID manifestGuid;
    private final String manifestType;

    /**
     * Constructor for a BasicManifest.
     * Initialise the type of manifest.
     *
     * @param manifestType
     */
    protected BasicManifest(String manifestType) {
        this.manifestType = manifestType;
    }

    /**
     * Verifies this manifest's GUID against its content.
     *
     * @return true if the GUID of the manifest matches the content.
     * @throws GuidGenerationException if the manifest's GUID could not be generated.
     */
    @Override
    public abstract boolean verify() throws GuidGenerationException;

    /**
     * Checks whether this manifest contains valid key-value entries.
     *
     * @return true if the manifest is valid.
     */
    @Override
    public boolean isValid() {
        return isGUIDValid(manifestGuid) && isManifestTypeValid();
    }

    /**
     * Transforms the content of this manifest to a JSON representation.
     *
     * @return JSON representation of this manifest.
     */
    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        obj.put(ManifestConstants.KEY_MANIFEST_GUID, manifestGuid);
        obj.put(ManifestConstants.KEY_TYPE, manifestType);

        return obj;
    }

    /**
     * Gets the GUID of this manifest.
     *
     * @return GUID of this manifest.
     */
    @Override
    public GUID getManifestGUID() {
        return this.manifestGuid;
    }

    /**
     * Gets the type of this manifest.
     *
     * @return the type of this manifest.
     */
    @Override
    public String getManifestType() {
        return this.manifestType;
    }

    /**
     * Checks if the given GUID contains valid hex characters.
     *
     * @param guid to validated.
     * @return true if the guid is valid.
     */
    protected boolean isGUIDValid(GUID guid) {
        Matcher matcher = HEX_PATTERN.matcher(guid.toString());
        return matcher.matches();
    }

    private boolean isManifestTypeValid() {
        return manifestType != null && !manifestType.isEmpty();
    }

    /**
     * Generates and set the manifest's guid.
     * Assumption:
     * the function generateManifestToHash MUST be implemented.
     *
     * @throws GuidGenerationException
     */
    protected void generateManifestGUID() throws GuidGenerationException {
        String manifest = generateManifestToHash().toString();
        manifestGuid = generateGUID(manifest);
    }

    /**
     * Generates a GUID given a string.
     *
     * @param string used to generate the GUID.
     * @return GUID of the string.
     * @throws GuidGenerationException if the GUID could not be generated.
     */
    protected GUID generateGUID(String string) throws GuidGenerationException {
        GUID guid = null;

        try (StringReader reader = new StringReader(string);
             InputStream inputStream = new ReaderInputStream(reader, "UTF-8");) {

            guid = generateGUID(inputStream);
        } catch (UnsupportedEncodingException e) {
            throw new GuidGenerationException("Unsupported Encoding");
        } catch (IOException e) {
            throw new GuidGenerationException("IO Exception");
        } catch (Exception e) {
            throw new GuidGenerationException("General Exception");
        }
        return guid;
    }

    /**
     * Generates a GUID given an InputStream.
     *
     * @param inputStream used to generate the GUID.
     * @return GUID of the input stream.
     * @throws GuidGenerationException if the GUID could not be generated.
     */
    protected GUID generateGUID(InputStream inputStream) throws GuidGenerationException {
        return new GUIDsha1(inputStream);
    }

    /**
     * Generates a JSON representation of the part of the manifest that are used
     * to generate the GUID of this manifest.
     *
     * @return JSONObject representation of this manifest.
     */
    protected abstract JSONObject generateManifestToHash();

}
