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
package uk.ac.standrews.cs.sos.services;

import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.sos.exceptions.node.NodeNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.node.NodeRegistrationException;
import uk.ac.standrews.cs.sos.impl.node.NodeStats;
import uk.ac.standrews.cs.sos.interfaces.node.NodeType;
import uk.ac.standrews.cs.sos.model.Node;
import uk.ac.standrews.cs.sos.model.NodesCollection;

import java.util.Set;

/**
 * NDS - Node Discovery Service
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface NodeDiscoveryService extends Service {

    /**
     * The manifestsDataService should be set to allow the NodeDiscoveryService to handle nodes as first class entities
     *
     * @param manifestsDataService for this node
     */
    void setMDS(ManifestsDataService manifestsDataService);

    /**
     * Get a node object for the local node
     *
     * @return this node
     */
    Node getThisNode();

    /**
     * Registers a node to the SOS
     *
     * @param node to register
     * @param localOnly if true, the node will be registered to this node only. Otherwise, if false, the node will be registered to other known NDS nodes
     * @return registered node
     */
    Node registerNode(Node node, boolean localOnly) throws NodeRegistrationException;

    /**
     * Get a known node to this Sea Of Stuff
     *
     * @param guid of the node to find
     * @return the noda with the given guid
     */
    Node getNode(IGUID guid) throws NodeNotFoundException;

    /**
     * Get a set of nodes matching the specified type
     *
     * @param type of request nodes
     * @return set of node refs
     */
    Set<IGUID> getNodes(NodeType type);

    /**
     * Get a set of nodes from a nodes collection.
     * The limit parameters will constrain the size of the set.
     *
     * @param nodesCollection from which to find the nodes
     * @param limit max number of returned nodes
     * @return the set of nodes found
     */
    Set<IGUID> getNodes(NodesCollection nodesCollection, int limit);

    /**
     * Returns a set of node refs matching the NodesCollection and NodeType constraints.
     * This method returns a maximum number of nodes as specified by the limit parameter.
     *
     * @param nodesCollection from which to find the nodes
     * @param type of node to find
     * @param limit max number of returned nodes
     * @return the filtered nodes collection
     */
    NodesCollection filterNodesCollection(NodesCollection nodesCollection, NodeType type, int limit);

    /**
     * Filter the nodes in the collection by the given limit
     *
     * @param nodesCollection
     * @param limit
     * @return
     */
    NodesCollection filterNodesCollection(NodesCollection nodesCollection, int limit);

    /**
     * Get all known nodes
     *
     * @return set of nodes
     */
    Set<IGUID> getNodes();

    /**
     * Returns a set of known nodes.
     * The limit constrains the maximum number of nodes returned.
     *
     * @param limit max number of nodes to return
     * @return the nodes found
     */
    Set<IGUID> getNodes(int limit);

    /**
     * Get the generic info about a given node
     *
     * @param guid of the node
     * @return the info in JSON format
     * @throws NodeNotFoundException if the node could not be found
     */
    String infoNode(IGUID guid) throws NodeNotFoundException;

    /**
     * Get the generic info about a given node
     *
     * @param node with partial info. This node must have: host address, host port, signature certificate
     * @return the info in JSON format
     * @throws NodeNotFoundException if the node could not be found
     */
    String infoNode(Node node) throws NodeNotFoundException;

    /**
     * Current stats about the node with the matching GUID
     *
     * @param guid of the node
     * @return the stats object for the node
     */
    NodeStats getNodeStats(IGUID guid);

}
