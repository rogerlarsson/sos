package uk.ac.standrews.cs.sos.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.ac.standrews.cs.sos.constants.JSONConstants;
import uk.ac.standrews.cs.sos.model.User;
import uk.ac.standrews.cs.utilities.crypto.CryptoException;
import uk.ac.standrews.cs.utilities.crypto.DigitalSignature;

import java.io.IOException;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class UserSerializer extends JsonSerializer<User> {

    @Override
    public void serialize(User user, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField(JSONConstants.KEY_GUID, user.guid().toString());
        jsonGenerator.writeStringField(JSONConstants.KEY_NAME, user.getName());

        try {
            jsonGenerator.writeStringField(JSONConstants.KEY_SIGNATURE_CERTIFICATE, DigitalSignature.getCertificateString(user.getSignatureCertificate()));
        } catch (CryptoException e) {
            e.printStackTrace();
        }

        jsonGenerator.writeEndObject();
    }
}