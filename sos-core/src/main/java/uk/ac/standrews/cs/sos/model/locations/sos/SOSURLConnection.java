package uk.ac.standrews.cs.sos.model.locations.sos;

import uk.ac.standrews.cs.GUIDFactory;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.exceptions.storage.DataStorageException;
import uk.ac.standrews.cs.sos.interfaces.node.Node;
import uk.ac.standrews.cs.sos.model.storage.InternalStorage;
import uk.ac.standrews.cs.sos.node.NodeManager;
import uk.ac.standrews.cs.storage.exceptions.BindingAbsentException;
import uk.ac.standrews.cs.storage.exceptions.DataException;
import uk.ac.standrews.cs.storage.interfaces.Directory;
import uk.ac.standrews.cs.storage.interfaces.File;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class handles all requests on the URLs under the sos:// scheme.
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSURLConnection extends URLConnection {

    private InternalStorage internalStorage;
    private NodeManager nodeManager;

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     */
    protected SOSURLConnection(InternalStorage internalStorage,
                               NodeManager nodeManager, URL url) {
        super(url);

        this.internalStorage = internalStorage;
        this.nodeManager = nodeManager;
    }

    @Override
    public void connect() throws IOException {}

    /**
     * Return the input stream given a sos location.
     * There are two cases:
     * 1 - the location is this one, thus we get the data from the internal storage
     * 2 - the location is not this one:
     *  a - if the location is known, we contact that node.
     *  b - if the location is not known, we contact a coordinator first
     *
     * @return
     * @throws IOException
     */
    @Override
    public InputStream getInputStream() throws IOException {

        try {
            IGUID thisGUID = nodeManager.getLocalNode().getNodeGUID();
            IGUID nodeGUID = GUIDFactory.recreateGUID(url.getHost());
            IGUID entityGUID = GUIDFactory.recreateGUID(url.getFile().substring(1)); // skip initial / sign

            if (thisGUID.equals(nodeGUID)) {
                return getDataLocally(entityGUID); // CASE 1
            } else {
                Node nodeToContact = nodeManager.getNode(nodeGUID);
                if (nodeToContact != null) {
                    return contactNode(nodeToContact, entityGUID); // CASE 2.A
                } else {
                    // TODO - contact coordinator, get info about node and then contact node
                    return null; // CASE 2.B
                }
            }
        } catch (GUIDGenerationException | DataException
                | BindingAbsentException | DataStorageException e) {
            throw new IOException(e);
        }
    }

    private InputStream getDataLocally(IGUID entityGUID) throws DataStorageException, BindingAbsentException, DataException, IOException {
        Directory dataDir = internalStorage.getDataDirectory();

        File path = (File) dataDir.get(entityGUID.toString());
        return path.getData().getInputStream();
    }

    private InputStream contactNode(Node node, IGUID entityId) throws IOException {

        InetSocketAddress inetSocketAddress = node.getHostAddress();

        String urlString = "http://" +
                            inetSocketAddress.getHostName() +
                            ":" + inetSocketAddress.getPort() +
                            "/storage/data?guid=" +
                            entityId.toString();

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        return conn.getInputStream();
    }

}
