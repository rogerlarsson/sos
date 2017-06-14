package uk.ac.standrews.cs.sos.impl.manifests.directory;

import uk.ac.standrews.cs.GUIDFactory;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.IKey;
import uk.ac.standrews.cs.castore.data.Data;
import uk.ac.standrews.cs.castore.data.StringData;
import uk.ac.standrews.cs.castore.exceptions.DataException;
import uk.ac.standrews.cs.castore.exceptions.PersistenceException;
import uk.ac.standrews.cs.castore.exceptions.RenameException;
import uk.ac.standrews.cs.castore.interfaces.IDirectory;
import uk.ac.standrews.cs.castore.interfaces.IFile;
import uk.ac.standrews.cs.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.impl.InvalidID;
import uk.ac.standrews.cs.sos.exceptions.manifest.*;
import uk.ac.standrews.cs.sos.exceptions.storage.DataStorageException;
import uk.ac.standrews.cs.sos.impl.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.impl.manifests.ManifestFactory;
import uk.ac.standrews.cs.sos.impl.node.LocalStorage;
import uk.ac.standrews.cs.sos.model.*;
import uk.ac.standrews.cs.sos.utils.FileUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * IDirectory for the manifests stored locally to this node
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class LocalManifestsDirectory extends AbstractManifestsDirectory {

    private final static String BACKUP_EXTENSION = ".bak";
    private final static String HEAD_TAG = "HEAD-";
    private final static String CURRENT_TAG = "CURRENT-";

    final private LocalStorage localStorage;

    /**
     * Creates a manifests directory given a sea of stuff configuration object and
     * a policy for the sea of stuff. The configuration object is need to know the
     * locations for the manifests.
     *
     * @param localStorage local storage used by this node
     */
    public LocalManifestsDirectory(LocalStorage localStorage) {
        this.localStorage = localStorage;
    }

    /**
     * Adds a manifest to the sea of stuff.
     *
     * @param manifest to be added to the sea of stuff
     */
    @Override
    public void addManifest(Manifest manifest) throws ManifestPersistException {

        try {
            if (manifest.isValid()) {
                saveManifest(manifest);
            } else {
                throw new ManifestPersistException("Manifest not valid");
            }
        } catch (ManifestsDirectoryException e) {
            throw new ManifestPersistException("Unable to save manifest " + manifest);
        }
    }

    /**
     * Find a manifest in the SOS given a GUID.
     *
     * @param guid of the manifest to be found
     * @return Manifest
     * @throws ManifestNotFoundException
     */
    @Override
    public Manifest findManifest(IGUID guid) throws ManifestNotFoundException {
        if (guid == null || guid.isInvalid()) {
            throw new ManifestNotFoundException("Cannot find manifest for null guid");
        }

        return getManifestFromGUID(guid);
    }

    @Override
    public Set<IGUID> getHeads(IGUID invariant) throws HEADNotFoundException {

        try {
            IDirectory manifestsDir = localStorage.getManifestsDirectory();
            String filename = HEAD_TAG + invariant.toString();

            // Make sure that the HEAD file exists
            IFile file = localStorage.createFile(manifestsDir, filename);
            if (!file.exists()) {
                throw new HEADNotFoundException();
            }

            String content = FileUtils.FileContent(localStorage, manifestsDir, filename);
            List<String> versions = content.isEmpty() ? new LinkedList<>() : Arrays.asList(content.split("\n"));

            Set<IGUID> versionsRefs =  versions.stream()
                    .map(v -> {
                        try {
                            return GUIDFactory.recreateGUID(v);
                        } catch (GUIDGenerationException e) {
                            return new InvalidID();
                        }
                    }).collect(Collectors.toSet());

            return versionsRefs;

        } catch (DataException | DataStorageException e) {
            throw new HEADNotFoundException();
        }
    }

    @Override
    public IGUID getCurrent(Role role, IGUID invariant) throws CURRENTNotFoundException {

        try {

            IDirectory manifestsDir = localStorage.getManifestsDirectory();
            String filename = CURRENT_TAG + invariant.toString() + "-ROLE-" + role.guid();

            String guid = FileUtils.FileContent(localStorage, manifestsDir, filename);
            return GUIDFactory.recreateGUID(guid);

        } catch (DataStorageException | DataException | GUIDGenerationException e) {
            throw new CURRENTNotFoundException();
        }

    }

    @Override
    public void setCurrent(Role role, Version version) {

        try {

            IDirectory manifestsDir = localStorage.getManifestsDirectory();
            String filename = CURRENT_TAG + version.getInvariantGUID().toString() + "-ROLE-" + role.guid();

            IFile file = FileUtils.CreateFileWithContent(localStorage, manifestsDir, filename, version.getVersionGUID().toString());
            file.persist();

        } catch (DataStorageException | PersistenceException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void flush() {}

    private Manifest getManifestFromGUID(IGUID guid) throws ManifestNotFoundException {
        IFile manifestFile = getManifestFile(guid);

        return FileUtils.ManifestFromFile(manifestFile);
    }

    private void saveManifest(Manifest manifest) throws ManifestsDirectoryException {

        try {
            IGUID manifestFileGUID = manifest.guid();

            boolean isAtomManifest = manifest.getType().equals(ManifestType.ATOM);
            boolean manifestExists = manifestExistsInStorage(manifestFileGUID);

            if (isAtomManifest && manifestExists) {
                saveExistingAtomManifest(manifest);
            } else if (manifestExists) {
                saveExistingManifest(manifestFileGUID, manifest);
            } else {
                saveToFile(manifest);
            }

        } catch (ManifestNotFoundException e) {
            throw new ManifestsDirectoryException(e);
        }

    }

    private void saveExistingAtomManifest(Manifest manifest) throws ManifestNotFoundException, ManifestsDirectoryException {
        IGUID guid = manifest.guid();
        Manifest existingManifest = getManifestFromGUID(guid);
        mergeAtomManifestAndSave(existingManifest, manifest);
    }

    private void saveExistingManifest(IGUID manifestFileGUID, Manifest manifest) throws ManifestsDirectoryException, ManifestNotFoundException {
        IFile manifestFile = getManifestFile(manifestFileGUID);
        IFile backupFile = backupManifest(manifest);
        FileUtils.DeleteFile(manifestFile);

        saveToFile(manifest);

        FileUtils.DeleteFile(backupFile);
    }

    private void mergeAtomManifestAndSave(Manifest existingManifest, Manifest manifest) throws ManifestsDirectoryException {
        IGUID guid = manifest.guid();

        try {
            IFile manifestFile = getManifestFile(guid);

            IFile backupFile = backupManifest(existingManifest);

            if (!existingManifest.equals(manifest)) {
                manifest = mergeManifests(guid, (Atom) existingManifest, (Atom) manifest);
                FileUtils.DeleteFile(manifestFile);
                saveToFile(manifest);
            }

            FileUtils.DeleteFile(backupFile);
        } catch (ManifestNotFoundException e) {
            throw new ManifestsDirectoryException("Manifests " + existingManifest.guid().toString() + " and " + manifest.guid().toString() + "could not be merged", e);
        }

    }

    private IFile backupManifest(Manifest manifest) throws ManifestsDirectoryException {

        try {
            IGUID manifestGUID = manifest.guid();
            IFile manifestFileToBackup = getManifestFile(manifestGUID);

            IDirectory manifestsDirectory = localStorage.getManifestsDirectory();
            IFile backupManifest = localStorage.createFile(manifestsDirectory,
                    manifestFileToBackup.getName() + BACKUP_EXTENSION, manifestFileToBackup.getData());
            backupManifest.persist();

            return backupManifest;
        } catch (ManifestNotFoundException | DataStorageException | DataException | PersistenceException e) {
            throw new ManifestsDirectoryException("Manifest could not be backed up ", e);
        }

    }

    private void saveToFile(Manifest manifest) throws ManifestsDirectoryException {

        try {
            String manifestGUID = manifest.guid().toString();
            IFile manifestTempFile = getManifestTempFile(manifestGUID);

            Data manifestData = new StringData(manifest.toString());
            manifestTempFile.setData(manifestData);
            manifestTempFile.persist();

            manifestTempFile.rename(manifestGUID + FileUtils.JSON_EXTENSION);

        } catch (PersistenceException | DataException | DataStorageException | RenameException e) {
            throw new ManifestsDirectoryException(e);
        }
    }

    private IFile getManifestFile(IGUID guid) throws ManifestNotFoundException {
        try {
            return getManifestFile(guid.toString());
        } catch (DataStorageException e) {
            throw new ManifestNotFoundException("Unable to find manifest file for GUID: " + guid);
        }
    }

    private IFile getManifestFile(String guid) throws DataStorageException {
        IDirectory manifestsDir = localStorage.getManifestsDirectory();

        return FileUtils.CreateFile(localStorage, manifestsDir, guid, FileUtils.JSON_EXTENSION);
    }

    private IFile getManifestTempFile(String guid) throws DataStorageException {
        IDirectory manifestsDir = localStorage.getManifestsDirectory();

        return FileUtils.CreateTempFile(localStorage, manifestsDir, guid);
    }


    private Manifest mergeManifests(IGUID guid, Atom first, Atom second) {
        HashSet<LocationBundle> locations = new HashSet<>();
        locations.addAll(first.getLocations());
        locations.addAll(second.getLocations());

        return ManifestFactory.createAtomManifest(guid, locations);
    }

    private boolean manifestExistsInStorage(IGUID guid) throws ManifestNotFoundException {
        IFile manifest = getManifestFile(guid);
        return manifest.exists();
    }

    public void advanceHead(IGUID invariant, IGUID version) {

        appendHead(invariant, version);
    }

    public void advanceHead(IGUID invariant, Set<IGUID> previousVersions, IGUID newVersion) {

        try {

            IDirectory manifestsDir = localStorage.getManifestsDirectory();
            String filename = HEAD_TAG + invariant.toString();

            Set<String> previousVersionsStrings = previousVersions.stream()
                    .map(IKey::toString)
                    .collect(Collectors.toSet());

            appendHead(invariant, newVersion);
            removeHeads(invariant, previousVersionsStrings);


        } catch (DataStorageException e) {
            e.printStackTrace();
        }
    }

    /**
     * Append a version to the HEAD file for the specified invariant
     * @param invariant
     * @param version
     */
    private void appendHead(IGUID invariant, IGUID version) {

        try {

            IDirectory manifestsDir = localStorage.getManifestsDirectory();
            String filename = HEAD_TAG + invariant.toString();

            // Make sure that the HEAD file exists
            IFile file = localStorage.createFile(manifestsDir, filename);
            if (!file.exists()) {
                file.persist();
            }

            String newContent;
            try {
                String content = FileUtils.FileContent(localStorage, manifestsDir, filename);
                Set<String> versions = content.isEmpty() ? new LinkedHashSet<>() : new LinkedHashSet<>(Arrays.asList(content.split("\n")));
                versions.add(version.toString());

                newContent = versions.stream().collect(Collectors.joining( "\n" ));

            } catch (DataStorageException | DataException e) {
                newContent = version.toString();
            }

            IFile fileWithContent = FileUtils.CreateFileWithContent(localStorage, manifestsDir, filename, newContent);
            fileWithContent.persist();

        } catch (DataStorageException | PersistenceException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove the specified versions from the HEAD file for the specified invariant
     * @param invariant
     * @param versionsToRemove
     */
    private void removeHeads(IGUID invariant, Set<String> versionsToRemove) {

        try {

            IDirectory manifestsDir = localStorage.getManifestsDirectory();
            String filename = HEAD_TAG + invariant.toString();

            String content = FileUtils.FileContent(localStorage, manifestsDir, filename);
            Set<String> versions = content.isEmpty() ? new LinkedHashSet<>() : new LinkedHashSet<>(Arrays.asList(content.split("\n")));
            versions.removeAll(versionsToRemove);

            String newContent = versions.stream().collect(Collectors.joining( "\n" ));

            IFile fileWithContent = FileUtils.CreateFileWithContent(localStorage, manifestsDir, filename, newContent);
            fileWithContent.persist();

        } catch (DataException | DataStorageException | PersistenceException e) {
            e.printStackTrace();
        }
    }

}
