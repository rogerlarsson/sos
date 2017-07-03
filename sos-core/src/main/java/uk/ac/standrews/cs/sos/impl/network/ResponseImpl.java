package uk.ac.standrews.cs.sos.impl.network;

import com.mashape.unirest.http.HttpResponse;
import uk.ac.standrews.cs.sos.interfaces.network.Response;
import uk.ac.standrews.cs.sos.utils.IO;
import uk.ac.standrews.cs.sos.utils.JSONHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper around the okhttp3 response class
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ResponseImpl implements Response {

    private HttpResponse<?> response;

    public ResponseImpl(HttpResponse<?> response) {
        this.response = response;
    }

    @Override
    public int getCode() {
        return response.getStatus();
    }

    @Override
    public InputStream getBody() {
        return response.getRawBody();
    }

    @Override
    public com.fasterxml.jackson.databind.JsonNode getJSON() {

        try {
            return JSONHelper.JsonObjMapper().readTree(response.getBody().toString());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getStringBody() throws IOException {

        return IO.InputStreamToString(getBody());
    }

    public int getContentLength() {

        return Integer.parseInt(response.getHeaders().getFirst("Content-Length"));
    }
}
