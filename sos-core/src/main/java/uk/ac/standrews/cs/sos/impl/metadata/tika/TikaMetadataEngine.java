package uk.ac.standrews.cs.sos.impl.metadata.tika;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;
import uk.ac.standrews.cs.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataException;
import uk.ac.standrews.cs.sos.impl.metadata.AbstractMetadataEngine;
import uk.ac.standrews.cs.storage.data.Data;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class TikaMetadataEngine extends AbstractMetadataEngine {

    @Override
    public TikaMetadata processData(Data data) throws MetadataException {

        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();

        try (InputStream stream = data.getInputStream()) {
            parser.parse(stream, handler, metadata);

            TikaMetadata meta = new TikaMetadata(metadata, TikaIgnoreMetadata.IGNORE_METADATA);
            meta.addProperty("Size", Long.toString(data.getSize()));

            long unixTime = System.currentTimeMillis() / 1000L;
            meta.addProperty("Timestamp", Long.toString(unixTime));
            return meta;
        } catch (IOException | TikaException | SAXException | GUIDGenerationException e) {
            throw new MetadataException("Unable to generate metadata from given data", e);
        }

    }

}