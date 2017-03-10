package uk.ac.standrews.cs.sos.actors;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataException;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataPersistException;
import uk.ac.standrews.cs.sos.interfaces.actors.DDS;
import uk.ac.standrews.cs.sos.interfaces.actors.MMS;
import uk.ac.standrews.cs.sos.interfaces.metadata.MetadataEngine;
import uk.ac.standrews.cs.sos.interfaces.model.Metadata;
import uk.ac.standrews.cs.storage.data.InputStreamData;

import java.io.InputStream;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSMMS implements MMS {

    private DDS dds;
    private MetadataEngine engine;

    public SOSMMS(DDS dds, MetadataEngine metadataEngine) {
        this.dds = dds;
        this.engine = metadataEngine;
    }


    @Override
    public void addMetadata(Metadata metadata) throws MetadataPersistException {
        try {
            dds.addManifest(metadata, false);
        } catch (ManifestPersistException e) {
            throw new MetadataPersistException(e);
        }
    }

    @Override
    public Metadata getMetadata(IGUID guid) throws MetadataNotFoundException {
        try {
            return (Metadata) dds.getManifest(guid);
        } catch (ManifestNotFoundException e) {
            throw new MetadataNotFoundException("unable to find metadata");
        }
    }

    @Override
    public Metadata processMetadata(InputStream inputStream) throws MetadataException {

        InputStreamData data = new InputStreamData(inputStream);
        return engine.processData(data);
    }

}