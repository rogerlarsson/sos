package uk.ac.standrews.cs.sos.impl.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.ac.standrews.cs.sos.constants.JSONConstants;
import uk.ac.standrews.cs.sos.impl.datamodel.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.model.Atom;

import java.io.IOException;
import java.util.Set;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class AtomManifestSerializer extends JsonSerializer<Atom> {

    @Override
    public void serialize(Atom atom, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField(JSONConstants.KEY_TYPE, atom.getType().toString());
        jsonGenerator.writeStringField(JSONConstants.KEY_GUID, atom.guid().toMultiHash());

        jsonGenerator.writeFieldName(JSONConstants.KEY_LOCATIONS);
        jsonGenerator.writeStartArray();
        serializeLocations(atom, jsonGenerator);
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }

    private void serializeLocations(Atom atom, JsonGenerator jsonGenerator) throws IOException {
        Set<LocationBundle> locations = atom.getLocations();
        for(LocationBundle location:locations) {
            jsonGenerator.writeObject(location);
        }
    }
}
