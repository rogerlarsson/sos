/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module core.
 *
 * core is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * core is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with core. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.sos.impl.usro;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.io.input.NullInputStream;
import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.logger.LEVEL;
import uk.ac.standrews.cs.sos.exceptions.crypto.SignatureException;
import uk.ac.standrews.cs.sos.impl.manifest.BasicManifest;
import uk.ac.standrews.cs.sos.impl.node.SOSLocalNode;
import uk.ac.standrews.cs.sos.model.ManifestType;
import uk.ac.standrews.cs.sos.model.User;
import uk.ac.standrews.cs.sos.utils.JSONHelper;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;
import uk.ac.standrews.cs.utilities.crypto.CryptoException;
import uk.ac.standrews.cs.utilities.crypto.DigitalSignature;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static uk.ac.standrews.cs.sos.constants.Internals.GUID_ALGORITHM;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class UserImpl extends BasicManifest implements User {

    private IGUID guid;
    private String name;
    protected String keysFolder; // TODO - manage from InternalStore?

    protected PublicKey d_publicKey;
    private PrivateKey signaturePrivateKey;

    public UserImpl(String name) throws SignatureException {
        this(GUIDFactory.generateRandomGUID(GUID_ALGORITHM), name);
    }

    UserImpl(IGUID guid, String name) throws SignatureException {
        this(ManifestType.USER, guid, name);
    }

    UserImpl(ManifestType manifestType, IGUID guid, String name) throws SignatureException {
        super(manifestType);

        this.guid = guid;
        this.name = name;
        this.keysFolder = SOSLocalNode.settings.getKeys().getLocation();

        manageSignatureKeys(false);
    }

    public UserImpl(IGUID guid, String name, PublicKey d_publicKey) throws SignatureException {
        this(ManifestType.USER, guid, name, d_publicKey);
    }

    UserImpl(ManifestType manifestType, IGUID guid, String name, PublicKey d_publicKey) throws SignatureException {
        super(manifestType);

        this.guid = guid;
        this.name = name;
        this.keysFolder = SOSLocalNode.settings.getKeys().getLocation();
        this.d_publicKey = d_publicKey;

        manageSignatureKeys(true);
    }

    public InputStream contentToHash() {
        return new NullInputStream(0); // GUID is randomly generated
    }

    @Override
    public IGUID guid() {
        return guid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PublicKey getSignaturePublicKey() {
        return d_publicKey;
    }

    @Override
    public String sign(String text) throws SignatureException {
        try {
            return DigitalSignature.sign64(signaturePrivateKey, text);
        } catch (CryptoException e) {
            throw new SignatureException(e);
        }
    }

    @Override
    public boolean verify(String text, String signatureToVerify) throws SignatureException {

        try {
            return DigitalSignature.verify64(d_publicKey, text, signatureToVerify);
        } catch (CryptoException e) {
            throw new SignatureException(e);
        }
    }

    @Override
    public boolean hasPrivateKeys() {

        if (signaturePrivateKey == null) return false;

        try {
            return DigitalSignature.verifyKeyPair(d_publicKey, signaturePrivateKey);
        } catch (CryptoException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        try {
            return JSONHelper.jsonObjMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            SOS_LOG.log(LEVEL.ERROR, "Unable to generate JSON for user/role " + guid() + " instanceof " + this.getClass().getName());
            return "";
        }
    }

    /**
     * Attempt to load the private key and the certificate for the digital signature.
     * If keys cannot be loaded, then generate them and save to disk
     *
     * @param loadOnly if true, it will try to load the keys, but not to generate them
     * @throws SignatureException if an error occurred while managing the keys
     */
    private void manageSignatureKeys(boolean loadOnly) throws SignatureException {

        try {
            File publicKeyFile = new File(keysFolder + guid().toMultiHash() + DigitalSignature.CERTIFICATE_EXTENSION);
            if (d_publicKey == null && publicKeyFile.exists()) {
                d_publicKey = DigitalSignature.getCertificate(publicKeyFile.toPath());
            }

            File privateKeyFile = new File(keysFolder + guid().toMultiHash() + DigitalSignature.PRIVATE_KEY_EXTENSION);
            if (signaturePrivateKey == null && privateKeyFile.exists()) {
                signaturePrivateKey = DigitalSignature.getPrivateKey(privateKeyFile.toPath());
            }


            if (!loadOnly && d_publicKey == null && signaturePrivateKey == null) {

                KeyPair keys = DigitalSignature.generateKeys();
                d_publicKey = keys.getPublic();
                signaturePrivateKey = keys.getPrivate();

                DigitalSignature.persist(keys, Paths.get(keysFolder + guid().toMultiHash()), Paths.get(keysFolder + guid().toMultiHash()));
            }

        } catch (CryptoException e) {
            throw new SignatureException(e);
        }
    }

}
