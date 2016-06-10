package uk.ac.standrews.cs.sos.model.locations.sos.url;

import uk.ac.standrews.cs.sos.node.NodeManager;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * This factory is used to add the sos:// scheme in the URL Stream Handler protocol.
 * All requests on sos:// will be handled by @see SOSURLConnection
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSURLStreamHandlerFactory implements URLStreamHandlerFactory {

    public static boolean URLStreamHandlerFactoryIsSet = false;

    private NodeManager nodeManager;

    /**
     * Construct the factory with the given node manager.
     *
     * @param nodeManager
     */
    public SOSURLStreamHandlerFactory(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
        URLStreamHandlerFactoryIsSet = true;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("sos".equals(protocol)) {
            return new SOSURLStreamHandler(nodeManager);
        }

        return null;
    }

}
