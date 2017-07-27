package uk.ac.standrews.cs.sos.model;

import uk.ac.standrews.cs.guid.IGUID;

import java.util.Set;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface NodesCollection {

    /**
     * Returns the refs of the nodes available from within this scope
     * if the type is SPECIFIED
     *
     * If the type is LOCAL or ANY, the set of nodes returned is empty.
     *
     * @return a set of refs to nodes
     */
    Set<IGUID> nodesRefs();

    /**
     * Get the type of the collection.
     *
     * @return the type of this nodes collection
     */
    TYPE type();

    enum TYPE {
        LOCAL, // This local node
        SPECIFIED, // The collection is limited to the specified nodes
        ANY // The collection is unlimited
    }

}
