package sos.deserializers;

import com.google.gson.*;
import sos.exceptions.ManifestNotMadeException;
import sos.model.implementations.components.manifests.CompoundManifest;
import sos.model.implementations.components.manifests.ManifestConstants;
import sos.model.implementations.components.manifests.ManifestFactory;
import sos.model.implementations.utils.Content;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class CompoundManifestDeserializer implements JsonDeserializer<CompoundManifest> {

    private static Gson gson = new GsonBuilder().registerTypeAdapter(Content.class, new ContentDeserializer()).create();

    @Override
    public CompoundManifest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        String signature = obj.get(ManifestConstants.KEY_SIGNATURE).getAsString();

        JsonArray jContents = obj.getAsJsonArray(ManifestConstants.KEY_CONTENTS);
        Collection<Content> contents = new ArrayList<Content>();
        for(int i = 0; i < jContents.size(); i++) {
            Content content = gson.fromJson(jContents.get(i), Content.class);
            contents.add(content);
        }

        CompoundManifest manifest = null;
        try {
            manifest = ManifestFactory.createCompoundManifest(contents, signature);
        } catch (ManifestNotMadeException e) {
            e.printStackTrace();
        }

        return manifest;
    }
}