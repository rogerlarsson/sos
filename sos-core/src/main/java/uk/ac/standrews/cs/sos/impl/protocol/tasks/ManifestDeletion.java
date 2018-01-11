package uk.ac.standrews.cs.sos.impl.protocol.tasks;

import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.logger.LEVEL;
import uk.ac.standrews.cs.sos.exceptions.protocol.SOSURLException;
import uk.ac.standrews.cs.sos.impl.protocol.SOSURL;
import uk.ac.standrews.cs.sos.impl.protocol.Task;
import uk.ac.standrews.cs.sos.impl.protocol.TaskState;
import uk.ac.standrews.cs.sos.interfaces.network.Response;
import uk.ac.standrews.cs.sos.model.Node;
import uk.ac.standrews.cs.sos.network.*;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import java.io.IOException;
import java.net.URL;

/**
 * TODO - extend task to all manifest-based services (mms, cms, usro, etc)
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ManifestDeletion extends Task {

    private Node node;
    private IGUID guid;

    public ManifestDeletion(Node node, IGUID guid) {
        this.node = node;
        this.guid = guid;
    }

    @Override
    protected void performAction() {

        try {
            URL url = SOSURL.DDS_DELETE_MANIFEST(node, guid);
            SyncRequest request = new SyncRequest(node.getSignatureCertificate(), HTTPMethod.GET, url, ResponseType.JSON);
            Response response = RequestsManager.getInstance().playSyncRequest(request);

            if (!(response instanceof ErrorResponseImpl)) {

                response.consumeResponse();
                setState(TaskState.SUCCESSFUL);
            } else {
                SOS_LOG.log(LEVEL.DEBUG, "ManifestDeletion -- ERROR RESPONSE");
                setState(TaskState.ERROR);
            }

        } catch (SOSURLException | IOException e) {
            setState(TaskState.ERROR);
            SOS_LOG.log(LEVEL.ERROR, "Unable to delete manifest with GUID " + guid.toMultiHash() + " in node " + node.guid().toMultiHash());
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
