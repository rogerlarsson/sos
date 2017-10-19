package uk.ac.standrews.cs.sos.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.sos.impl.json.NodeDeserializer;
import uk.ac.standrews.cs.sos.impl.json.NodeSerializer;

import java.net.InetSocketAddress;
import java.security.PublicKey;

/**
 * Node interface
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
@JsonSerialize(using = NodeSerializer.class)
@JsonDeserialize(using = NodeDeserializer.class)
public interface Node extends Manifest {

    /**
     * This is the unique GUID for this node
     *
     * hash(signature certificate)
     *
     * @return GUID of the context
     */
    IGUID guid();


    /**
     * This is the signature certificate the the node can expose to the rest of the SOS.
     *
     * @return
     */
    PublicKey getSignatureCertificate();

    /**
     * This is the address of the node.
     * This information should be used to contact the node.
     *
     * @return
     */
    InetSocketAddress getHostAddress();

    // TODO - getIP
    String getHostname();

    /**
     * Returns true if this is a client node
     * @return
     */
    boolean isAgent();

    /**
     * Returns true if this is a storage node
     * @return
     */
    boolean isStorage();

    /**
     * Returns true if this is a DDS node
     * @return
     */
    boolean isDDS();

    /**
     * Returns true if this is a NDS node
     * @return
     */
    boolean isNDS();

    /**
     * Returns true if this is a MMS node
     * @return
     */
    boolean isMMS();

    /**
     * Returns true if this is a CMS node
     *
     * @return
     */
    boolean isCMS();

    /**
     * Returns true if this is a RMS node
     * @return
     */
    boolean isRMS();

}
