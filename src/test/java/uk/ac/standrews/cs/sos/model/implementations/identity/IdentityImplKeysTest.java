package uk.ac.standrews.cs.sos.model.implementations.identity;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.ac.standrews.cs.sos.configurations.SeaConfiguration;
import uk.ac.standrews.cs.sos.configurations.TestConfiguration;
import uk.ac.standrews.cs.sos.exceptions.identity.DecryptionException;
import uk.ac.standrews.cs.sos.exceptions.identity.EncryptionException;
import uk.ac.standrews.cs.sos.exceptions.identity.KeyGenerationException;
import uk.ac.standrews.cs.sos.exceptions.identity.KeyLoadedException;
import uk.ac.standrews.cs.sos.model.interfaces.identity.Identity;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class IdentityImplKeysTest {

    private SeaConfiguration configuration;

    @BeforeMethod
    public void setup() {
        configuration = new TestConfiguration();

        // Delete any left over keys from past
        deleteKeys(configuration);
    }

    @AfterMethod
    public void tearDown() {
        deleteKeys(configuration);
    }

    @Test
    public void testPublicKeyExists() throws KeyGenerationException, KeyLoadedException {
        Identity identity = new IdentityImpl(configuration);
        assertNotNull(identity.getPublicKey());
    }

    @Test
    public void testPublicKeyLoadedExists() throws KeyGenerationException, KeyLoadedException {
        Identity identity = new IdentityImpl(configuration);
        assertNotNull(identity.getPublicKey());

        identity = new IdentityImpl(configuration);
        assertNotNull(identity.getPublicKey());
    }

    @Test
    public void testEncryptDecrypt() throws Exception, EncryptionException, DecryptionException {
        Identity identity = new IdentityImpl(configuration);

        byte[] encrypted = identity.encrypt("hello");
        String result = identity.decrypt(encrypted);
        assertEquals(result, "hello");
    }

    @Test
    public void testLoadedKeyEncryptDecrypt() throws Exception, EncryptionException, DecryptionException {
        Identity identity = new IdentityImpl(configuration);
        Identity identityLoaded = new IdentityImpl(configuration);

        byte[] encrypted = identity.encrypt("hello");
        String result = identityLoaded.decrypt(encrypted);
        assertEquals(result, "hello");
    }

    private void deleteKeys(SeaConfiguration configuration) {
        File privKey = new File(configuration.getIdentityPaths()[0]);
        File pubkey = new File(configuration.getIdentityPaths()[1]);

        privKey.delete();
        pubkey.delete();
    }

}