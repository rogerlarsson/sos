package uk.ac.standrews.cs.sos.experiments;

import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataException;
import uk.ac.standrews.cs.sos.experiments.exceptions.ExperimentException;
import uk.ac.standrews.cs.sos.impl.locations.URILocation;
import uk.ac.standrews.cs.sos.impl.manifests.builders.AtomBuilder;
import uk.ac.standrews.cs.sos.impl.manifests.builders.VersionBuilder;
import uk.ac.standrews.cs.sos.impl.node.SOSLocalNode;
import uk.ac.standrews.cs.sos.instrument.InstrumentFactory;
import uk.ac.standrews.cs.sos.instrument.StatsTYPE;
import uk.ac.standrews.cs.sos.model.Metadata;
import uk.ac.standrews.cs.sos.model.Version;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface ExperimentUnit {

    /**
     * The setup for the experiment unit
     *
     * @throws ExperimentException
     */
    void setup() throws ExperimentException;

    /**
     * The code that runs the actual experiment for this unit
     *
     * @throws ExperimentException
     */
    void run() throws ExperimentException;


    /**
     * Add the data inside the folder to the local node
     *
     * @param node
     * @param folder
     * @throws URISyntaxException
     * @throws MetadataException
     * @throws IOException
     */
    default void addFolderContentToNode(SOSLocalNode node, File folder) throws URISyntaxException, MetadataException, IOException {

        SimpleFileVisitor<Path> fv = new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                try {
                    AtomBuilder atomBuilder = new AtomBuilder().setLocation(new URILocation(file.toUri().toString()));
                    Metadata metadata = node.getAgent().addMetadata(atomBuilder.getData()); // TODO - do this in the version builder?
                    VersionBuilder versionBuilder = new VersionBuilder()
                            .setAtomBuilder(atomBuilder)
                            .setMetadata(metadata);

                    Version version = node.getAgent().addData(versionBuilder);
                    InstrumentFactory.instance().measure(StatsTYPE.experiment, "Added version " + version.guid().toShortString() + " from URI " + file.toString());
                } catch (MetadataException | URISyntaxException e) {
                    e.printStackTrace();
                }

                return FileVisitResult.CONTINUE;
            }
        };

        long start = System.nanoTime();
        Files.walkFileTree(folder.toPath(), fv);
        System.out.println("Time to add all contents: " + (System.nanoTime() - start) / 1000000000.0 + " seconds");
    }
}
