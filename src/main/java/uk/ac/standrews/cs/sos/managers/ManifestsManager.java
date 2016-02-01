package uk.ac.standrews.cs.sos.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import uk.ac.standrews.cs.sos.configurations.SeaConfiguration;
import uk.ac.standrews.cs.sos.deserializers.AssetManifestDeserializer;
import uk.ac.standrews.cs.sos.deserializers.AtomManifestDeserializer;
import uk.ac.standrews.cs.sos.deserializers.CompoundManifestDeserializer;
import uk.ac.standrews.cs.sos.exceptions.UnknownGUIDException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestMergeException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotMadeException;
import uk.ac.standrews.cs.sos.exceptions.manifest.UnknownManifestTypeException;
import uk.ac.standrews.cs.sos.exceptions.storage.DataStorageException;
import uk.ac.standrews.cs.sos.exceptions.storage.ManifestCacheException;
import uk.ac.standrews.cs.sos.exceptions.storage.ManifestPersistException;
import uk.ac.standrews.cs.sos.exceptions.storage.ManifestSaveException;
import uk.ac.standrews.cs.sos.model.implementations.components.manifests.*;
import uk.ac.standrews.cs.sos.model.implementations.utils.FileHelper;
import uk.ac.standrews.cs.sos.model.implementations.utils.GUID;
import uk.ac.standrews.cs.sos.model.implementations.utils.Location;
import uk.ac.standrews.cs.sos.model.interfaces.components.Manifest;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Manage the manifests of the sea of stuff.
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ManifestsManager {

    private final static String BACKUP_EXTENSION = ".bak";

    private SeaConfiguration configuration;
    private MemCache cache;
    private Gson gson;

    /**
     * Creates a manifests manager given a sea of stuff configuration object and
     * a policy for the sea of stuff. The configuration object is need to know the
     * locations for the manifests.
     *
     * @param configuration
     * @param cache
     */
    public ManifestsManager(SeaConfiguration configuration, MemCache cache) {
        this.configuration = configuration;
        this.cache = cache;

        configureGson();
    }

    /**
     * Adds a manifest to the sea of stuff.
     *
     * @param manifest
     */
    public void addManifest(Manifest manifest) throws ManifestSaveException {
        if (manifest.isValid()) {
            try {
                saveManifest(manifest);
            } catch (ManifestCacheException e) {
                throw new ManifestSaveException("Manifest could not be cached");
            } catch (ManifestPersistException e) {
                throw new ManifestSaveException("Manifest could not be persisted");
            } catch (UnknownGUIDException | ManifestMergeException e) {
                throw new ManifestSaveException("An equivalent manifest exists, but could not be fetched or merged");
            }
        } else {
            throw new ManifestSaveException("Manifest not valid");
        }
    }

    /**
     * Find a manifest in the sea of stuff given a GUID.
     *
     * @param guid
     * @return
     * @throws ManifestException
     */
    public Manifest findManifest(GUID guid) throws ManifestException {
        if (guid == null) {
            throw new ManifestException();
        }

        Manifest manifest;
        try {
            manifest = getManifestFromFile(guid);
            // TODO - if manifest is not found, then get it from one of the registered services.
        } catch (UnknownGUIDException ex) {
            throw new ManifestException();
        }

        return manifest;
    }

    private void configureGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        registerGSonTypeAdapters(gsonBuilder);
        gson = gsonBuilder.create();
    }

    private void registerGSonTypeAdapters(GsonBuilder builder) {
        builder.registerTypeAdapter(AtomManifest.class, new AtomManifestDeserializer());
        builder.registerTypeAdapter(CompoundManifest.class, new CompoundManifestDeserializer());
        builder.registerTypeAdapter(AssetManifest.class, new AssetManifestDeserializer());
    }

    private Manifest getManifestFromFile(GUID guid) throws UnknownGUIDException {
        Manifest manifest;
        final String path = getManifestPath(guid);
        try {
            String manifestData = readManifestFromFile(path);

            JsonObject obj = gson.fromJson(manifestData, JsonObject.class);
            String type = obj.get(ManifestConstants.KEY_TYPE).getAsString();

            manifest = constructManifestFromJson(guid, type, manifestData);
        } catch (FileNotFoundException | UnknownManifestTypeException | ManifestNotMadeException e) {
            throw new UnknownGUIDException();
        }

        return manifest;
    }

    private String readManifestFromFile(String path) throws FileNotFoundException {
        // http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
        Scanner scanner = new Scanner(new File(path));
        String text = scanner.useDelimiter("\\A").next();
        scanner.close();
        return text;
    }

    private Manifest constructManifestFromJson(GUID guid, String type, String manifestData) throws UnknownManifestTypeException, ManifestNotMadeException {
        Manifest manifest;
        try {
            switch (type) {
                case ManifestConstants.ATOM:
                    manifest = gson.fromJson(manifestData, AtomManifest.class);
                    ((AtomManifest) manifest).setContentGUID(guid);
                    break;
                case ManifestConstants.COMPOUND:
                    manifest = gson.fromJson(manifestData, CompoundManifest.class);
                    break;
                case ManifestConstants.ASSET:
                    manifest = gson.fromJson(manifestData, AssetManifest.class);
                    break;
                default:
                    throw new UnknownManifestTypeException();
            }
        } catch (JsonSyntaxException e) {
            throw new ManifestNotMadeException();
        }
        return manifest;
    }

    // if atom-manifest, check if it exists already
    // then merge and save
    // otherwise just save
    private void saveManifest(Manifest manifest) throws ManifestCacheException, ManifestPersistException, UnknownGUIDException, ManifestMergeException {
        GUID guid = manifest.getContentGUID();

        if (manifest.getManifestType().equals(ManifestConstants.ATOM) &&
                manifestExistsInLocalStorage(manifest.getContentGUID())) {

            String backupPath = backupManifest(manifest);

            Manifest existingManifest = getManifestFromFile(guid);
            manifest = mergeManifests((AtomManifest) existingManifest, (AtomManifest) manifest);

            FileHelper.deleteFile(backupPath);
            saveToFile(manifest);
            FileHelper.deleteFile(backupPath + BACKUP_EXTENSION);
        } else {
            saveToFile(manifest);
        }
        cacheManifest(manifest);
    }

    private String backupManifest(Manifest manifest) throws ManifestMergeException {
        GUID guidUsedToStoreManifest = getGUIDUsedToStoreManifest(manifest);
        String originalPath = getManifestPath(guidUsedToStoreManifest);
        try {
            FileHelper.copyToFile(new Location(originalPath).getSource(),
                    new Location(originalPath + BACKUP_EXTENSION));
        } catch (IOException | URISyntaxException e) {
            throw new ManifestMergeException();
        }
        return originalPath;
    }

    private GUID getGUIDUsedToStoreManifest(Manifest manifest) {
        GUID guid;
        if (manifest.getManifestType().equals(ManifestConstants.ASSET)) {
            guid = ((AssetManifest) manifest).getVersionGUID();
        } else {
            guid = manifest.getContentGUID();
        }
        return guid;
    }

    private void saveToFile(Manifest manifest) throws ManifestPersistException {
        JsonObject manifestJSON = manifest.toJSON();

        // Remove content guid and use that for the manifest file name
        String guid = "";
        String type = manifest.getManifestType();
        if (type.equals(ManifestConstants.ASSET)) {
            guid = manifestJSON.remove(ManifestConstants.KEY_VERSION).getAsString();
        } else {
            guid = manifestJSON.remove(ManifestConstants.KEY_CONTENT_GUID).getAsString();
        }

        final String path = getManifestPath(guid);
        File file = new File(path);

        // if filepath doesn't exists, then create it
        File parent = file.getParentFile();
        if(!parent.exists() && !parent.mkdirs()){
            throw new IllegalStateException("Couldn't create dir: " + parent); // TODO - custom exception
        }

        if (file.exists())
            return;

        try (FileWriter fileWriter = new FileWriter(file);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            Gson gson = new Gson();
            String json = gson.toJson(manifestJSON);
            bufferedWriter.write(json);
        } catch (IOException ioe) {
            throw new ManifestPersistException();
        } catch (Exception e) {
            throw new ManifestPersistException();
        } finally {
            file.setReadOnly();
        }
    }

    private void cacheManifest(Manifest manifest) throws ManifestCacheException {
        try {
            cache.addManifest(manifest);
        } catch (UnknownManifestTypeException e) {
            throw new ManifestCacheException("Manifest could not be cached");
        }
    }

    private String getManifestPath(GUID guid) {
        return getManifestPath(guid.toString());
    }

    private String getManifestPath(String guid) {
        return configuration.getLocalManifestsLocation() + normaliseGUID(guid);
    }

    private String normaliseGUID(String guid) {
        return guid.substring(0, 2) + "/" + guid.substring(2) + ".json";
    }

    private Manifest mergeManifests(AtomManifest first, AtomManifest second) throws ManifestMergeException {
        Collection<Location> locations = new HashSet<>();
        locations.addAll(first.getLocations());
        locations.addAll(second.getLocations());

        Manifest manifest;
        try {
            manifest = ManifestFactory.createAtomManifest(configuration, locations);
        } catch (ManifestNotMadeException | DataStorageException e) {
            throw new ManifestMergeException();
        }
        return manifest;
    }

    private boolean manifestExistsInLocalStorage(GUID guid) {
        String path = getManifestPath(guid);
        return new File(path).exists();
    }
}