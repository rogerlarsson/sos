package uk.ac.standrews.cs.sos.experiments.experiments;

import uk.ac.standrews.cs.guid.ALGORITHM;
import uk.ac.standrews.cs.sos.constants.Internals;
import uk.ac.standrews.cs.sos.exceptions.ConfigurationException;
import uk.ac.standrews.cs.sos.experiments.Experiment;
import uk.ac.standrews.cs.sos.experiments.ExperimentConfiguration;
import uk.ac.standrews.cs.sos.experiments.ExperimentUnit;
import uk.ac.standrews.cs.sos.experiments.exceptions.ExperimentException;

import java.io.File;
import java.io.IOException;

/**
 *
 * Testing IO on data and manifests
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class Experiment_GUID_1 extends BaseExperiment implements Experiment {

    public Experiment_GUID_1(ExperimentConfiguration experimentConfiguration) throws ExperimentException {
        super(experimentConfiguration);
    }

    public Experiment_GUID_1(ExperimentConfiguration experimentConfiguration, String outputFilename) throws ExperimentException {
        super(experimentConfiguration, outputFilename);
    }

    @Override
    public ExperimentUnit getExperimentUnit() {
        return new ExperimentUnit_GUID_1();
    }

    private class ExperimentUnit_GUID_1 implements ExperimentUnit {

        @Override
        public void setup() {}

        @Override
        public void run() throws ExperimentException {

            Internals.GUID_ALGORITHM = ALGORITHM.SHA256;
            try {
                String datasetPath = experiment.getExperimentNode().getDatasetPath();
                addFolderContentToNodeAsAtoms(node, new File(datasetPath));

            } catch (IOException e) {
                e.printStackTrace();
                throw new ExperimentException();
            }

            rest_a_bit();

            Internals.GUID_ALGORITHM = ALGORITHM.SHA1;
            try {
                String datasetPath = experiment.getExperimentNode().getDatasetPath();
                addFolderContentToNodeAsAtoms(node, new File(datasetPath));

            } catch (IOException e) {
                e.printStackTrace();
                throw new ExperimentException();
            }

        }
    }

    public static void main(String[] args) throws ConfigurationException, ExperimentException {

        File experimentConfigurationFile = new File(CONFIGURATION_FOLDER.replace("{experiment}", "guid_1") + "configuration.json");
        ExperimentConfiguration experimentConfiguration = new ExperimentConfiguration(experimentConfigurationFile);

        Experiment_GUID_1 experiment_guid_1 = new Experiment_GUID_1(experimentConfiguration, "test_guid_6");
        experiment_guid_1.process();
    }
}
