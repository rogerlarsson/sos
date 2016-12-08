package uk.ac.standrews.cs.sos.rest;

import uk.ac.standrews.cs.LEVEL;
import uk.ac.standrews.cs.sos.HTTP.HTTPStatus;
import uk.ac.standrews.cs.sos.RESTConfig;
import uk.ac.standrews.cs.sos.node.SOSLocalNode;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
@Path("/")
public class RESTGeneral {

    @GET
    @Path("/info")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getInfo() {
        SOS_LOG.log(LEVEL.INFO, "REST: /info");

        SOSLocalNode sos = RESTConfig.sos;
        return Response.status(HTTPStatus.OK)
                .entity(sos.toString())
                .build();
    }

}
