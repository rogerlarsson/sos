package uk.ac.standrews.cs.sos.model.context.closures;

import uk.ac.standrews.cs.sos.actors.SOSAgent;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataNotFoundException;
import uk.ac.standrews.cs.sos.interfaces.manifests.Asset;

import java.util.function.Predicate;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ImageClosure extends ClosureImpl {

    private ImageClosure(SOSAgent agent, Predicate<Asset> predicate) {
        super(agent, predicate);
    }

    public boolean test(Asset asset) {

        try {
            agent.getMetadata(asset.getMetadata());
        } catch (MetadataNotFoundException e) {
            e.printStackTrace();
        }


        return false;
    }
}
