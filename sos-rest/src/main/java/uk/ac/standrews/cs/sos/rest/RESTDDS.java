package uk.ac.standrews.cs.sos.rest;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.standrews.cs.GUIDFactory;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.HTTP.HTTPResponses;
import uk.ac.standrews.cs.sos.RESTConfig;
import uk.ac.standrews.cs.sos.bindings.DDSNode;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.interfaces.manifests.Manifest;
import uk.ac.standrews.cs.sos.interfaces.sos.DDS;
import uk.ac.standrews.cs.sos.model.manifests.*;
import uk.ac.standrews.cs.sos.utils.JSONHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
@Path("/dds/")
@DDSNode
public class RESTDDS {

    @POST
    @Path("/manifest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response postManifest(String json) throws IOException {

        Manifest manifest;
        try {
            JsonNode node = JSONHelper.JsonObjMapper().readTree(json);
            ManifestType type = ManifestType.get(node.get(ManifestConstants.KEY_TYPE).textValue());
            manifest = getManifest(type, json);
        } catch (IOException e) {
            return HTTPResponses.BAD_REQUEST("Invalid Input");
        }

        if (manifest == null) {
            return HTTPResponses.BAD_REQUEST("Invalid Input");
        }

        DDS dds = RESTConfig.sos.getDDS();
        try {
            dds.addManifest(manifest, false);
        } catch (ManifestPersistException e) {
            return HTTPResponses.BAD_REQUEST("Invalid Input");
        }

        return HTTPResponses.CREATED(manifest.toString());
    }

    @GET
    @Path("/manifest/guid/{guid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response getManifest(@PathParam("guid") String guid) {
        if (guid == null || guid.isEmpty()) {
            return HTTPResponses.BAD_REQUEST("Bad input");
        }

        IGUID manifestGUID;
        try {
            manifestGUID = GUIDFactory.recreateGUID(guid);
        } catch (GUIDGenerationException e) {
            return HTTPResponses.BAD_REQUEST("Bad input");
        }

        DDS dds = RESTConfig.sos.getDDS();
        try {
            Manifest manifest = dds.getManifest(manifestGUID);
            return HTTPResponses.OK(manifest.toString());
        } catch (ManifestNotFoundException e) {
            return HTTPResponses.BAD_REQUEST("Invalid Input");
        }
    }

    private Manifest getManifest(ManifestType type, String json) throws IOException {
        Manifest manifest;
        switch(type) {
            case ATOM:
                manifest = getAtomManifest(json);
                break;
            case COMPOUND:
                manifest = getCompoundManifest(json);
                break;
            case ASSET:
                manifest = getVersionManifest(json);
                break;
            default:
                return null;
        }

        return manifest;
    }

    private AtomManifest getAtomManifest(String json) throws IOException {
        return JSONHelper.JsonObjMapper().readValue(json, AtomManifest.class);
    }

    private CompoundManifest getCompoundManifest(String json) throws IOException {
        return JSONHelper.JsonObjMapper().readValue(json, CompoundManifest.class);
    }

    private AssetManifest getVersionManifest(String json) throws IOException {
        return JSONHelper.JsonObjMapper().readValue(json, AssetManifest.class);
    }
}
