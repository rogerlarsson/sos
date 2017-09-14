package uk.ac.standrews.cs.sos.impl.services;

import uk.ac.standrews.cs.castore.data.Data;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataException;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataPersistException;
import uk.ac.standrews.cs.sos.interfaces.metadata.MetadataEngine;
import uk.ac.standrews.cs.sos.model.Metadata;
import uk.ac.standrews.cs.sos.model.NodesCollection;
import uk.ac.standrews.cs.sos.services.DataDiscoveryService;
import uk.ac.standrews.cs.sos.services.MetadataService;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSMetadataService implements MetadataService {

    private DataDiscoveryService dataDiscoveryService;
    private MetadataEngine engine;

    public SOSMetadataService(MetadataEngine metadataEngine, DataDiscoveryService dataDiscoveryService) {
        this.engine = metadataEngine;
        this.dataDiscoveryService = dataDiscoveryService;
    }

    @Override
    public Metadata processMetadata(Data data) throws MetadataException {

        return engine.processData(data);
    }

    @Override
    public void addMetadata(Metadata metadata) throws MetadataPersistException {
        try {
            dataDiscoveryService.addManifest(metadata);
        } catch (ManifestPersistException e) {
            throw new MetadataPersistException(e);
        }
    }

    @Override
    public Metadata getMetadata(IGUID guid) throws MetadataNotFoundException {
        try {
            return (Metadata) dataDiscoveryService.getManifest(guid);
        } catch (ManifestNotFoundException e) {
            throw new MetadataNotFoundException("Unable to find metadata");
        }
    }

    @Override
    public Metadata getMetadata(NodesCollection nodesCollection, IGUID guid) throws MetadataNotFoundException {
        try {
            return (Metadata) dataDiscoveryService.getManifest(nodesCollection, guid);
        } catch (ManifestNotFoundException e) {
            throw new MetadataNotFoundException("Unable to find metadata");
        }
    }

}
