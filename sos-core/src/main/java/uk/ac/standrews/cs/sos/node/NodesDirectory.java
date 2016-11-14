package uk.ac.standrews.cs.sos.node;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.exceptions.db.DatabaseConnectionException;
import uk.ac.standrews.cs.sos.exceptions.node.NodesDirectoryException;
import uk.ac.standrews.cs.sos.interfaces.node.Node;
import uk.ac.standrews.cs.sos.interfaces.node.NodesDatabase;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * This is the node directory for this node, which keeps track of the known nodes.
 *
 * TODO - apply policy to enforce what to return and how much
 * TODO - queries are not performed at the DB level, this might be a bit inefficient
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class NodesDirectory {

    private Node localNode;
    private NodesDatabase nodesDatabase;

    private Collection<Node> knownNodes;

    public NodesDirectory(Node localNode, NodesDatabase nodesDatabase) throws NodesDirectoryException {
        this.localNode = localNode;
        this.nodesDatabase = nodesDatabase;

        this.knownNodes = new HashSet<>();
        loadNodesFromDB();
    }

    /**
     * Add an arbitrary node to the directory.
     * This will be used to discovery nodes/data in the LocalSOSNode.
     *
     * @param node
     */
    public void addNode(Node node) {
        knownNodes.add(node);
    }

    /**
     * Get all known nodes.
     *
     * @return
     */
    public Collection<Node> getKnownNodes() {
        return knownNodes;
    }

    /**
     * Get a LocalSOSNode node given its guid identifier.
     *
     * @param guid
     * @return
     */
    public Node getNode(IGUID guid) {
        Optional<Node> node = knownNodes.stream()
                .filter(n -> n.getNodeGUID().equals(guid))
                .findFirst();

        return node.isPresent() ? node.get() : null;
    }

    /**
     * Get all NDS Nodes
     *
     * @return
     */
    public Collection<Node> getNDSNodes() {
        return getNodes(Node::isNDS);
    }

    /**
     * Get all DDS Nodes
     *
     * @return
     */
    public Collection<Node> getDDSNodes() {
        return getNodes(Node::isDDS);
    }

    /**
     * Get all MCS Nodes
     *
     * @return
     */
    public Collection<Node> getMCSNodes() {
        return getNodes(Node::isMCS);
    }

    /**
     * Get all Storage Nodes
     * @return
     */
    public Collection<Node> getStorageNodes() {
        return getNodes(Node::isStorage);
    }

    private Collection<Node> getNodes(Predicate<Node> predicate) {

        List<Node> retval = knownNodes.parallelStream()
                .filter(predicate)
                .collect(toList());

        return retval;
    }

    /**
     * Get the local node running
     * @return
     */
    public Node getLocalNode() {
        return this.localNode;
    }

    /**
     * Persist the collection of known nodes.
     *
     * @throws NodesDirectoryException
     */
    public void persistNodesTable() throws NodesDirectoryException {
        try {
            for (Node knownNode : knownNodes) {
                nodesDatabase.addNode(knownNode); // FIXME - do not persist nodes that have not changed
            }
        } catch (DatabaseConnectionException e) {
            throw new NodesDirectoryException(e);
        }
    }

    /**
     * Load nodes from the DB to the node directory (in memory)
     * @throws NodesDirectoryException
     */
    private void loadNodesFromDB() throws NodesDirectoryException {
        try {
            Collection<SOSNode> nodes = nodesDatabase.getNodes();
            knownNodes.addAll(nodes);
        } catch (DatabaseConnectionException e) {
            throw new NodesDirectoryException(e);
        }
    }
}
