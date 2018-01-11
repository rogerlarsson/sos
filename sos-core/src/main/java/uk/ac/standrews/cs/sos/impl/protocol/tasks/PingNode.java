package uk.ac.standrews.cs.sos.impl.protocol.tasks;

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
 * NOTE: use this only for testing and experiments
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class PingNode extends Task {

    private final Node node;
    private final String message;
    private Long timestamp;
    private Long latency;

    public PingNode(Node node, String message) {
        super();

        this.node = node;
        this.message = message;

        timestamp = 0L;
    }

    @Override
    public void performAction() {
        SOS_LOG.log(LEVEL.INFO, "Info about node: " + node.guid().toMultiHash());

        try {
            URL url = SOSURL.NODE_PING(node, message);
            SyncRequest request = new SyncRequest(node.getSignatureCertificate(), HTTPMethod.GET, url, ResponseType.TEXT);

            long startRequest = System.nanoTime();
            Response response = RequestsManager.getInstance().playSyncRequest(request);

            if (!(response instanceof ErrorResponseImpl)) {

                response.consumeResponse();

                if (response.getCode() == HTTPStatus.OK) {
                    setState(TaskState.SUCCESSFUL);
                } else {
                    setState(TaskState.UNSUCCESSFUL);
                }

            } else {
                setState(TaskState.UNSUCCESSFUL);
            }

            latency = System.nanoTime() - startRequest;
            timestamp = System.currentTimeMillis();

        } catch (SOSURLException | IOException e) {
            setState(TaskState.ERROR);
            SOS_LOG.log(LEVEL.ERROR, "Unable to get info about node " + node.guid().toMultiHash());
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

    public boolean valid() {
        return getState() == TaskState.SUCCESSFUL;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "InfoNode " + node.guid();
    }

    public Long getLatency() {
        return latency;
    }
}
