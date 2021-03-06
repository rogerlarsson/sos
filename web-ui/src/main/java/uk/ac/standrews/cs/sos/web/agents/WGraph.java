/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module web-ui.
 *
 * web-ui is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * web-ui is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with web-ui. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.sos.web.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import spark.Request;
import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.guid.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.exceptions.ServiceException;
import uk.ac.standrews.cs.sos.exceptions.manifest.HEADNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.TIPNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.node.NodesCollectionException;
import uk.ac.standrews.cs.sos.impl.node.NodesCollectionImpl;
import uk.ac.standrews.cs.sos.impl.node.SOSLocalNode;
import uk.ac.standrews.cs.sos.model.*;
import uk.ac.standrews.cs.sos.services.Agent;
import uk.ac.standrews.cs.sos.services.ManifestsDataService;
import uk.ac.standrews.cs.sos.utils.JSONHelper;

import java.util.Set;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class WGraph {

    public static String RenderPartial(Request req, SOSLocalNode sos) throws GUIDGenerationException {

        String guidParam = req.params("id");
        if (guidParam.contains("-")) guidParam = guidParam.split("-")[1];
        IGUID guid = GUIDFactory.recreateGUID(guidParam);

        // We assume that the manifest is of type version
        try {
            Manifest selectedManifest = sos.getAgent().getManifest(guid);
            ObjectNode graph = JSONHelper.jsonObjMapper().createObjectNode();
            MakeVersionGraph(graph, sos, selectedManifest);

            return graph.toString();
        } catch (ServiceException e) {

            return JSONHelper.jsonObjMapper().createObjectNode().toString();
        }
    }

    public static String RenderAsset(Request req, SOSLocalNode sos) throws GUIDGenerationException {

        String guidParam = req.params("versionid");
        if (guidParam.contains("-")) guidParam = guidParam.split("-")[1];
        IGUID guid = GUIDFactory.recreateGUID(guidParam);

        try {
            // We assume that the manifest is of type version
            Manifest manifest = sos.getAgent().getManifest(guid);
            if (!manifest.getType().equals(ManifestType.VERSION)) return "Version manifest should be selected";

            Version version = (Version) manifest;
            ObjectNode graph = JSONHelper.jsonObjMapper().createObjectNode();
            MakeAssetGraph(graph, sos, version.invariant(), true);

            return graph.toString();
        } catch (ServiceException e) {

            return JSONHelper.jsonObjMapper().createObjectNode().toString();
        }
    }

    private static void MakeVersionGraph(ObjectNode graph, SOSLocalNode sos, Manifest selectedManifest) {

        ArrayNode nodes = ManifestNodeGraph(sos.getAgent(), sos.getMDS(), selectedManifest);
        ArrayNode edges = ManifestEdgesGraph(selectedManifest);

        graph.put("nodes", nodes);
        graph.put("edges", edges);
    }

    private static void MakeAssetGraph(ObjectNode graph, SOSLocalNode sos, IGUID invariant, boolean lookForContentEagerly) {

        ArrayNode nodes = AllVersionsNodesGraph(sos.getMDS(), invariant, lookForContentEagerly);
        ArrayNode edges = AllVersionsEdgesGraph(sos.getMDS(), invariant, lookForContentEagerly);

        graph.put("nodes", nodes);
        graph.put("edges", edges);
    }

    private static ArrayNode AllVersionsNodesGraph(ManifestsDataService manifestsDataService, IGUID invariant, boolean lookForContentEagerly) {

        ArrayNode arrayNode = JSONHelper.jsonObjMapper().createArrayNode();

        NodesCollection nodesCollection;
        try {
            nodesCollection = lookForContentEagerly ? new NodesCollectionImpl(NodesCollectionType.ANY) : new NodesCollectionImpl(NodesCollectionType.LOCAL);
        } catch (NodesCollectionException e) {
            return arrayNode;
        }

        Set<IGUID> versionRefs = manifestsDataService.getVersions(nodesCollection, invariant);
        for(IGUID versionRef:versionRefs) {

            try {
                Version version = (Version) manifestsDataService.getManifest(versionRef);

                boolean isHEAD = false;
                try {
                    IGUID head = manifestsDataService.getHead(version.invariant());
                    isHEAD = version.guid().equals(head);
                } catch (HEADNotFoundException ignored) {
                }

                boolean isTIP = false;
                try {
                    Set<IGUID> tips = manifestsDataService.getTips(version.invariant());
                    isTIP = tips.contains(version.guid());
                } catch (TIPNotFoundException ignored) {
                }


                ObjectNode node = VersionManifestNode(version, version.invariant().toMultiHash(), isHEAD, isTIP);
                arrayNode.add(node);

            } catch (ManifestNotFoundException e) {

                ObjectNode unknownNode = UnknownNode(versionRef);
                arrayNode.add(unknownNode);
            }
        }

        return arrayNode;
    }

    private static ArrayNode AllVersionsEdgesGraph(ManifestsDataService manifestsDataService, IGUID invariant, boolean lookForContentEagerly) {

        ArrayNode arrayNode = JSONHelper.jsonObjMapper().createArrayNode();

        NodesCollection nodesCollection;
        try {
            nodesCollection = lookForContentEagerly ? new NodesCollectionImpl(NodesCollectionType.ANY) : new NodesCollectionImpl(NodesCollectionType.LOCAL);
        } catch (NodesCollectionException e) {
            return arrayNode;
        }

        Set<IGUID> versionRefs = manifestsDataService.getVersions(nodesCollection, invariant);
        for(IGUID versionRef:versionRefs) {

            try {
                Version version = (Version) manifestsDataService.getManifest(versionRef);

                Set<IGUID> prevs = version.previous();
                if (prevs != null && !prevs.isEmpty()) {
                    for (IGUID prev : prevs) {
                        ObjectNode prevNode = MakeEdge(version.guid(), prev, "", "previous");
                        arrayNode.add(prevNode);
                    }
                }
            } catch (ManifestNotFoundException e) {
                // DO NOTHING
            }
        }

        return arrayNode;
    }

    // TODO - must refactor
    private static ArrayNode ManifestNodeGraph(Agent agent, ManifestsDataService manifestsDataService, Manifest manifest) {

        ArrayNode arrayNode = JSONHelper.jsonObjMapper().createArrayNode();

        if (manifest.getType() == ManifestType.VERSION) {
            Version version = (Version) manifest;

            boolean isHEAD = false;
            try {
                IGUID head = manifestsDataService.getHead(version.invariant());
                isHEAD = version.guid().equals(head);
            } catch (HEADNotFoundException ignored) { }

            boolean isTIP = false;
            try {
                Set<IGUID> tips = manifestsDataService.getTips(version.invariant());
                isTIP = tips.contains(version.guid());
            } catch (TIPNotFoundException ignored) { }


            ObjectNode node = VersionManifestNode(version, version.invariant().toMultiHash(), isHEAD, isTIP);
            arrayNode.add(node);

            // Content
            try {
                Manifest contentManifest = agent.getManifest(new NodesCollectionImpl(NodesCollectionType.LOCAL), version.content());
                ObjectNode contentNode = ManifestNode(contentManifest);
                arrayNode.add(contentNode);
            } catch (NodesCollectionException | ServiceException e) {

                ObjectNode unknownNode = UnknownNode(version.content());
                arrayNode.add(unknownNode);
            }

            // Previous
            Set<IGUID> prevs = version.previous();

            if (prevs != null && !prevs.isEmpty()) {
                for (IGUID prev : prevs) {

                    try {
                        Manifest previousManifest = agent.getManifest(new NodesCollectionImpl(NodesCollectionType.LOCAL), prev);
                        ObjectNode prevNode = ManifestNode(previousManifest, version.invariant().toMultiHash());
                        arrayNode.add(prevNode);
                    } catch (NodesCollectionException | ServiceException e) {

                        ObjectNode unknownNode = UnknownNode(prev);
                        arrayNode.add(unknownNode);
                    }
                }
            }

            // Metadata
            IGUID metaGUID = version.getMetadata();
            if (metaGUID != null && !metaGUID.isInvalid()) {
                try {
                    Metadata metadata = agent.getMetadata(new NodesCollectionImpl(NodesCollectionType.LOCAL), metaGUID);
                    ObjectNode metadataNode = ManifestNode(metadata);
                    arrayNode.add(metadataNode);
                } catch (NodesCollectionException | ServiceException e) {

                    ObjectNode unknownNode = UnknownNode(metaGUID);
                    arrayNode.add(unknownNode);
                }
            }

        } else if (manifest.getType() == ManifestType.COMPOUND) {
            ObjectNode node = ManifestNode(manifest);
            arrayNode.add(node);

            Compound compound = (Compound) manifest;
            Set<Content> contents = compound.getContents();
            for(Content content:contents) {

                try {
                    Manifest contentManifest = agent.getManifest(new NodesCollectionImpl(NodesCollectionType.LOCAL), content.getGUID());
                    String label = content.getLabel() != null ? content.getLabel() : "";
                    ObjectNode contentNode = ManifestNode(label, contentManifest);
                    arrayNode.add(contentNode);
                } catch (NodesCollectionException | ServiceException e) {

                    ObjectNode unknownNode = UnknownNode(content.getGUID());
                    arrayNode.add(unknownNode);
                }
            }
        } else { // ATOM
            ObjectNode node = ManifestNode(manifest);
            arrayNode.add(node);

            ObjectNode dataNode = DataNode(manifest.guid());
            arrayNode.add(dataNode);
        }


        return arrayNode;
    }

    private static ArrayNode ManifestEdgesGraph(Manifest manifest) {

        ArrayNode arrayNode = JSONHelper.jsonObjMapper().createArrayNode();

        if (manifest.getType() == ManifestType.VERSION) {
            Version version = (Version) manifest;

            ObjectNode objectNode = MakeEdge(version.guid(), version.content());
            arrayNode.add(objectNode);

            // Previous
            Set<IGUID> prevs = version.previous();
            if (prevs != null && !prevs.isEmpty()) {
                for (IGUID prev : prevs) {
                    ObjectNode prevNode = MakeEdge(version.guid(), prev, "", "previous");
                    arrayNode.add(prevNode);
                }
            }

            // Metadata
            IGUID metaGUID = version.getMetadata();
            if (metaGUID != null && !metaGUID.isInvalid()) {
                ObjectNode metadataNode = MakeEdge(version.guid(), metaGUID, "", "Meta");
                arrayNode.add(metadataNode);
            }

        } else if (manifest.getType() == ManifestType.COMPOUND) {
            Compound compound = (Compound) manifest;
            Set<Content> contents = compound.getContents();
            for(Content content:contents) {
                String label = content.getLabel() != null ? content.getLabel() : "";
                ObjectNode objectNode = MakeEdge(manifest.guid(), content.getGUID(), label);
                arrayNode.add(objectNode);
            }
        } else { // ATOM
            ObjectNode objectNode = MakeEdge(manifest.guid(), manifest.guid(), "DATA");
            arrayNode.add(objectNode);
        }

        return arrayNode;
    }

    private static ObjectNode ManifestNode(Manifest manifest) {
        return ManifestNode("", manifest);
    }

    private static ObjectNode ManifestNode(String label, Manifest manifest) {
        ObjectMapper mapper = JSONHelper.jsonObjMapper();

        if (!label.isEmpty()) label += "-";

        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", manifest.guid().toMultiHash());
        objectNode.put("label", "Type: " + manifest.getType() + "\nGUID: " + manifest.guid().toMultiHash().substring(0, 15));
        objectNode.put("group", manifest.getType().toString());
        objectNode.put("shape", "box");
        objectNode.put("font", mapper.createObjectNode().put("face", "monospace").put("align", "left"));

        return objectNode;
    }

    private static ObjectNode ManifestNode(Manifest manifest, String group) {
        ObjectMapper mapper =JSONHelper.jsonObjMapper();

        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", manifest.guid().toMultiHash());
        objectNode.put("label", "Type: " + manifest.getType() + "\nGUID: " + manifest.guid().toMultiHash().substring(0, 15));
        objectNode.put("group", group);
        objectNode.put("shape", "box");
        objectNode.put("font", mapper.createObjectNode().put("face", "monospace").put("align", "left"));

        return objectNode;
    }

    private static ObjectNode VersionManifestNode(Version version, String group, boolean isHEAD, boolean isTIP) {
        ObjectMapper mapper = JSONHelper.jsonObjMapper();

        String head = isHEAD ? "<b>" + isHEAD + "</b>" : isHEAD + "";
        String tip = isTIP ? "<b>" + isTIP + "</b>" : isTIP + "";

        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", version.guid().toMultiHash());
        objectNode.put("label", "Type: " + version.getType() + "\nGUID: " + version.guid().toMultiHash().substring(0, 15) + "\nHEAD: " + head + "\nTIP: " + tip);
        objectNode.put("group", group);
        objectNode.put("shape", "box");
        objectNode.put("font",
                mapper.createObjectNode()
                        .put("face", "monospace")
                        .put("align", "left")
                        .put("multi", "html"));

        return objectNode;
    }

    private static ObjectNode DataNode(IGUID guid) {
        ObjectMapper mapper = JSONHelper.jsonObjMapper();

        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", "DATA-" + guid.toMultiHash());
        objectNode.put("label", "Type: DATA\nGUID: " + guid.toMultiHash());
        objectNode.put("shape", "diamond");
        objectNode.put("font", mapper.createObjectNode().put("face", "monospace").put("align", "left"));

        return objectNode;
    }

    private static ObjectNode UnknownNode(IGUID guid) {

        ObjectMapper mapper = JSONHelper.jsonObjMapper();

        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", guid.toMultiHash());
        objectNode.put("label", "Type: unknown\nGUID: " + guid.toMultiHash());
        objectNode.put("shape", "box");
        objectNode.put("color", "#FF0000");
        objectNode.put("font", mapper.createObjectNode().put("face", "monospace").put("align", "left"));

        return objectNode;
    }

    private static ObjectNode MakeEdge(IGUID from, IGUID to) {
        return MakeEdge(from, to, "");
    }

    private static ObjectNode MakeEdge(IGUID from, IGUID to, String toPrefix) {
        return MakeEdge(from, to, toPrefix, "");
    }

    private static ObjectNode MakeEdge(IGUID from, IGUID to, String toPrefix, String label) {
        ObjectMapper mapper = JSONHelper.jsonObjMapper();

        if (!toPrefix.isEmpty()) toPrefix += "-";

        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("from", from.toMultiHash());
        objectNode.put("to", to.toMultiHash());
        objectNode.put("label", label);
        objectNode.put("arrows", "to");
        // objectNode.put("physics", "false");

        return objectNode;
    }

}
