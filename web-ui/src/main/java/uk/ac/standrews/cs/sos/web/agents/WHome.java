package uk.ac.standrews.cs.sos.web.agents;

import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.logger.LEVEL;
import uk.ac.standrews.cs.sos.exceptions.manifest.HEADNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.userrole.UserNotFoundException;
import uk.ac.standrews.cs.sos.impl.node.SOSLocalNode;
import uk.ac.standrews.cs.sos.model.*;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;
import uk.ac.standrews.cs.sos.web.VelocityUtils;
import uk.ac.standrews.cs.utilities.Pair;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static uk.ac.standrews.cs.sos.web.WebApp.SHORT_DATA_LIMIT;
import static uk.ac.standrews.cs.sos.web.agents.WData.GetData;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class WHome {

    public static String Render(SOSLocalNode sos) {

        Map<String, Object> model = new HashMap<>();

        Set<Map<String, Object> > assets = new LinkedHashSet<>();

        Set<IGUID> assetInvariants = sos.getDDS().getInvariants(ManifestType.VERSION);
        for(IGUID invariant : assetInvariants) {

            Map<String, Object> versionModel = new HashMap<>();
            try {
                Version version = (Version) sos.getDDS().getManifest(sos.getDDS().getHead(invariant));

                Manifest manifest = sos.getDDS().getManifest(version.content());
                if (manifest.getType().equals(ManifestType.ATOM)) {
                    String outputData = GetData(sos, version, SHORT_DATA_LIMIT, true);
                    versionModel.put("data", outputData);
                }

                versionModel.put("invariant", version.invariant());
                versionModel.put("version", version.version());
                versionModel.put("content", version.content());
                versionModel.put("contentType", manifest.getType());

            } catch (ManifestNotFoundException | HEADNotFoundException e) {
                SOS_LOG.log(LEVEL.ERROR, "WebAPP - unable to process version for HOME PAGE");
            }

            assets.add(versionModel);
        }

        model.put("assets", assets);

        Set<Pair<User, Role>> usro = new LinkedHashSet<>();
        for(Role role:sos.getRMS().getRoles()) {
            try {
                User user = sos.getRMS().getUser(role.getUser());
                usro.add(new Pair<>(user, role));
            } catch (UserNotFoundException e) { /* do nothing */ }
        }
        model.put("usro", usro);

        return VelocityUtils.RenderTemplate("velocity/index.vm", model);
    }

}
