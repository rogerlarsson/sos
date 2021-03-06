/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module core.
 *
 * core is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * core is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with core. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.sos.impl.protocol.tasks;

import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.logger.LEVEL;
import uk.ac.standrews.cs.sos.exceptions.node.NodeNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.protocol.SOSProtocolException;
import uk.ac.standrews.cs.sos.exceptions.protocol.SOSURLException;
import uk.ac.standrews.cs.sos.impl.protocol.SOSURL;
import uk.ac.standrews.cs.sos.impl.protocol.Task;
import uk.ac.standrews.cs.sos.impl.protocol.TaskState;
import uk.ac.standrews.cs.sos.interfaces.network.Response;
import uk.ac.standrews.cs.sos.model.Context;
import uk.ac.standrews.cs.sos.model.Manifest;
import uk.ac.standrews.cs.sos.model.Node;
import uk.ac.standrews.cs.sos.model.NodesCollection;
import uk.ac.standrews.cs.sos.network.ErrorResponseImpl;
import uk.ac.standrews.cs.sos.network.HTTPMethod;
import uk.ac.standrews.cs.sos.network.RequestsManager;
import uk.ac.standrews.cs.sos.network.SyncRequest;
import uk.ac.standrews.cs.sos.services.NodeDiscoveryService;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import java.io.IOException;
import java.net.URL;

/**
 * TODO - extend task to all manifest-based services (mms, cms, usro, etc)
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ManifestDeletion extends Task {

    private NodeDiscoveryService nodeDiscoveryService;
    private NodesCollection nodesCollection;
    private Manifest manifest;

    public ManifestDeletion(NodeDiscoveryService nodeDiscoveryService, NodesCollection nodesCollection, Manifest manifest) {
        this.nodeDiscoveryService = nodeDiscoveryService;
        this.nodesCollection = nodesCollection;
        this.manifest = manifest;
    }

    @Override
    protected void performAction() {

        // Delete manifests over nodes collection. If deletion fails in one of the nodes, keep going...
        for(IGUID nodeRef:nodesCollection.nodesRefs()) {

            try {
                Node node = nodeDiscoveryService.getNode(nodeRef);
                deleteManifest(node);
            } catch (SOSProtocolException e) {

                setState(TaskState.UNSUCCESSFUL);
                SOS_LOG.log(LEVEL.ERROR, "Unable to delete manifest with GUID " + manifest.guid().toMultiHash() + " in node " + nodeRef.toMultiHash());

            } catch (NodeNotFoundException e) {

                setState(TaskState.UNSUCCESSFUL);
                SOS_LOG.log(LEVEL.ERROR, "Unable to find node " + nodeRef.toMultiHash() + " so manifest with GUID " + manifest.guid().toMultiHash() + " could not be deleted");
            }
        }

        if (getState() != TaskState.UNSUCCESSFUL) {
            setState(TaskState.SUCCESSFUL);
        }

    }

    // Delete manifest at given node
    private void deleteManifest(Node node) throws SOSProtocolException {

        try {
            URL url = getManifestURL(node, manifest);
            SyncRequest request = new SyncRequest(node.getSignatureCertificate(), HTTPMethod.DELETE, url);
            Response response = RequestsManager.getInstance().playSyncRequest(request);

            if (!(response instanceof ErrorResponseImpl)) {

                response.consumeResponse();
                SOS_LOG.log(LEVEL.DEBUG, "ManifestDeletion - Manifest with GUID " + manifest.guid().toMultiHash() + " deleted in node " + node.guid().toMultiHash());
            } else {
                SOS_LOG.log(LEVEL.DEBUG, "ManifestDeletion -- ERROR RESPONSE for Manifest with GUID " + manifest.guid().toMultiHash() + " to be deleted in node " + node.guid().toMultiHash());
                throw new SOSProtocolException("ManifestDeletion -- ERROR RESPONSE");
            }

        } catch (SOSURLException | IOException e) {
            throw new SOSProtocolException("Unable to delete manifest with GUID " + manifest.guid().toMultiHash() + " in node " + node.guid().toMultiHash());
        }

    }

    private URL getManifestURL(Node node, Manifest manifest) throws SOSURLException {

        switch(manifest.getType()) {

            case ATOM: case ATOM_PROTECTED:
            case COMPOUND: case COMPOUND_PROTECTED:
            case VERSION:

                if (node.isMDS()) {
                    return SOSURL.MDS_DELETE_MANIFEST(node, manifest.guid());
                }

            case CONTEXT:

                if (node.isCMS()) {
                    return SOSURL.CMS_DELETE_CONTEXT_VERSIONS(node, ((Context) manifest).invariant());
                }

            case ROLE:
            case USER:
            case METADATA: case METADATA_PROTECTED:
            case NODE:
                throw new SOSURLException("Type: " + manifest.getType() + " not supported yet");

            default:
                throw new SOSURLException("Unable to return manifest URL for node " + node.toString());
        }

    }

    @Override
    public String serialize() {
        return null;
    }

    @Override
    public Task deserialize(String json) throws IOException {
        return null;
    }
}
