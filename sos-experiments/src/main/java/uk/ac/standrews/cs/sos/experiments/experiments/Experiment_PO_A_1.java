package uk.ac.standrews.cs.sos.experiments.experiments;

import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.sos.exceptions.ConfigurationException;
import uk.ac.standrews.cs.sos.experiments.Experiment;
import uk.ac.standrews.cs.sos.experiments.ExperimentConfiguration;
import uk.ac.standrews.cs.sos.experiments.ExperimentUnit;
import uk.ac.standrews.cs.sos.experiments.exceptions.ExperimentException;
import uk.ac.standrews.cs.sos.instrument.InstrumentFactory;
import uk.ac.standrews.cs.sos.instrument.StatsTYPE;
import uk.ac.standrews.cs.sos.model.Role;
import uk.ac.standrews.cs.sos.services.ContextService;

import java.io.File;

/**
 * The experiment PO_A_1 investigates the performance of contexts when the policies operate on data, metadata, roles, etc
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class Experiment_PO_A_1 extends BaseExperiment implements Experiment {

    public Experiment_PO_A_1(ExperimentConfiguration experimentConfiguration) throws ExperimentException {
        super(experimentConfiguration);
    }

    @Override
    public ExperimentUnit getExperimentUnit() {
        return new ExperimentUnit_PO_A_1();
    }

    @Override
    public void finishIteration() {
        super.finishIteration();

        InstrumentFactory.instance().measure(StatsTYPE.experiment, StatsTYPE.none, "END OF EXPERIMENT PO_A_1.");
    }

    private class ExperimentUnit_PO_A_1 implements ExperimentUnit {

        private ContextService cms;

        @Override
        public void setup() throws ExperimentException {
            InstrumentFactory.instance().measure(StatsTYPE.experiment, StatsTYPE.none, "SETTING UP EXPERIMENT");
            System.out.println("Node GUID is " + node.guid().toMultiHash());

            try {
                System.out.println("Adding users/roles to node");
                addFolderUSROToNode(node, experiment);

                cms = node.getCMS();

                System.out.println("Adding content to node");
                String datasetPath = experiment.getExperimentNode().getDatasetPath();
                Role role = node.getUSRO().getRole(GUIDFactory.recreateGUID("SHA256_16_485bc6e643077d0d825d92f883ecb7bc18f5d62242e4752dd9772f21a6886317"));
                addFolderContentToNode(node, new File(datasetPath), role);

                System.out.println("Adding contexts to node");
                addContexts();

                System.out.println("Running predicates");
                cms.runPredicates();
            } catch (Exception e) {
                throw new ExperimentException();
            }
        }

        @Override
        public void run() {
            InstrumentFactory.instance().measure(StatsTYPE.experiment, StatsTYPE.none, "RUNNING EXPERIMENT");

            cms.runPolicies();
        }

        private void addContexts() throws Exception {

            addContext(cms, experiment, "no_policies");
            addContext(cms, experiment, "do_nothing_policy");

            // Roles loaded from experiment resources
            addContext(cms, experiment, "grant_access");

            // Must have multiple nodes up and running
            addContext(cms, experiment, "data_replication_1");
            addContext(cms, experiment, "manifest_replication_1");
        }

    }

    public static void main(String[] args) throws ExperimentException, ConfigurationException {

        File experimentConfigurationFile = new File(CONFIGURATION_FOLDER.replace("{experiment}", "po_a_1") + "configuration.json");
        ExperimentConfiguration experimentConfiguration = new ExperimentConfiguration(experimentConfigurationFile);

        Experiment_PO_A_1 experiment_po_A_1 = new Experiment_PO_A_1(experimentConfiguration);
        experiment_po_A_1.process();
    }

}
