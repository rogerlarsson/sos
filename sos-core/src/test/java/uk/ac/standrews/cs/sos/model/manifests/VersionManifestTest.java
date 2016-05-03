package uk.ac.standrews.cs.sos.model.manifests;

import com.google.gson.JsonObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.Test;
import uk.ac.standrews.cs.GUIDFactory;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.SetUpTest;
import uk.ac.standrews.cs.sos.constants.Hashes;
import uk.ac.standrews.cs.sos.interfaces.identity.Identity;
import uk.ac.standrews.cs.sos.utils.StreamsUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class VersionManifestTest extends SetUpTest {

    private static final String EXPECTED_JSON_BASIC_ASSET =
            "{\"Type\":\"Version\"," +
                    "\"Signature\":\"AAAB\"," +
                    "\"ContentGUID\": \""+ Hashes.TEST_STRING_HASHED+"\"" +
                    "}}";

    private static final String EXPECTED_JSON_METADATA_ASSET =
            "{\"Type\":\"Version\"," +
                    "\"Signature\":\"AAAB\"," +
                    "\"Metadata\":[\""+ Hashes.TEST_STRING_HASHED+"\"]," +
                    "\"ContentGUID\": \""+ Hashes.TEST_STRING_HASHED+"\"" +
                    "}}";

    private static final String EXPECTED_JSON_PREVIOUS_ASSET =
            "{\"Type\":\"Version\"," +
                    "\"Invariant\":\""+ Hashes.TEST_STRING_HASHED+"\"," +
                    "\"Signature\":\"AAAB\"," +
                    "\"Previous\":[\""+ Hashes.TEST_STRING_HASHED+"\"]," +
                    "\"ContentGUID\": \""+ Hashes.TEST_STRING_HASHED+"\"" +
                    "}}";

    private static final String EXPECTED_JSON_METADATA_AND_PREVIOUS_ASSET =
            "{\"Type\":\"Version\"," +
                    "\"Invariant\":\""+ Hashes.TEST_STRING_HASHED+"\"," +
                    "\"Signature\":\"AAAB\"," +
                    "\"Metadata\":[\""+ Hashes.TEST_STRING_HASHED+"\"]," +
                    "\"Previous\":[\""+ Hashes.TEST_STRING_HASHED+"\"]," +
                    "\"ContentGUID\": \""+ Hashes.TEST_STRING_HASHED+"\"" +
                    "}}";

    @Test
    public void testBasicAssetConstructor() throws Exception {
        InputStream inputStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID guid = GUIDFactory.generateGUID(inputStreamFake);

        Identity identityMocked = mock(Identity.class);
        byte[] fakedSignature = new byte[]{0, 0, 1};
        when(identityMocked.sign(any(String.class))).thenReturn(fakedSignature);

        VersionManifest assetManifest = new VersionManifest(null, guid, null, null, identityMocked);

        JsonObject gson = assetManifest.toJSON();
        assertNotNull(gson.get("Version"));
        assertNotNull(gson.get("Invariant"));
        JSONAssert.assertEquals(EXPECTED_JSON_BASIC_ASSET, gson.toString(), false);
    }

    @Test
    public void testMetadataAssetConstructor() throws Exception {
        InputStream inputStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID guid = GUIDFactory.generateGUID(inputStreamFake);

        InputStream metadataStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID metadataGUID = GUIDFactory.generateGUID(metadataStreamFake);
        Collection<IGUID> metadata = new ArrayList<>();
        metadata.add(metadataGUID);

        Identity identityMocked = mock(Identity.class);
        byte[] fakedSignature = new byte[]{0, 0, 1};
        when(identityMocked.sign(any(String.class))).thenReturn(fakedSignature);

        VersionManifest assetManifest = new VersionManifest(null, guid, null, metadata, identityMocked);

        JsonObject gson = assetManifest.toJSON();
        assertNotNull(gson.get("Version"));
        assertNotNull(gson.get("Invariant"));
        JSONAssert.assertEquals(EXPECTED_JSON_METADATA_ASSET, gson.toString(), false);
    }

    @Test
    public void testPreviousAssetConstructor() throws Exception {
        InputStream inputStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID guid = GUIDFactory.generateGUID(inputStreamFake);

        InputStream invariantStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID invariantGUID = GUIDFactory.generateGUID(invariantStreamFake);

        InputStream previousStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID previousGUID = GUIDFactory.generateGUID(previousStreamFake);
        Collection<IGUID> previous = new ArrayList<>();
        previous.add(previousGUID);

        Identity identityMocked = mock(Identity.class);
        byte[] fakedSignature = new byte[]{0, 0, 1};
        when(identityMocked.sign(any(String.class))).thenReturn(fakedSignature);

        VersionManifest assetManifest = new VersionManifest(invariantGUID, guid, previous, null, identityMocked);

        JsonObject gson = assetManifest.toJSON();
        assertNotNull(gson.get("Version"));
        JSONAssert.assertEquals(EXPECTED_JSON_PREVIOUS_ASSET, gson.toString(), false);
    }

    @Test
    public void testMetadataAndPreviousAssetConstructor() throws Exception {
        InputStream inputStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID guid = GUIDFactory.generateGUID(inputStreamFake);

        InputStream invariantStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID invariantGUID = GUIDFactory.generateGUID(invariantStreamFake);

        InputStream previousStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID previousGUID = GUIDFactory.generateGUID(previousStreamFake);
        Collection<IGUID> previous = new ArrayList<>();
        previous.add(previousGUID);

        InputStream metadataStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID metadataGUID = GUIDFactory.generateGUID(metadataStreamFake);
        Collection<IGUID> metadata = new ArrayList<>();
        metadata.add(metadataGUID);

        Identity identityMocked = mock(Identity.class);
        byte[] fakedSignature = new byte[]{0, 0, 1};
        when(identityMocked.sign(any(String.class))).thenReturn(fakedSignature);

        VersionManifest assetManifest = new VersionManifest(invariantGUID, guid, previous, metadata, identityMocked);

        JsonObject gson = assetManifest.toJSON();
        assertNotNull(gson.get("Version"));
        JSONAssert.assertEquals(EXPECTED_JSON_METADATA_AND_PREVIOUS_ASSET, gson.toString(), false);
    }

    @Test
    public void testGetters() throws Exception {
        InputStream inputStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID guid = GUIDFactory.generateGUID(inputStreamFake);

        InputStream invariantStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID invariantGUID = GUIDFactory.generateGUID(invariantStreamFake);

        InputStream previousStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID previousGUID = GUIDFactory.generateGUID(previousStreamFake);
        Collection<IGUID> previous = new ArrayList<>();
        previous.add(previousGUID);

        InputStream metadataStreamFake = StreamsUtils.StringToInputStream(Hashes.TEST_STRING);
        IGUID metadataGUID = GUIDFactory.generateGUID(metadataStreamFake);
        Collection<IGUID> metadata = new ArrayList<>();
        metadata.add(metadataGUID);

        Identity identityMocked = mock(Identity.class);
        byte[] fakedSignature = new byte[]{0, 0, 1};
        when(identityMocked.sign(any(String.class))).thenReturn(fakedSignature);

        VersionManifest assetManifest = new VersionManifest(invariantGUID, guid, previous, metadata, identityMocked);

        assertEquals(assetManifest.getContentGUID(), guid);
        assertEquals(assetManifest.getInvariantGUID(), invariantGUID);
        assertEquals(assetManifest.getMetadata(), metadata);
        assertEquals(assetManifest.getPreviousManifests(), previous);
    }
}