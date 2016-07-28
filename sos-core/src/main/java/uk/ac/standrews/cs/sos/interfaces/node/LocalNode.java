package uk.ac.standrews.cs.sos.interfaces.node;

import uk.ac.standrews.cs.sos.interfaces.identity.Identity;
import uk.ac.standrews.cs.sos.interfaces.sos.Client;
import uk.ac.standrews.cs.sos.interfaces.sos.DiscoveryData;
import uk.ac.standrews.cs.sos.interfaces.sos.DiscoveryNode;
import uk.ac.standrews.cs.sos.interfaces.sos.Storage;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface LocalNode extends Node {

    Client getClient();

    Storage getStorage();

    DiscoveryData getDiscoveryData();

    DiscoveryNode getDiscoveryNode();

    Identity getIdentity();

    void kill();
}