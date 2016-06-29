package uk.ac.standrews.cs.sos.model.datastore;

import uk.ac.standrews.cs.sos.interfaces.locations.Location;
import uk.ac.standrews.cs.sos.model.Configuration;
import uk.ac.standrews.cs.sos.model.locations.bundles.CacheLocationBundle;
import uk.ac.standrews.cs.sos.model.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.storage.interfaces.Storage;

import java.io.InputStream;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class StreamCache extends StreamStore {

    public StreamCache(Configuration configuration, Storage storage, InputStream inputStream) {
        super(configuration, storage, inputStream);
    }

    @Override
    protected LocationBundle getBundle(Location location) {
        return new CacheLocationBundle(location);
    }

}
