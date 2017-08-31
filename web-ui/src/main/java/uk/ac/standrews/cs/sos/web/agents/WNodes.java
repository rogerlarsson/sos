package uk.ac.standrews.cs.sos.web.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import spark.Request;
import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.guid.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.exceptions.node.NodeNotFoundException;
import uk.ac.standrews.cs.sos.impl.node.SOSLocalNode;
import uk.ac.standrews.cs.sos.web.VelocityUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class WNodes {

    public static String Render(SOSLocalNode sos) throws JsonProcessingException {
        Map<String, Object> model = new HashMap<>();

        model.put("thisNode", sos.getNDS().getThisNode());
        model.put("nodes", sos.getNDS().getNodes());

        return VelocityUtils.RenderTemplate("velocity/nodes.vm", model);
    }

    public static String GetInfo(Request request, SOSLocalNode sos) throws GUIDGenerationException, NodeNotFoundException {

        IGUID nodeid = GUIDFactory.recreateGUID(request.params("nodeid"));
        return sos.getNDS().infoNode(nodeid);
    }
}
