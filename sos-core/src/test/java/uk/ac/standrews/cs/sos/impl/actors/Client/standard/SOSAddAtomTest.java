package uk.ac.standrews.cs.sos.impl.actors.Client.standard;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.Test;
import uk.ac.standrews.cs.sos.constants.Hashes;
import uk.ac.standrews.cs.sos.impl.locations.URILocation;
import uk.ac.standrews.cs.sos.impl.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.impl.manifests.AtomManifest;
import uk.ac.standrews.cs.sos.impl.manifests.builders.AtomBuilder;
import uk.ac.standrews.cs.sos.model.Atom;
import uk.ac.standrews.cs.sos.model.Location;
import uk.ac.standrews.cs.sos.model.Manifest;
import uk.ac.standrews.cs.sos.model.ManifestType;
import uk.ac.standrews.cs.sos.utils.HelperTest;
import uk.ac.standrews.cs.storage.interfaces.Directory;
import uk.ac.standrews.cs.storage.interfaces.File;

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.testng.AssertJUnit.*;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSAddAtomTest extends AgentTest {

    private static final int PAUSE_TIME_MS = 500;
    private static final int TEST_TIMEOUT = 10000;

    @Test
    public void testAddAtom() throws Exception {
        Location location = HelperTest.createDummyDataFile(localStorage);
        AtomBuilder builder = new AtomBuilder().setLocation(location);
        Atom manifest = agent.addAtom(builder);
        assertEquals(manifest.getType(), ManifestType.ATOM);

        Manifest retrievedManifest = agent.getManifest(manifest.guid());
        assertEquals(ManifestType.ATOM, retrievedManifest.getType());
        Set<LocationBundle> retrievedLocations = ((AtomManifest) retrievedManifest).getLocations();
        assertEquals(2, retrievedLocations.size());

        JSONAssert.assertEquals(manifest.toString(), retrievedManifest.toString(), true);
    }

    @Test
    public void testRetrieveAtomData() throws Exception {
        Location location = HelperTest.createDummyDataFile(localStorage);
        AtomBuilder builder = new AtomBuilder().setLocation(location);
        Atom manifest = agent.addAtom(builder);
        assertEquals(manifest.getType(), ManifestType.ATOM);

        Manifest retrievedManifest = agent.getManifest(manifest.guid());
        try (InputStream inputStream = agent.getAtomContent((AtomManifest) retrievedManifest)) {
            assertTrue(IOUtils.contentEquals(location.getSource(), inputStream));
        }
    }

    @Test
    public void testAtomDataVerify() throws Exception {
        Location location = HelperTest.createDummyDataFile(localStorage);
        AtomBuilder builder = new AtomBuilder().setLocation(location);
        Atom manifest = agent.addAtom(builder);
        assertEquals(manifest.getType(), ManifestType.ATOM);

        Manifest retrievedManifest = agent.getManifest(manifest.guid());
        assertTrue(retrievedManifest.verifySignature(null));
    }

    @Test
    public void testAtomDataVerifyFails() throws Exception {
        Location location = HelperTest.createDummyDataFile(localStorage);
        AtomBuilder builder = new AtomBuilder().setLocation(location);
        Atom manifest = agent.addAtom(builder);
        assertEquals(manifest.getType(), ManifestType.ATOM);

        Manifest retrievedManifest = agent.getManifest(manifest.guid());
        Set<LocationBundle> retrievedLocations = ((AtomManifest) retrievedManifest).getLocations();
        LocationBundle cachedLocation = retrievedLocations.iterator().next();

        HelperTest.appendToFile(cachedLocation.getLocation(), "Data has changed");
        assertFalse(retrievedManifest.verifySignature(null));
    }

    @Test (timeOut = TEST_TIMEOUT)
    public void testAddAtomFromURL() throws Exception {
        Location location = new URILocation("http://www.eastcottvets.co.uk/uploads/Animals/gingerkitten.jpg");
        AtomBuilder builder = new AtomBuilder().setLocation(location);
        Atom manifest = agent.addAtom(builder);
        assertEquals(manifest.getType(), ManifestType.ATOM);

        Manifest retrievedManifest = agent.getManifest(manifest.guid());
        assertEquals(ManifestType.ATOM, retrievedManifest.getType());

        System.out.println("SOSAddAtomTest: " + manifest.guid());
    }

    @Test (timeOut = TEST_TIMEOUT)
    public void testAddAtomFromURLHttps() throws Exception {
        Location location = new URILocation("https://i.ytimg.com/vi/NtgtMQwr3Ko/maxresdefault.jpg");
        AtomBuilder builder = new AtomBuilder().setLocation(location);
        Atom manifest = agent.addAtom(builder);
        assertEquals(manifest.getType(), ManifestType.ATOM);

        Manifest retrievedManifest = agent.getManifest(manifest.guid());
        assertEquals(ManifestType.ATOM, retrievedManifest.getType());

        System.out.println("SOSAddAtomTest: " + manifest.guid());
    }

    @Test (timeOut = TEST_TIMEOUT)
    public void testAddAtomFromURLHttpsPdf() throws Exception {
        Location location = new URILocation("https://www.adobe.com/be_en/active-use/pdf/Alice_in_Wonderland.pdf");
        AtomBuilder builder = new AtomBuilder().setLocation(location);
        Atom manifest = agent.addAtom(builder);
        assertEquals(manifest.getType(), ManifestType.ATOM);

        Manifest retrievedManifest = agent.getManifest(manifest.guid());
        assertEquals(ManifestType.ATOM, retrievedManifest.getType());

        System.out.println("SOSAddAtomTest: " + manifest.guid());
    }

    @Test (timeOut = TEST_TIMEOUT)
    public void testAddAtomFromURLHttpsTextFile() throws Exception {
        Location location = new URILocation("http://www.umich.edu/~umfandsf/other/ebooks/alice30.txt");
        AtomBuilder builder = new AtomBuilder().setLocation(location);
        Atom manifest = agent.addAtom(builder);
        assertEquals(manifest.getType(), ManifestType.ATOM);

        Manifest retrievedManifest = agent.getManifest(manifest.guid());
        assertEquals(ManifestType.ATOM, retrievedManifest.getType());

        System.out.println("SOSAddAtomTest: " + manifest.guid());
    }

    // NOTE: this test fails if using mutable internal storage
    @Test (timeOut = TEST_TIMEOUT, enabled = false)
    public void testAddAtomTwiceNoUpdate() throws Exception {
        Location location = new URILocation(Hashes.TEST_HTTP_BIN_URL);
        AtomBuilder builder = new AtomBuilder().setLocation(location);
        Atom manifest = agent.addAtom(builder);

        Directory dataDir = localStorage.getDataDirectory();
        Directory manifestsDir = localStorage.getManifestsDirectory();

        File file = localStorage.createFile(dataDir, manifest.guid().toString());
        File manifestFile = localStorage.createFile(manifestsDir, manifest.guid() + ".json");
        long lmFile = file.lastModified();
        long lmManifestFile = manifestFile.lastModified();

        Thread.sleep(PAUSE_TIME_MS);

        Location newLocation = new URILocation(Hashes.TEST_HTTP_BIN_URL);
        AtomBuilder secondBuilder = new AtomBuilder().setLocation(newLocation);
        Atom newManifest = agent.addAtom(secondBuilder);

        assertEquals(manifest.guid(), newManifest.guid());

        File newFile = localStorage.createFile(dataDir, newManifest.guid().toString());
        File newManifestFile = localStorage.createFile(manifestsDir, newManifest.guid() + ".json");
        long newlmFile = newFile.lastModified();
        long newlmManifestFile = newManifestFile.lastModified();

        assertEquals(newlmFile, lmFile);
        assertEquals(newlmManifestFile, lmManifestFile);
    }

    @Test
    public void testAddAtomFromStream() throws Exception {
        String testString = "first line and second line";
        InputStream stream = HelperTest.StringToInputStream(testString);
        AtomBuilder builder = new AtomBuilder().setInputStream(stream);
        Atom manifest = agent.addAtom(builder);
        assertNotNull(manifest.guid());
        assertEquals(manifest.getLocations().size(), 1);

        InputStream inputStream = agent.getAtomContent(manifest);
        String resultString = HelperTest.InputStreamToString(inputStream);
        assertEquals(testString, resultString);

        stream.close();
        inputStream.close();
    }

    @Test
    public void testAddLargeAtom() throws Exception {

        int HUNDRED_MB = 1024 * 1024 * 100;
        String bigString = RandomStringUtils.randomAscii(HUNDRED_MB);
        long start = System.nanoTime();

        try (InputStream stream = HelperTest.StringToInputStream(bigString)) {
            AtomBuilder builder = new AtomBuilder().setInputStream(stream);
            Atom manifest = agent.addAtom(builder);
            assertNotNull(manifest.guid());
        }

        System.out.println("1 atoms of 100mb uploaded in " + (System.nanoTime() - start) / 1000000000.0 + " seconds");
    }

    @Test
    public void testAddAtomsInSequence() throws Exception {

        int ONE_MB = 1024 * 1024;
        ConcurrentLinkedQueue<String> testStrings = new ConcurrentLinkedQueue<>();
        for(int i = 0; i < 100; i++) {
            testStrings.add(RandomStringUtils.randomAscii(ONE_MB)); // 1 ascii is 1 byte (in most computers)
        }

        long start = System.nanoTime();
        for(int i = 0; i < 100; i++) {
            addAtom(testStrings);
        }

        System.out.println("100 atoms of 1mb uploaded in " + (System.nanoTime() - start) / 1000000000.0 + " seconds");
    }

    @Test
    public void testAddAtomsInParallel() throws Exception {

        final int ATOMS_TO_ADD = 100;

        int ONE_MB = 1024 * 1024;
        ConcurrentLinkedQueue<String> testStrings = new ConcurrentLinkedQueue<>();
        for(int i = 0; i < ATOMS_TO_ADD; i++) {
            testStrings.add(RandomStringUtils.randomAlphabetic(ONE_MB) + i); // 1 ascii is 1 byte (in most computers)
        }

        System.out.println("Strings left  " + testStrings.size());

        Runnable runnable = () -> {
            addAtom(testStrings);
        };

        ExecutorService executor = Executors.newCachedThreadPool();

        long start = System.nanoTime();
        for(int i = 0; i < ATOMS_TO_ADD; i++) {
            executor.submit(runnable);
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        System.out.println("Strings left  " + testStrings.size());
        System.out.println("Parallel - " + ATOMS_TO_ADD + " atoms of 1mb uploaded in " + (System.nanoTime() - start) / 1000000000.0 + " seconds");
    }

    private void addAtom(ConcurrentLinkedQueue<String> testStrings) {
        try (InputStream stream = HelperTest.StringToInputStream(testStrings.poll());){

            AtomBuilder builder = new AtomBuilder().setInputStream(stream);
            Atom manifest = agent.addAtom(builder);
            assertNotNull(manifest.guid());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}