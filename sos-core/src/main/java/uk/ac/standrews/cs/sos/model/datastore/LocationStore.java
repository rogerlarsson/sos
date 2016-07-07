package uk.ac.standrews.cs.sos.model.datastore;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.exceptions.SourceLocationException;
import uk.ac.standrews.cs.sos.interfaces.locations.Location;
import uk.ac.standrews.cs.sos.model.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.model.storage.InternalStorage;
import uk.ac.standrews.cs.storage.data.InputStreamData;
import uk.ac.standrews.cs.storage.exceptions.StorageException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public abstract class LocationStore extends CommonStore {

    private Location origin;
    private LocationBundle locationBundle;

    public LocationStore(InternalStorage storage, Location location) {
        super(storage);
        this.origin = location;
    }

    @Override
    public IGUID store() throws StorageException {
        IGUID guid;
        if (origin == null) {
            throw new StorageException();
        }

        try {
            guid = generateGUID(origin);
            if (guid == null) {
                return null;
            }

            storeData(origin, guid);
            Location location = getLocation(guid);
            locationBundle = getBundle(location);
        } catch (GUIDGenerationException | SourceLocationException e) {
            throw new StorageException();
        }

        return guid;
    }

    @Override
    public LocationBundle getLocationBundle() {
        return locationBundle;
    }

    private void storeData(Location location, IGUID guid) throws StorageException {

        try (InputStream dataStream =
                     StorageHelper.getInputStreamFromLocation(location)) {

            storeData(guid, new InputStreamData((dataStream)));
        } catch (SourceLocationException | IOException e) {
            throw new StorageException();
        }
    }

}
