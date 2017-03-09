package uk.ac.standrews.cs.sos.model.manifests.builders;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.interfaces.model.Metadata;

import java.util.Set;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class AssetBuilder {

    private IGUID content;
    private Metadata metadata;
    private IGUID invariant;
    private Set<IGUID> previousCollection;

    private boolean invariantIsSet = false;
    private boolean metadataIsSet = false;
    private boolean prevIsSet = false;

    public AssetBuilder(IGUID content) {
        this.content = content;
    }

    public AssetBuilder setInvariant(IGUID invariant) {
        if (!invariantIsSet) {
            this.invariant = invariant;
            invariantIsSet = true;
        }

        return this;
    }

    public AssetBuilder setMetadata(Metadata metadata) {
        this.metadata = metadata;
        metadataIsSet = true;

        return this;
    }

    public AssetBuilder setPrevious(Set<IGUID> previousCollection) {
        if (!prevIsSet) {
            this.previousCollection = previousCollection;
            prevIsSet = true;
        }

        return this;
    }

    public boolean hasInvariant() {
        return invariantIsSet;
    }

    public boolean hasMetadata() {
        return metadataIsSet;
    }

    public boolean hasPrevious() {
        return prevIsSet;
    }

    public IGUID getContent() {
        return content;
    }

    public IGUID getMetadataCollection() {
        if (metadata == null) {
            return null;
        }

        return metadata.guid();
    }

    public IGUID getInvariant() {
        return invariant;
    }

    public Set<IGUID> getPreviousCollection() {
        return previousCollection;
    }

}
