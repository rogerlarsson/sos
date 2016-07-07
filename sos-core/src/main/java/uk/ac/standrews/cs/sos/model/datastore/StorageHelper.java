package uk.ac.standrews.cs.sos.model.datastore;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.exceptions.SourceLocationException;
import uk.ac.standrews.cs.sos.interfaces.locations.Location;
import uk.ac.standrews.cs.sos.model.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.model.storage.InternalStorage;
import uk.ac.standrews.cs.storage.exceptions.StorageException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class StorageHelper {

    /**
     * Return an InputStream for the given location.
     * The method calling this function should ensure that the stream is closed.
     *
     * @param location
     * @return
     * @throws SourceLocationException
     */
    public static InputStream getInputStreamFromLocation(Location location) throws SourceLocationException {
        InputStream stream;
        try {
            stream = location.getSource();
        } catch (IOException e) {
            throw new SourceLocationException(location.toString() + " " + e);
        }

        return stream;
    }

    public static IGUID cacheAtomAndUpdateLocationBundles(InternalStorage storage, Location location, Collection<LocationBundle> bundles) throws StorageException {
        Store cache = new LocationCache(storage, location);

        return storeAtomAndUpdateLocationBundles(cache, bundles);
    }

    public static IGUID cacheAtomAndUpdateLocationBundles(InternalStorage storage, InputStream inputStream, Collection<LocationBundle> bundles) throws StorageException {
        Store cache = new StreamCache(storage, inputStream);

        return storeAtomAndUpdateLocationBundles(cache, bundles);
    }

    public static IGUID persistAtomAndUpdateLocationBundles(InternalStorage storage, Location location, Collection<LocationBundle> bundles) throws StorageException {
        Store cache = new LocationPersist(storage, location);

        return storeAtomAndUpdateLocationBundles(cache, bundles);
    }

    public static IGUID persistAtomAndUpdateLocationBundles(InternalStorage storage, InputStream inputStream, Collection<LocationBundle> bundles) throws StorageException {
        Store cache = new StreamPersist(storage, inputStream);

        return storeAtomAndUpdateLocationBundles(cache, bundles);
    }

    private static IGUID storeAtomAndUpdateLocationBundles(Store store, Collection<LocationBundle> bundles) throws StorageException {
        StorageManager storageManager = new StorageManager(store);
        IGUID guid = storageManager.storeAtom();
        if (bundles!= null && guid != null) {
            bundles.add(storageManager.getLocationBundle());
        }

        return guid;
    }

}
