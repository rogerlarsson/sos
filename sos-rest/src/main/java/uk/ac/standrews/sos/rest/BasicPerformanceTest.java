package uk.ac.standrews.sos.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import uk.ac.standrews.sos.ServerState;
import uk.ac.standrews.cs.sos.model.SeaConfiguration;

import javax.ws.rs.core.Application;
import java.io.IOException;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class BasicPerformanceTest extends JerseyTestNg.ContainerPerClassTest {

    @Override
    protected Application configure() {
        try {
            SeaConfiguration.setRootName("test-rest-perf");
            ServerState.startSOS();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResourceConfig()
                .packages("uk.ac.standrews.cs.rest");
    }
}
