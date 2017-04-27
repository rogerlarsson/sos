package uk.ac.standrews.cs.sos.impl.actors;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.LEVEL;
import uk.ac.standrews.cs.sos.actors.NDS;
import uk.ac.standrews.cs.sos.exceptions.node.NodeNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.node.NodeRegistrationException;
import uk.ac.standrews.cs.sos.exceptions.node.NodesDirectoryException;
import uk.ac.standrews.cs.sos.impl.node.SOSNode;
import uk.ac.standrews.cs.sos.impl.node.directory.LocalNodesDirectory;
import uk.ac.standrews.cs.sos.interfaces.node.Database;
import uk.ac.standrews.cs.sos.model.Node;
import uk.ac.standrews.cs.sos.protocol.TasksQueue;
import uk.ac.standrews.cs.sos.protocol.tasks.GetNode;
import uk.ac.standrews.cs.sos.protocol.tasks.RegisterNode;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import java.util.Iterator;
import java.util.Set;

/**
 * The SOSNDS represents a basic NDS implementation.
 * It provides naive methods to register new nodes in the sos and get known nodes.
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSNDS implements NDS {

    private LocalNodesDirectory localNodesDirectory;

    public SOSNDS(Node localNode, Database database) throws NodesDirectoryException {
        localNodesDirectory = new LocalNodesDirectory(localNode, database);
    }

    @Override
    public Node getThisNode() {
        return localNodesDirectory.getLocalNode();
    }

    @Override
    public Node registerNode(Node node, boolean localOnly) throws NodeRegistrationException {
        if (node == null) {
            throw new NodeRegistrationException("Invalid node");
        }

        Node nodeToRegister = new SOSNode(node);

        try {
            localNodesDirectory.addNode(nodeToRegister);
            localNodesDirectory.persistNodesTable();
        } catch (NodesDirectoryException e) {
            throw new NodeRegistrationException("Unable to register node", e);
        }

        // Register the node to other NDS nodes
        if (!localOnly) {
            Set<Node> ndsNodes = localNodesDirectory.getNDSNodes(LocalNodesDirectory.NO_LIMIT);
            ndsNodes.forEach(n -> {
                RegisterNode registerNode = new RegisterNode(node, n);
                TasksQueue.instance().performAsyncTask(registerNode);
            });
        }

        return nodeToRegister;
    }

    @Override
    public Node getNode(IGUID guid) throws NodeNotFoundException {

        if (guid == null || guid.isInvalid()) {
            throw new NodeNotFoundException("Cannot find node for invalid GUID");
        }

        Node localNode = localNodesDirectory.getLocalNode();
        if (localNode.getNodeGUID().equals(guid)) {
            return localNode;
        }

        Node nodeToContact = localNodesDirectory.getNode(guid);

        if (nodeToContact == null) {
            nodeToContact = findNodeViaNDS(guid);
        }

        if (nodeToContact == null) {
            throw new NodeNotFoundException("Unable to find node for GUID: " + guid.toString());
        } else {
            SOS_LOG.log(LEVEL.INFO, "Node with GUID " + guid + " was found: " + nodeToContact.toString());
        }

        return nodeToContact;
    }

    @Override
    public Set<Node> getNDSNodes() {
        return localNodesDirectory.getNDSNodes(LocalNodesDirectory.NO_LIMIT);
    }

    @Override
    public Set<Node> getDDSNodes() {
        return localNodesDirectory.getDDSNodes(LocalNodesDirectory.NO_LIMIT);
    }

    @Override
    public Iterator<Node> getDDSNodesIterator() {
        return localNodesDirectory.getDDSNodesIterator();
    }

    @Override
    public Set<Node> getMMSNodes() {
        return localNodesDirectory.getMMSNodes(LocalNodesDirectory.NO_LIMIT);
    }

    @Override
    public Set<Node> getStorageNodes() {
        return localNodesDirectory.getStorageNodes(LocalNodesDirectory.NO_LIMIT);
    }

    @Override
    public Iterator<Node> getStorageNodesIterator() {
        return localNodesDirectory.getStorageNodesIterator();
    }

    @Override
    public Set<Node> getCMSNodes() {
        return localNodesDirectory.getCMSNodes(LocalNodesDirectory.NO_LIMIT);
    }

    @Override
    public Set<Node> getRMSNodes() {
        return localNodesDirectory.getRMSNodes(LocalNodesDirectory.NO_LIMIT);
    }

    @Override
    public Set<Node> getAllNodes() {
        return localNodesDirectory.getKnownNodes();
    }

    /**
     * Find a matching node for the given GUID through other known NDS nodes
     */
    private Node findNodeViaNDS(IGUID nodeGUID) throws NodeNotFoundException {

        Set<Node> ndsNodes = localNodesDirectory.getNDSNodes(LocalNodesDirectory.NO_LIMIT);
        GetNode getNode = new GetNode(nodeGUID, ndsNodes.iterator());
        TasksQueue.instance().performSyncTask(getNode);

        Node retval = getNode.getFoundNode();
        if (retval == null) {
            throw new NodeNotFoundException("Unable to find node with GUID " + nodeGUID);
        }

        return retval;
    }
}