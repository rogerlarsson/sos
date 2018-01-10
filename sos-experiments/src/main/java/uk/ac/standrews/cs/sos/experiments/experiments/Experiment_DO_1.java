package uk.ac.standrews.cs.sos.experiments.experiments;

import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.sos.exceptions.ConfigurationException;
import uk.ac.standrews.cs.sos.exceptions.context.ContextException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.exceptions.storage.DataStorageException;
import uk.ac.standrews.cs.sos.experiments.Experiment;
import uk.ac.standrews.cs.sos.experiments.ExperimentConfiguration;
import uk.ac.standrews.cs.sos.experiments.ExperimentUnit;
import uk.ac.standrews.cs.sos.experiments.exceptions.ExperimentException;
import uk.ac.standrews.cs.sos.impl.datamodel.builders.AtomBuilder;
import uk.ac.standrews.cs.sos.impl.datamodel.locations.URILocation;
import uk.ac.standrews.cs.sos.impl.node.NodesCollectionImpl;
import uk.ac.standrews.cs.sos.impl.protocol.TasksQueue;
import uk.ac.standrews.cs.sos.impl.protocol.tasks.TriggerPredicate;
import uk.ac.standrews.cs.sos.instrument.InstrumentFactory;
import uk.ac.standrews.cs.sos.instrument.StatsTYPE;
import uk.ac.standrews.cs.sos.model.*;
import uk.ac.standrews.cs.sos.services.ContextService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Investigate the context performance as the cardinality of its domain changes
 *
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class Experiment_DO_1 extends BaseExperiment implements Experiment {

    public Experiment_DO_1(ExperimentConfiguration experimentConfiguration) throws ExperimentException {
        super(experimentConfiguration);
    }

    public Experiment_DO_1(ExperimentConfiguration experimentConfiguration, String outputFilename) throws ExperimentException {
        super(experimentConfiguration, outputFilename);
    }

    @Override
    public ExperimentUnit getExperimentUnit() {
        return new ExperimentUnit_DO_1();
    }

    private class ExperimentUnit_DO_1 implements ExperimentUnit {

        private ContextService cms;

        @Override
        public void setup() throws ExperimentException {
            InstrumentFactory.instance().measure(StatsTYPE.experiment, StatsTYPE.none, "SETTING UP EXPERIMENT");
            System.out.println("Node GUID is " + node.guid().toMultiHash());

            try {
                cms = node.getCMS();
            } catch (Exception e) {
                throw new ExperimentException();
            }

        }

        @Override
        public void run() throws ExperimentException {

            try {
                String[] contextsToRun = new String[] {"predicate_1", "predicate_2"}; // , "predicate_3"};

                for(String contextToRun:contextsToRun) {

                    System.out.println("Adding contexts to node");
                    IGUID contextGUID = addContext(cms, experiment, contextToRun);

                    System.out.println("Spawning context to nodes in domain");
                    Context context = cms.getContext(contextGUID);
                    cms.spawnContext(context);

                    System.out.println("Adding content to nodes");
                    distributeData(context);

                    ExecutorService executorService = Executors.newFixedThreadPool(11); // 11 threads should be enough

                    List<Callable<Object>> runnables = new LinkedList<>();
                    runnables.add(cms::runPredicates);
                    runnables.addAll(triggerRemotePredicate(context));

                    System.out.println("Running Predicates");
                    long start = System.nanoTime();
                    executorService.invokeAll(runnables); // This method returns when all the calls finish
                    long duration = System.nanoTime() - start;
                    InstrumentFactory.instance().measure(StatsTYPE.predicate_remote, StatsTYPE.predicate_dataset, contextToRun, duration);

                    executorService.shutdownNow();

                    // TODO - remove data from nodes???
                }

            } catch (ContextException | IOException | InterruptedException | ManifestPersistException e) {
                throw new ExperimentException(e);
            }
        }

        // TODO - move to ExperimentUnit interface
        private void distributeData(Context context) throws IOException {

            int domainSize = context.domain().size();
            System.out.println("Domain size: " + domainSize);

            ExperimentConfiguration.Experiment.ExperimentNode experimentNode = experiment.getExperimentNode();
            String datasetPath = experimentNode.getDatasetPath();
            File folderDataset = new File(datasetPath);

            if (domainSize == 0) {
                addFolderContentToNode(node, folderDataset);

            } else {
                assert(context.domain().type() == NodesCollectionType.SPECIFIED);

                // Retrieve list of files to distribute

                File[] listOfFiles = folderDataset.listFiles();
                assert(listOfFiles != null);
                System.out.println("Total number of files: " + listOfFiles.length);

                // The split is done considering this local node too. That is why the (+1) is added to the domain size
                // The split is approximated with an upper bound
                int filesPerSublist = (listOfFiles.length + (domainSize + 1) - 1) / (domainSize + 1);
                System.out.println("Files per node: " + filesPerSublist);

                // Perform list splitting into sublists
                File[][] sublists = new File[domainSize][filesPerSublist];
                if (experimentNode.isEqual_distribution_dataset()) {

                    for(int i = 0; i < domainSize; i++) {
                        for(int j = 0; j < filesPerSublist && (i * filesPerSublist + j) < listOfFiles.length; j++) {
                            sublists[i][j] = listOfFiles[i * filesPerSublist + j];
                        }
                    }

                } else {

                    // TODO - consider ranges as specified in configuration
                    int[][] distributionSets = experimentNode.getDistribution_sets();
                }

                // Distribute sublists
                int i = 0;
                for(IGUID nodeInDomain:context.domain().nodesRefs()) {
                    distributeDataToNode(sublists[i], nodeInDomain);
                    i++;
                }

            }
        }

        private void distributeDataToNode(File[] sublist, IGUID nodeRef) throws IOException {

            System.out.println("Distribute " + sublist.length + " files to node with GUID " + nodeRef.toMultiHash());
            for(File file:sublist) {
                distributeDatumToNode(file, nodeRef);
            }
        }

        private void distributeDatumToNode(File file, IGUID nodeRef) throws IOException {

            try {
                Set<IGUID> nodes = new LinkedHashSet<>();
                nodes.add(nodeRef);
                NodesCollection remoteNode = new NodesCollectionImpl(nodes);

                Location dataLocation = new URILocation(file.getAbsolutePath());

                AtomBuilder atomBuilder = new AtomBuilder()
                        .setDoNotStoreDataLocally(true)
                        .setDoNotStoreManifestLocally(true)
                        .setReplicationFactor(2) // This node will be ignored because of params above
                        .setReplicationNodes(remoteNode)
                        .setLocation(dataLocation);

                node.getStorageService().addAtom(atomBuilder); // using this method the data is added properly to the other node

            } catch (DataStorageException | ManifestPersistException | URISyntaxException e) {
                throw new IOException("Unable to distribute data to remote experiment node properly");
            }
        }

        private List<Callable<Object>> triggerRemotePredicate(Context context) {

            List<Callable<Object>> runnables = new LinkedList<>();
            NodesCollection domain = context.domain();
            for(IGUID nodeRef:domain.nodesRefs()) {
                runnables.add(triggerRemotePredicate(nodeRef, context.guid()));
            }

            return runnables;
        }

        private Callable<Object> triggerRemotePredicate(IGUID nodeRef, IGUID contextGUID) {

            return () -> {
                System.out.println("Running pred for node " + nodeRef.toMultiHash());
                Node nodeToContext = node.getNDS().getNode(nodeRef);
                TriggerPredicate triggerPredicate = new TriggerPredicate(nodeToContext, contextGUID);

                TasksQueue.instance().performSyncTask(triggerPredicate);

                System.out.println("Finished to run pred for node " + nodeRef.toMultiHash() + " State: " + triggerPredicate.getState().name());
                return 0;
            };
        }

    }

    public static void main(String[] args) throws ExperimentException, ConfigurationException {

        File experimentConfigurationFile = new File(CONFIGURATION_FOLDER.replace("{experiment}", "do_1") + "configuration.json");
        ExperimentConfiguration experimentConfiguration = new ExperimentConfiguration(experimentConfigurationFile);

        Experiment_DO_1 experiment_do_1 = new Experiment_DO_1(experimentConfiguration);
        experiment_do_1.process();
    }

}
