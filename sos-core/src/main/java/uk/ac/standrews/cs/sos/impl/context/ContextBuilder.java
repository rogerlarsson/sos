package uk.ac.standrews.cs.sos.impl.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.guid.impl.keys.InvalidID;
import uk.ac.standrews.cs.sos.constants.JSONConstants;
import uk.ac.standrews.cs.sos.exceptions.context.ContextBuilderException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotMadeException;
import uk.ac.standrews.cs.sos.impl.datamodel.CompoundManifest;
import uk.ac.standrews.cs.sos.model.*;
import uk.ac.standrews.cs.sos.utils.JSONHelper;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static uk.ac.standrews.cs.sos.constants.JSONConstants.*;
import static uk.ac.standrews.cs.sos.impl.context.ContextBuilder.ContextBuilderType.FAT;
import static uk.ac.standrews.cs.sos.impl.context.ContextBuilder.ContextBuilderType.TEMP;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ContextBuilder {

    private static final String CONTEXT_KEY = "context";

    public enum ContextBuilderType {
        FAT, TEMP
    }

    public ContextBuilderType getContextBuilderType() {
        return contextBuilderType;
    }

    private ContextBuilderType contextBuilderType;

    // FAT Context JSON
    private JsonNode contextDefinitions;

    private CompoundManifest compoundManifest;

    // TEMP
    private IGUID previous;
    private Compound contents;
    private NodesCollection domain;
    private NodesCollection codomain;
    private long maxage;


    public ContextBuilder(JsonNode contextDefinition, ContextBuilderType contextBuilderType) {
        this.contextBuilderType = contextBuilderType;
        this.contextDefinitions = contextDefinition;
    }

    public ContextBuilder(IGUID previous, Compound contents, NodesCollection domain, NodesCollection codomain, long maxage) {
        this.contextBuilderType = TEMP;

        this.previous = previous;
        this.contents = contents;
        this.domain = domain;
        this.codomain = codomain;
        this.maxage = maxage;
    }

    public JsonNode context(IGUID predicate, Set<IGUID> policies) throws ContextBuilderException {

        if (contextBuilderType != FAT) {
            throw new ContextBuilderException();
        }

        JsonNode context = contextDefinitions.get(CONTEXT_KEY);

        ((ObjectNode)context).put(JSONConstants.KEY_CONTEXT_PREDICATE, predicate.toMultiHash());
        ArrayNode arrayNode = ((ObjectNode)context).putArray(JSONConstants.KEY_CONTEXT_POLICIES);
        for(IGUID policy:policies) {
            arrayNode.add(policy.toMultiHash());
        }

        IGUID content;
        try {
            // Reference to empty compound
            compoundManifest = new CompoundManifest(CompoundType.COLLECTION, new LinkedHashSet<>(), null);
            content = compoundManifest.guid();
        } catch (ManifestNotMadeException e) {
            content = new InvalidID();
        }

        ((ObjectNode)context).put(JSONConstants.KEY_CONTEXT_CONTENT, content.toMultiHash());

        return context;
    }

    public JsonNode predicate() throws ContextBuilderException {

        if (contextBuilderType == FAT) {
            return contextDefinitions.get(KEY_CONTEXT_PREDICATE);
        }

        throw new ContextBuilderException();
    }

    public JsonNode policies() throws ContextBuilderException {

        if (contextBuilderType == FAT) {
            return contextDefinitions.get(KEY_CONTEXT_POLICIES);
        }

        throw new ContextBuilderException();
    }

    public CompoundManifest getCompoundManifest() {
        return compoundManifest;
    }

    public IGUID getPrevious() {
        return previous;
    }

    public Compound getContents() {
        return contents;
    }

    public NodesCollection getDomain() {
        return domain;
    }

    public NodesCollection getCodomain() {
        return codomain;
    }

    public long getMaxage() {
        return maxage;
    }

    /**
     * Convert a context and its predicate and policies to a FAT JSON string.
     *
     * This method works similarly to the ContextSerializer, but this one produces a FAT JSON.
     *
     * @param context object
     * @param predicate of the context
     * @param policies of the context
     * @return FAT Context format
     */
    public static String toFATString(Context context, Predicate predicate, Set<Policy> policies) throws IOException {

        ObjectNode objectNode = JSONHelper.jsonObjMapper().createObjectNode();

        ObjectNode contextNode = objectNode.putObject(CONTEXT_KEY);
        contextNode.put(KEY_CONTEXT_NAME, context.getName());
        contextNode.putPOJO(KEY_CONTEXT_DOMAIN, context.domain());
        contextNode.putPOJO(KEY_CONTEXT_CODOMAIN, context.codomain());
        contextNode.put(KEY_CONTEXT_MAX_AGE, context.maxAge());

        ObjectNode objPredicate = removeField(predicate, KEY_GUID);
        objectNode.set(KEY_CONTEXT_PREDICATE, objPredicate);

        ArrayNode objPolicies = removeFieldInArray(policies, KEY_GUID);
        objectNode.set(KEY_CONTEXT_POLICIES, objPolicies);

        return JSONHelper.jsonObjMapper().writeValueAsString(objectNode);
    }

    private static ObjectNode removeField(Object object, String field) throws IOException {

        ObjectNode jsonNode = (ObjectNode) JSONHelper.jsonObjMapper().readTree(
                JSONHelper.jsonObjMapper().writeValueAsString(object)
        );
        jsonNode.remove(field);

        return jsonNode;
    }

    private static ArrayNode removeFieldInArray(Set<?> objects, String field) throws IOException {

        ArrayNode arrayNode = (ArrayNode) JSONHelper.jsonObjMapper().readTree(
                JSONHelper.jsonObjMapper().writeValueAsString(objects)
        );

        Iterator<JsonNode> it = arrayNode.elements();
        while(it.hasNext()) {

            ((ObjectNode) it.next()).remove(field);
        }

        return arrayNode;
    }

}
