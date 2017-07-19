package uk.ac.standrews.cs.sos.experiments.experiments;

import uk.ac.standrews.cs.sos.actors.experiments.ContextServiceExperiment;
import uk.ac.standrews.cs.sos.configuration.SOSConfiguration;
import uk.ac.standrews.cs.sos.exceptions.ConfigurationException;
import uk.ac.standrews.cs.sos.experiments.Experiment;
import uk.ac.standrews.cs.sos.impl.locations.URILocation;
import uk.ac.standrews.cs.sos.impl.manifests.builders.AtomBuilder;
import uk.ac.standrews.cs.sos.impl.manifests.builders.VersionBuilder;
import uk.ac.standrews.cs.sos.impl.node.SOSLocalNode;

import java.io.File;
import java.net.URISyntaxException;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class Experiment_X_1 extends BaseExperiment implements Experiment {

    private SOSLocalNode node;
    private ContextServiceExperiment cms;

    private int counter;

    public Experiment_X_1(String experimentConfigurationPath) throws ConfigurationException {
        super(experimentConfigurationPath);
    }

    @Override
    public void setup() throws Exception {
        super.setup();

        // TODO - update the config file so that it is possible to turn on/off some features of the SOSNode
        // TODO - put the results of the experiments in the output folder
        // for example, in this experiment I do not want to run any background threads
        File configFile = new File(CONFIGURATION_FOLDER + "x_1/x_1.json");
        SOSConfiguration configuration = new SOSConfiguration(configFile);

        node = ServerState.init(configuration);
        cms = (ContextServiceExperiment) node.getCMS();

        addContentToNode();
        addContexts();
    }

    @Override
    public void start() {
        super.start();

        counter = cms.runPredicates();
    }

    @Override
    public void finish() {
        super.finish();

        ServerState.kill();

        // TODO - delete all the downloaded content, so that the next experiment is run clean
    }


    @Override
    public void collectStats() {
        super.collectStats();

        System.out.println("Number of entities processed by the predicate: " + counter);
    }

    private void addContentToNode() throws URISyntaxException {

        // Add a bunch of data, versions, and so on here
        AtomBuilder atomBuilder = new AtomBuilder().setLocation(new URILocation("https://www.takemefishing.org/tmf/assets/images/fish/american-shad-464x170.png"));
        VersionBuilder versionBuilder = new VersionBuilder().setAtomBuilder(atomBuilder);

        node.getAgent().addData(versionBuilder);
    }

    private void addContexts() throws Exception {

        node.getCMS().addContext("" +
                "{\n" +
                "    \"name\": \"All\",\n" +
                "    \"predicate\": \"CommonPredicates.AcceptAll();\"\n" +
                "}");
    }

    public static void main(String[] args) throws Exception {

        Experiment_X_1 experiment_pr_1 = new Experiment_X_1(CONFIGURATION_FOLDER + "x_1/x_1_exp.json");
        experiment_pr_1.setup();

        experiment_pr_1.start();
        experiment_pr_1.finish();
        experiment_pr_1.collectStats();
    }
}
