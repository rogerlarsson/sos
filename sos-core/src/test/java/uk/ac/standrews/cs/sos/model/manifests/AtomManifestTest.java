package uk.ac.standrews.cs.sos.model.manifests;


import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.Test;
import uk.ac.standrews.cs.GUIDFactory;
import uk.ac.standrews.cs.sos.SetUpTest;
import uk.ac.standrews.cs.sos.constants.Hashes;
import uk.ac.standrews.cs.sos.interfaces.locations.Location;
import uk.ac.standrews.cs.sos.model.locations.URILocation;
import uk.ac.standrews.cs.sos.model.locations.bundles.CacheLocationBundle;
import uk.ac.standrews.cs.sos.model.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.model.locations.bundles.ProvenanceLocationBundle;
import uk.ac.standrews.cs.sos.utils.HelperTest;
import uk.ac.standrews.cs.sos.utils.JSONHelper;

import java.util.ArrayList;
import java.util.Collection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class AtomManifestTest extends SetUpTest {

    @Test
    public void testNoLocations() throws Exception {
        Collection<LocationBundle> bundles = new ArrayList<>();
        AtomManifest atomManifest = ManifestFactory.createAtomManifest(GUIDFactory.recreateGUID(Hashes.TEST_STRING_HASHED), bundles);

        Collection<LocationBundle> others = atomManifest.getLocations();
        assertEquals(others, bundles);
    }

    @Test
    public void testNullGUID() throws Exception {
        Collection<LocationBundle> bundles = new ArrayList<>();
        Location location = HelperTest.createDummyDataFile(internalStorage);
        bundles.add(new CacheLocationBundle(location));
        AtomManifest atomManifest = ManifestFactory.createAtomManifest(null, bundles);

        Collection<LocationBundle> others = atomManifest.getLocations();
        assertEquals(others, bundles);
        assertFalse(atomManifest.isValid());
    }

    @Test
    public void testGetLocations() throws Exception {
        Collection<LocationBundle> bundles = new ArrayList<>();
        Location location = HelperTest.createDummyDataFile(internalStorage);
        bundles.add(new CacheLocationBundle(location));
        AtomManifest atomManifest = ManifestFactory.createAtomManifest(GUIDFactory.recreateGUID(Hashes.TEST_STRING_HASHED), bundles);

        Collection<LocationBundle> others = atomManifest.getLocations();
        assertEquals(others, bundles);
    }

    @Test (timeOut = 10000)
    public void testToJSON() throws Exception {
        Location location = new URILocation(Hashes.TEST_HTTP_BIN_URL);
        LocationBundle bundle = new ProvenanceLocationBundle(location);
        Collection<LocationBundle> bundles = new ArrayList<>();
        bundles.add(bundle);
        AtomManifest atomManifest = ManifestFactory.createAtomManifest(GUIDFactory.recreateGUID(Hashes.TEST_HTTP_BIN_HASH), bundles);

        Collection<LocationBundle> newBundles = atomManifest.getLocations();
        assertEquals(newBundles.size(), 1);

        JsonNode node = JSONHelper.JsonObjMapper().readTree(atomManifest.toString());
        JsonNode locationsNode = node.get(ManifestConstants.KEY_LOCATIONS);
        assertTrue(locationsNode.isArray());
        assertEquals(locationsNode.size(), 1);
    }

    @Test (timeOut = 10000)
    public void testIsValid() throws Exception {
        Location location = new URILocation(Hashes.TEST_HTTP_BIN_URL);
        LocationBundle bundle = new ProvenanceLocationBundle(location);
        Collection<LocationBundle> bundles = new ArrayList<>();
        bundles.add(bundle);
        AtomManifest atomManifest = ManifestFactory.createAtomManifest(GUIDFactory.recreateGUID(Hashes.TEST_HTTP_BIN_HASH), bundles);

        assertEquals(atomManifest.isValid(), true);
    }

    @Test (timeOut = 10000)
    public void testIsVerified() throws Exception {
        Location location = new URILocation(Hashes.TEST_HTTP_BIN_URL);
        LocationBundle bundle = new ProvenanceLocationBundle(location);
        Collection<LocationBundle> bundles = new ArrayList<>();
        bundles.add(bundle);
        AtomManifest atomManifest = ManifestFactory.createAtomManifest(GUIDFactory.recreateGUID(Hashes.TEST_HTTP_BIN_HASH), bundles);

        assertEquals(atomManifest.verify(null), true);
    }
}