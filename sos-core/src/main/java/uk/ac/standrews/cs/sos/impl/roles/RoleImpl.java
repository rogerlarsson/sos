package uk.ac.standrews.cs.sos.impl.roles;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.exceptions.crypto.DecryptionException;
import uk.ac.standrews.cs.sos.exceptions.crypto.EncryptionException;
import uk.ac.standrews.cs.sos.exceptions.crypto.KeyGenerationException;
import uk.ac.standrews.cs.sos.exceptions.crypto.KeyLoadedException;
import uk.ac.standrews.cs.sos.json.RoleDeserializer;
import uk.ac.standrews.cs.sos.json.RoleSerializer;
import uk.ac.standrews.cs.sos.model.Role;
import uk.ac.standrews.cs.sos.model.User;
import uk.ac.standrews.cs.sos.utils.crypto.SignatureCrypto;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
@JsonSerialize(using = RoleSerializer.class)
@JsonDeserialize(using = RoleDeserializer.class)
public class RoleImpl implements Role { // TODO - this will take over the identity class

    private IGUID userGUID;
    private IGUID roleGUID;
    private String name;

    private PublicKey pubkey;
    private SignatureCrypto signature;
    private File privateKeyFile;
    private File publicKeyFile;
    private final static String KEYS_FOLDER = System.getProperty("user.home") + "/sos/keys/";

    // TODO - use RSA to encrypt content?


    /**
     * TODO - create role starting from the user
     *
     * keys are either created and persisted, or loaded
     *
     * @param guid
     * @param name
     * @throws KeyGenerationException
     * @throws KeyLoadedException
     */
    public RoleImpl(User user, IGUID guid, String name) throws KeyGenerationException, KeyLoadedException {
        this.userGUID = user.guid();
        this.roleGUID = guid;
        this.name = name;

        signature = new SignatureCrypto();

        privateKeyFile = new File(KEYS_FOLDER + roleGUID + ".pem");
        publicKeyFile = new File(KEYS_FOLDER + roleGUID + "-pub.pem");
        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            generateKeys();
        } else {
            loadKeys();
        }

        this.pubkey = signature.getPublicKey();

        // TODO - generate SIGNATURE for this role using the user
    }

    @Override
    public IGUID guid() {
        return roleGUID;
    }

    @Override
    public IGUID getUser() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String algorithm() {
        return null;
    }

    @Override
    public PublicKey getPubkey() {
        return pubkey;
    }

    @Override
    public String getSignature() {
        return null;
    }

    @Override
    public String sign(String text) throws EncryptionException {
        return signature.sign64(text);
    }

    @Override
    public boolean verify(String text, String signatureToVerify) throws DecryptionException {
        return signature.verify64(text, signatureToVerify);
    }

    /**
     * Generate key which contains a pair of private and public key.
     * Store the set of keys in appropriate files based on the specified configuration.
     */
    private void generateKeys() throws KeyGenerationException {
        signature.generateKeys();

        try {
            signature.saveToFile(privateKeyFile, publicKeyFile);
        } catch (IOException e) {
            throw new KeyGenerationException("Unable to save keys");
        }
    }

    private void loadKeys() throws KeyLoadedException {
        signature.loadKeys(privateKeyFile, publicKeyFile);
    }

}