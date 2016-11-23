package uk.ac.standrews.cs.sos.model.context;

import uk.ac.standrews.cs.GUIDFactory;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.sos.interfaces.context.Closure;
import uk.ac.standrews.cs.sos.interfaces.context.Context;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ContextImpl implements Context {

    private final IGUID guid;
    private final String name;

    private Closure closure;

    private Object[] policies;

    public ContextImpl(String name, Closure closure) {
        this.name = name;
        this.closure = closure;

        guid = GUIDFactory.generateRandomGUID();
    }

    @Override
    public IGUID getGUID() {
        return guid;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setPolicies() {
        // TODO
        // this.policies =
    }

    @Override
    public void getContextPolicies() {
        // return policies;
    }

    @Override
    public Closure getClosure() {
        return closure;
    }

    @Override
    public Context AND(Context context) {
        String newName = name + "AND" + context.getName();
        Closure newClosure = closure.AND(context.getClosure());

        Context newContext = new ContextImpl(newName, newClosure);
        return newContext;
    }

    @Override
    public Context OR(Context context) {
        String newName = name + "OR" + context.getName();
        Closure newClosure = closure.OR(context.getClosure());

        Context newContext = new ContextImpl(newName, newClosure);
        return newContext;
    }
}
