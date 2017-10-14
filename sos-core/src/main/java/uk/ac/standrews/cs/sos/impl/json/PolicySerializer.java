package uk.ac.standrews.cs.sos.impl.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.ac.standrews.cs.sos.constants.JSONConstants;
import uk.ac.standrews.cs.sos.model.ManifestType;
import uk.ac.standrews.cs.sos.model.Policy;

import java.io.IOException;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class PolicySerializer extends JsonSerializer<Policy> {

    @Override
    public void serialize(Policy policy, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField(JSONConstants.KEY_TYPE, ManifestType.POLICY.toString());
        jsonGenerator.writeStringField(JSONConstants.KEY_GUID, policy.guid().toMultiHash());

        jsonGenerator.writeFieldName(JSONConstants.KEY_COMPUTATIONAL_DEPENDENCIES);
        jsonGenerator.writeTree(policy.dependencies());

        jsonGenerator.writeStringField(JSONConstants.KEY_POLICY_APPLY, policy.apply().asText());
        jsonGenerator.writeStringField(JSONConstants.KEY_POLICY_SATISFIED, policy.satisfied().asText());

        jsonGenerator.writeFieldName(JSONConstants.KEY_POLICY_FIELDS);
        jsonGenerator.writeTree(policy.fields());

        jsonGenerator.writeEndObject();
    }
}