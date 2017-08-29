package uk.ac.standrews.cs.sos.impl.context.directory;

import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.guid.exceptions.GUIDGenerationException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The ContextsContents holds all the information regarding contexts and their contents.
 * The contexts themselves are stored via the DDS.
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ContextsContents implements Serializable  {

    // Maps the context to the versions belonging to it
    private transient HashMap<IGUID, HashMap<IGUID, ContextContent>> mappings;
    
    public ContextsContents() {
        mappings = new HashMap<>();
    }

    public void addUpdateMapping(IGUID context, IGUID version, ContextContent content) {

        if (!mappings.containsKey(context)) {
            mappings.put(context, new HashMap<>());
        }

        mappings.get(context).put(version, content);
    }

    /**
     * Get the known values for the version at the given context
     *
     * @param context
     * @param version
     * @return
     */
    public ContextContent get(IGUID context, IGUID version) {

        return mappings.get(context).get(version);
    }

    public void remove(IGUID context, IGUID version) {

        mappings.get(context).remove(version);
    }

    /**
     * Checks if the version has already been apply for the given context
     *
     * @param context
     * @param version
     * @return
     */
    public boolean hasBeenProcessed(IGUID context, IGUID version) {

        return mappings.containsKey(context) && mappings.get(context).containsKey(version);
    }

    /**
     * Get a set for all the contents for a given context
     *
     * @param context
     * @return
     */
    public Set<IGUID> getContentsThatPassedPredicateTest(IGUID context) {
        HashMap<IGUID, ContextContent> contents = mappings.get(context);
        if (contents == null) {
            return Collections.emptySet();
        } else {
            return contents.entrySet()
                    .stream()
                    .filter(p -> p.getValue().predicateResult)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

        }
    }

    public Map<IGUID, ContextContent> getContentsThatPassedPredicateTestRows(IGUID context) {
        HashMap<IGUID, ContextContent> contents = mappings.get(context);
        if (contents == null) {
            return new HashMap<>();
        } else {
            return contents.entrySet()
                    .stream()
                    .filter(p -> p.getValue().predicateResult)
                    .collect(Collectors.toMap( x -> x.getKey(), x -> x.getValue()));

        }
    }

    ///////////////////
    // Serialization //
    ///////////////////

    // This method defines how the cache is serialised
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        out.writeInt(mappings.size());
        for(Map.Entry<IGUID, HashMap<IGUID, ContextContent>> mapping:mappings.entrySet()) {
            out.writeUTF(mapping.getKey().toMultiHash());

            out.writeInt(mapping.getValue().size());
            for(Map.Entry<IGUID, ContextContent> content:mapping.getValue().entrySet()) {
                out.writeUTF(content.getKey().toMultiHash());
                out.writeBoolean(content.getValue().predicateResult);
                out.writeLong(content.getValue().timestamp);
                out.writeBoolean(content.getValue().policySatisfied);
            }
        }
    }

    // This method defines how the cache is de-serialised
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        try {
            mappings = new LinkedHashMap<>();

            int numberOfContexts = in.readInt();
            for (int i = 0; i < numberOfContexts; i++) {
                String guids = in.readUTF();
                IGUID contextGUID = GUIDFactory.recreateGUID(guids);

                mappings.put(contextGUID, new LinkedHashMap<>());

                int numberOfContents = in.readInt();
                for(int j = 0; j < numberOfContents; j++) {
                    String contentGUIDS = in.readUTF();
                    IGUID contentGUID = GUIDFactory.recreateGUID(contentGUIDS);
                    boolean predicateResult = in.readBoolean();
                    long timestamp = in.readLong();
                    boolean policySatisfied = in.readBoolean();

                    ContextContent contextContent = new ContextContent();
                    contextContent.predicateResult = predicateResult;
                    contextContent.timestamp = timestamp;
                    contextContent.policySatisfied = policySatisfied;

                    mappings.get(contextGUID).put(contentGUID, contextContent);
                }
            }

        } catch (GUIDGenerationException e) {
            throw new IOException(e);
        }
    }
}
