package uk.ac.standrews.cs.sos.network;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.input.NullInputStream;
import uk.ac.standrews.cs.sos.interfaces.network.Response;

import java.io.InputStream;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ErrorResponseImpl implements Response {
    @Override
    public int getCode() {
        return HTTPStatus.INTERNAL_SERVER;
    }

    @Override
    public InputStream getBody() {
        return new NullInputStream(0);
    }

    @Override
    public JsonNode getJSON() {
        return null;
    }

    @Override
    public String getStringBody() {
        return "";
    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public void consumeResponse() { }
}
