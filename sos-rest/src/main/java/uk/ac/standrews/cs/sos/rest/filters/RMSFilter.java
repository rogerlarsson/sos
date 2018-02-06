package uk.ac.standrews.cs.sos.rest.filters;

import uk.ac.standrews.cs.sos.rest.HTTP.HTTPResponses;
import uk.ac.standrews.cs.sos.rest.RESTConfig;
import uk.ac.standrews.cs.sos.rest.bindings.RMSNode;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
@Provider
@RMSNode
public class RMSFilter extends CommonFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        super.filter(requestContext);

        if (!RESTConfig.sos.isRMS()) {
            Response response = HTTPResponses.BAD_REQUEST(RESTConfig.sos, null, "I am not an RMS node");
            throw new WebApplicationException(response);
        }

    }
}
