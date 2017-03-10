package uk.ac.standrews.cs.sos.model.context;

import uk.ac.standrews.cs.GUIDFactory;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestVerificationException;
import uk.ac.standrews.cs.sos.interfaces.Role;
import uk.ac.standrews.cs.sos.interfaces.actors.Agent;
import uk.ac.standrews.cs.sos.interfaces.context.Rule;
import uk.ac.standrews.cs.sos.interfaces.identity.Identity;
import uk.ac.standrews.cs.sos.interfaces.model.Asset;
import uk.ac.standrews.cs.sos.interfaces.model.Context;
import uk.ac.standrews.cs.sos.model.manifests.ManifestType;

import java.util.function.Predicate;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ContextImpl implements Context {

    protected Agent agent;

    private final IGUID guid;
    private final String name;
    private Predicate<Asset> predicate;

    public ContextImpl(Agent agent, String name) {
        this.agent = agent;
        this.name = name;

        guid = GUIDFactory.generateRandomGUID();
    }

    public ContextImpl(Agent agent, String name, Predicate<Asset> predicate) {
        this.agent = agent;
        this.name = name;
        this.predicate = predicate;

        guid = GUIDFactory.generateRandomGUID();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Rule[] getRules() {
        return null;
    }

    @Override
    public boolean test(Asset asset) {
        return predicate.test(asset);
    }

    @Override
    public Context AND(Context context) {
        String newName = name + ".AND." + context.getName();

        return new ContextImpl(agent, newName, this.and(context));
    }

    @Override
    public Context OR(Context context) {
        String newName = name + ".OR." + context.getName();
        return new ContextImpl(agent, newName, this.or(context));
    }

    @Override
    public Role getOwner() {
        return null;
    }

    @Override
    public String toString() {
        return "Context GUID: " + guid + ", Name: " + name;
    }

    @Override
    public boolean verifySignature(Identity identity) throws ManifestVerificationException {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public ManifestType getType() {
        return ManifestType.CONTEXT;
    }

    @Override
    public IGUID guid() {
        return guid;
    }
}
