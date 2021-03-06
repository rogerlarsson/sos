/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module rest.
 *
 * rest is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * rest is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with rest. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.sos.rest.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.guid.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.logger.LEVEL;
import uk.ac.standrews.cs.sos.exceptions.node.NodeNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.node.NodeRegistrationException;
import uk.ac.standrews.cs.sos.impl.node.SOSNode;
import uk.ac.standrews.cs.sos.interfaces.node.NodeType;
import uk.ac.standrews.cs.sos.model.Node;
import uk.ac.standrews.cs.sos.rest.HTTP.HTTPResponses;
import uk.ac.standrews.cs.sos.rest.RESTConfig;
import uk.ac.standrews.cs.sos.rest.bindings.NDSNode;
import uk.ac.standrews.cs.sos.services.NodeDiscoveryService;
import uk.ac.standrews.cs.sos.utils.JSONHelper;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashSet;
import java.util.Set;

import static uk.ac.standrews.cs.sos.network.Request.SOS_NODE_CHALLENGE_HEADER;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
@Path("/sos/nds/")
@NDSNode
public class RESTNDS {

    @POST
    @Path("/node")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response register(SOSNode node, @HeaderParam(SOS_NODE_CHALLENGE_HEADER) String node_challenge) {
        SOS_LOG.log(LEVEL.INFO, "REST: POST /sos/nds/node");

        NodeDiscoveryService nodeDiscoveryService = RESTConfig.sos.getNDS();

        try {
            Node registeredNode = nodeDiscoveryService.registerNode(node, true); // TODO - might change based on local configuration (see settings)
            if (registeredNode != null) {
                return HTTPResponses.CREATED(RESTConfig.sos, node_challenge, registeredNode.toString());
            } else {
                return HTTPResponses.INTERNAL_SERVER(RESTConfig.sos, node_challenge);
            }

        } catch (NodeRegistrationException e) {
            return HTTPResponses.INTERNAL_SERVER(RESTConfig.sos, node_challenge);
        }

    }

    @GET
    @Path("/node/guid/{guid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response findByGUID(@PathParam("guid") String guid, @HeaderParam(SOS_NODE_CHALLENGE_HEADER) String node_challenge) {
        SOS_LOG.log(LEVEL.INFO, "REST: GET /sos/nds/node/guid/{guid}");

        if (guid == null || guid.isEmpty()) {
            return HTTPResponses.BAD_REQUEST(RESTConfig.sos, node_challenge, "Bad input");
        }

        IGUID nodeGUID;
        try {
            nodeGUID = GUIDFactory.recreateGUID(guid);
        } catch (GUIDGenerationException e) {
            return HTTPResponses.BAD_REQUEST(RESTConfig.sos, node_challenge, "Bad input");
        }

        NodeDiscoveryService nodeDiscoveryService = RESTConfig.sos.getNDS();

        try {
            Node node = nodeDiscoveryService.getNode(nodeGUID);
            return HTTPResponses.OK(RESTConfig.sos, node_challenge, node.toString());
        } catch (NodeNotFoundException e) {
            return HTTPResponses.NOT_FOUND(RESTConfig.sos, node_challenge, "Node with GUID: " + nodeGUID.toMultiHash() + " could not be found");
        }

    }

    @GET
    @Path("/service/{service}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response findByService(@PathParam("service") String service, @HeaderParam(SOS_NODE_CHALLENGE_HEADER) String node_challenge) {
        SOS_LOG.log(LEVEL.INFO, "REST: GET /sos/nds/service/{service}");

        try {
            NodeDiscoveryService nodeDiscoveryService = RESTConfig.sos.getNDS();

            NodeType nodeType = NodeType.get(service.toLowerCase());
            Set<Node> nodes = new LinkedHashSet<>();
            for(IGUID nodeRef:nodeDiscoveryService.getNodes(nodeType)) {
                try {
                    Node node = nodeDiscoveryService.getNode(nodeRef);
                    nodes.add(node);
                } catch (NodeNotFoundException e) {
                    /* IGNORE */
                }
            }

            String out = JSONHelper.jsonObjMapper().writeValueAsString(nodes);

            return HTTPResponses.OK(RESTConfig.sos, node_challenge, out);

        } catch (JsonProcessingException e) {

            return HTTPResponses.INTERNAL_SERVER(RESTConfig.sos, node_challenge);
        }
    }

}
