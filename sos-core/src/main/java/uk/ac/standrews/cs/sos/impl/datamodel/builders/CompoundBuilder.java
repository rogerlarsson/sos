package uk.ac.standrews.cs.sos.impl.datamodel.builders;

import uk.ac.standrews.cs.sos.model.CompoundType;
import uk.ac.standrews.cs.sos.model.Content;
import uk.ac.standrews.cs.sos.model.Role;

import java.util.Set;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class CompoundBuilder extends ManifestBuilder {

    private CompoundType type;
    private Set<Content> contents;

    public Set<Content> getContents() {
        return contents;
    }

    public CompoundBuilder setContents(Set<Content> contents) {
        this.contents = contents;

        return this;
    }

    public CompoundType getType() {
        return type;
    }

    public CompoundBuilder setType(CompoundType type) {
        this.type = type;

        return this;
    }

    @Override
    public CompoundBuilder setRole(Role role) {
        this.role = role;

        return this;
    }

}