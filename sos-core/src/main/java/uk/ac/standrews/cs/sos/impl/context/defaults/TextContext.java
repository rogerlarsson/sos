package uk.ac.standrews.cs.sos.impl.context.defaults;

import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataNotFoundException;
import uk.ac.standrews.cs.sos.impl.actors.SOSAgent;
import uk.ac.standrews.cs.sos.impl.context.ContextImpl;
import uk.ac.standrews.cs.sos.impl.context.SOSPredicateImpl;
import uk.ac.standrews.cs.sos.impl.context.policies.ReplicationPolicy;
import uk.ac.standrews.cs.sos.impl.metadata.MetadataConstants;
import uk.ac.standrews.cs.sos.model.Metadata;
import uk.ac.standrews.cs.sos.model.Policy;
import uk.ac.standrews.cs.sos.model.SOSPredicate;
import uk.ac.standrews.cs.sos.model.Version;

/**
 * This is a simple context that categorises all textual content and replicates it at least two times
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class TextContext extends ContextImpl {

    @Override
    public SOSPredicate predicate() {

        // Get this node's agent
        SOSAgent agent = SOSAgent.instance();

        return new SOSPredicateImpl(p -> {
            try {
                Version version = (Version) agent.getManifest(p);

                Metadata metadata = agent.getMetadata(version.getMetadata());
                String contentType = metadata.getPropertyAsString(MetadataConstants.CONTENT_TYPE);
                return isText(contentType);

            } catch (ManifestNotFoundException | MetadataNotFoundException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    @Override
    public Policy[] policies() {
        return new Policy[]{
                new ReplicationPolicy(2, false)
        };
    }

    private boolean isText(String contentType) {
        switch(contentType.toLowerCase()) {
            case "text":
            case "text/plain":
            case "text/richtext":
            case "text/enriched":
            case "text/html":
                return true;
            default:
                return false;
        }
    }
}