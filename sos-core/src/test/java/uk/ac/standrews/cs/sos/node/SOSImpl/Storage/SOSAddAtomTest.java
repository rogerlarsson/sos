package uk.ac.standrews.cs.sos.node.SOSImpl.Storage;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;
import uk.ac.standrews.cs.sos.interfaces.locations.Location;
import uk.ac.standrews.cs.sos.interfaces.manifests.Atom;
import uk.ac.standrews.cs.sos.model.locations.URILocation;
import uk.ac.standrews.cs.sos.model.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.model.locations.bundles.PersistLocationBundle;
import uk.ac.standrews.cs.sos.model.locations.bundles.ProvenanceLocationBundle;
import uk.ac.standrews.cs.sos.model.manifests.ManifestConstants;
import uk.ac.standrews.cs.sos.utils.HelperTest;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSAddAtomTest extends StorageTest {

    @Test
    public void testRetrieveAtomData() throws Exception {
        Location location = HelperTest.createDummyDataFile(configuration);
        Atom manifest = model.addAtom(location);
        assertEquals(manifest.getManifestType(), ManifestConstants.ATOM);

        InputStream inputStream = model.getAtomContent(manifest);
        assertTrue(IOUtils.contentEquals(location.getSource(), inputStream));
        inputStream.close();
    }

    @Test
    public void testAddAtomPersistentLocation() throws Exception {
        Location location = HelperTest.createDummyDataFile(configuration);
        Atom manifest = model.addAtom(location);
        assertEquals(manifest.getManifestType(), ManifestConstants.ATOM);

        Collection<LocationBundle> retrievedLocations = (manifest.getLocations());
        Iterator<LocationBundle> bundles = retrievedLocations.iterator();
        assertEquals(2, retrievedLocations.size());

        LocationBundle firstBundle = bundles.next();
        assertTrue(firstBundle instanceof ProvenanceLocationBundle);

        LocationBundle secondBundle = bundles.next();
        assertTrue(secondBundle instanceof PersistLocationBundle);
    }

    @Test
    public void testAddAtomStreamPersistentLocation() throws Exception {
        String testString = "first line and second line";
        InputStream stream = HelperTest.StringToInputStream(testString);
        Atom manifest = model.addAtom(stream);
        assertEquals(manifest.getManifestType(), ManifestConstants.ATOM);

        Collection<LocationBundle> retrievedLocations = (manifest.getLocations());
        Iterator<LocationBundle> bundles = retrievedLocations.iterator();
        assertEquals(1, retrievedLocations.size());

        LocationBundle secondBundle = bundles.next();
        assertTrue(secondBundle instanceof PersistLocationBundle);
    }

    @Test
    public void testAddAtomFromURLPersistentLocation() throws Exception {
        Location location = new URILocation("http://www.eastcottvets.co.uk/uploads/Animals/gingerkitten.jpg");
        Atom manifest = model.addAtom(location);
        assertEquals(manifest.getManifestType(), ManifestConstants.ATOM);

        Collection<LocationBundle> retrievedLocations = (manifest.getLocations());
        Iterator<LocationBundle> bundles = retrievedLocations.iterator();
        assertEquals(2, retrievedLocations.size());

        LocationBundle firstBundle = bundles.next();
        assertTrue(firstBundle instanceof ProvenanceLocationBundle);

        LocationBundle secondBundle = bundles.next();
        assertTrue(secondBundle instanceof PersistLocationBundle);
    }

}