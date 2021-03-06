/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module filesystem.
 *
 * filesystem is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * filesystem is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with filesystem. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.sos.filesystem;

import uk.ac.standrews.cs.fs.exceptions.FileSystemCreationException;
import uk.ac.standrews.cs.fs.interfaces.IFileSystem;
import uk.ac.standrews.cs.fs.interfaces.IFileSystemFactory;
import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.guid.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.logger.LEVEL;
import uk.ac.standrews.cs.sos.exceptions.ServiceException;
import uk.ac.standrews.cs.sos.filesystem.impl.SOSFileSystem;
import uk.ac.standrews.cs.sos.filesystem.utils.AssetObject;
import uk.ac.standrews.cs.sos.impl.datamodel.builders.CompoundBuilder;
import uk.ac.standrews.cs.sos.impl.datamodel.builders.VersionBuilder;
import uk.ac.standrews.cs.sos.model.Compound;
import uk.ac.standrews.cs.sos.model.CompoundType;
import uk.ac.standrews.cs.sos.model.Version;
import uk.ac.standrews.cs.sos.services.Agent;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import java.io.*;
import java.util.Collections;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSFileSystemFactory implements IFileSystemFactory {

    public static final String WEBDAV_PATH = System.getProperty("user.home") + "/webdav/";
    public static final String WEBDAV_CURRENT_PATH = WEBDAV_PATH + "current/";

    private Agent agent;
    private IGUID rootGUID;

    private static AssetObject assetObject;

    public SOSFileSystemFactory(Agent agent, IGUID rootGUID) {
        this(rootGUID);

        this.agent = agent;
    }

    public SOSFileSystemFactory(IGUID rootGUID) {
        this.rootGUID = rootGUID;
    }

    @Override
    public IFileSystem makeFileSystem() throws FileSystemCreationException {
        if (agent != null && rootGUID != null) {
            return makeFileSystem(agent);
        }

        throw new FileSystemCreationException();
    }

    public IFileSystem makeFileSystem(Agent agent) throws FileSystemCreationException {
        SOS_LOG.log(LEVEL.INFO, "WEBDAV - Factory - Making the CreateFile System");

        // Create directories for Webdav info
        File dir = new File(WEBDAV_CURRENT_PATH);
        dir.mkdirs();

        if (agent != null) {
            Version rootVersion = getRoot(agent, rootGUID);
            if (rootVersion == null) {
                rootVersion = createRoot(agent);
            }

            return new SOSFileSystem(agent, rootVersion);
        } else {
            SOS_LOG.log(LEVEL.ERROR, "WEBDAV - Unable to create file system");
            return null;
        }
    }

    private Version createRoot(Agent sos) throws FileSystemCreationException {

        try {
            Compound compound = createRootCompound(sos);
            IGUID compoundGUID = compound.guid();
            VersionBuilder builder = new VersionBuilder(compoundGUID).setInvariant(rootGUID);

            Version retval = sos.addVersion(builder);
            IGUID versionGUID = retval.version();

            WriteCurrentVersion(rootGUID, versionGUID);

            return retval;
        } catch (ServiceException | FileNotFoundException e) {
            throw new FileSystemCreationException("WEBDAV - Unable to create Root Asset");
        }

    }

    public static Version getRoot(Agent sos, IGUID root) {
        Version retval = null;
        try {
            if (assetObject == null) {
                IGUID version = ReadCurrentVersion(root);
                assetObject = new AssetObject(root, version);
            }

            retval = (Version) sos.getManifest(assetObject.getVersion());

        } catch (GUIDGenerationException | IOException | ServiceException e) { /* Ignore */ }

        return retval;
    }

    private Compound createRootCompound(Agent sos) throws ServiceException {

        CompoundBuilder compoundBuilder = new CompoundBuilder()
                .setType(CompoundType.COLLECTION)
                .setContents(Collections.emptySet());

        return sos.addCompound(compoundBuilder);
    }

    public static void WriteCurrentVersion(IGUID invariant, IGUID version) throws FileNotFoundException {

        assetObject = new AssetObject(invariant, version);

        try (PrintWriter out = new PrintWriter(WEBDAV_CURRENT_PATH + assetObject.getInvariant())){
            out.println(assetObject.getVersion());
        }
    }

    private static IGUID ReadCurrentVersion(IGUID invariant) throws IOException, GUIDGenerationException {

        File current = new File(WEBDAV_CURRENT_PATH + invariant);
        if (!current.exists()) {
            throw new IOException();
        }

        try(BufferedReader Buff = new BufferedReader(new FileReader(WEBDAV_CURRENT_PATH + invariant))) {
            String text = Buff.readLine();
            return GUIDFactory.recreateGUID(text);
        }
    }
}
