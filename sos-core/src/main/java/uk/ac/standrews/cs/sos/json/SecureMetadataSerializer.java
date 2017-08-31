package uk.ac.standrews.cs.sos.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.sos.constants.JSONConstants;
import uk.ac.standrews.cs.sos.model.ManifestType;
import uk.ac.standrews.cs.sos.model.SecureMetadata;

import java.io.IOException;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SecureMetadataSerializer extends JsonSerializer<SecureMetadata> {

    @Override
    public void serialize(SecureMetadata metadata, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField(JSONConstants.KEY_GUID, metadata.guid().toMultiHash());
        jsonGenerator.writeStringField(JSONConstants.KEY_TYPE, ManifestType.METADATA_PROTECTED.toString());

        jsonGenerator.writeFieldName(JSONConstants.KEY_META_PROPERTIES);
        jsonGenerator.writeStartArray();
        serializeElements(metadata, jsonGenerator);
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }

    // TODO - all the code below is shared with MetadataSerializer
    private void serializeElements(SecureMetadata metadata, JsonGenerator jsonGenerator) throws IOException {

        String[] properties = metadata.getAllPropertyNames();
        for(String property:properties) {
            jsonGenerator.writeStartObject();

            Object value = metadata.getProperty(property);
            String type = getType(value);

            jsonGenerator.writeStringField(JSONConstants.KEY_META_KEY, property);
            jsonGenerator.writeStringField(JSONConstants.KEY_META_TYPE, type);
            writeValue(jsonGenerator, type, value);

            jsonGenerator.writeEndObject();

        }
    }

    public String getType(Object value) {
        if (value instanceof Long) {
            return "LONG";
        } else if (value instanceof Integer) {
            return "INT";
        } else if (value instanceof IGUID) {
            return "GUID";
        } else {
            return "STRING";
        }
    }

    private void writeValue(JsonGenerator jsonGenerator, String type, Object value) throws IOException {
        switch(type) {
            case "LONG":
                jsonGenerator.writeNumberField(JSONConstants.KEY_META_VALUE, (Long) value);
                break;
            case "INT":
                jsonGenerator.writeNumberField(JSONConstants.KEY_META_VALUE, (Integer) value);
                break;
            case "STRING":
                jsonGenerator.writeStringField(JSONConstants.KEY_META_VALUE, (String) value);
                break;
            case "GUID":
                jsonGenerator.writeStringField(JSONConstants.KEY_META_VALUE, ((IGUID) value).toMultiHash());
                break;
        }
    }

}
