package uk.ac.standrews.cs.sos.web.usro;

import com.fasterxml.jackson.core.JsonProcessingException;
import spark.Request;
import spark.Response;
import uk.ac.standrews.cs.GUIDFactory;
import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.exceptions.crypto.ProtectionException;
import uk.ac.standrews.cs.sos.exceptions.crypto.SignatureException;
import uk.ac.standrews.cs.sos.exceptions.userrole.UserNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.userrole.UserRolePersistException;
import uk.ac.standrews.cs.sos.impl.node.SOSLocalNode;
import uk.ac.standrews.cs.sos.impl.roles.RoleImpl;
import uk.ac.standrews.cs.sos.impl.roles.UserImpl;
import uk.ac.standrews.cs.sos.model.Role;
import uk.ac.standrews.cs.sos.model.User;
import uk.ac.standrews.cs.sos.web.VelocityUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class WUsersRoles {

    public static String Render(SOSLocalNode sos){
        Map<String, Object> model = new HashMap<>();
        model.put("users", sos.getRMS().getUsers());
        model.put("roles", sos.getRMS().getRoles());

        return VelocityUtils.RenderTemplate("velocity/usro.vm", model);
    }

    public static String CreateUser(Request request, Response response, SOSLocalNode sos) throws JsonProcessingException {

        try {
            String username = request.queryParams("username");

            User user = new UserImpl(username);
            sos.getRMS().addUser(user);

            response.redirect("/usro");
            return "";

        } catch (SignatureException | UserRolePersistException e) {
            response.redirect("/usro");
            return "";
        }
    }

    public static String CreateRole(Request request, Response response, SOSLocalNode sos) throws JsonProcessingException {

        try {
            IGUID userGUID = GUIDFactory.recreateGUID(request.queryParams("userGUID"));
            String rolename = request.queryParams("rolename");

            User user = sos.getRMS().getUser(userGUID);

            Role role = new RoleImpl(user, rolename);
            sos.getRMS().addRole(role);

            response.redirect("/usro");
            return "";

        } catch (SignatureException | UserRolePersistException | UserNotFoundException | GUIDGenerationException | ProtectionException e) {
            response.redirect("/usro");
            return "";
        }
    }

}
