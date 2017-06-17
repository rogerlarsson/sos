package uk.ac.standrews.cs.sos.impl.actors;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.LEVEL;
import uk.ac.standrews.cs.castore.interfaces.IDirectory;
import uk.ac.standrews.cs.castore.interfaces.IFile;
import uk.ac.standrews.cs.sos.actors.UsersRolesService;
import uk.ac.standrews.cs.sos.exceptions.crypto.ProtectionException;
import uk.ac.standrews.cs.sos.exceptions.crypto.SignatureException;
import uk.ac.standrews.cs.sos.exceptions.storage.DataStorageException;
import uk.ac.standrews.cs.sos.exceptions.userrole.RoleNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.userrole.UserNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.userrole.UserRolePersistException;
import uk.ac.standrews.cs.sos.impl.node.LocalStorage;
import uk.ac.standrews.cs.sos.impl.roles.LocalUsersRolesDirectory;
import uk.ac.standrews.cs.sos.impl.roles.RoleImpl;
import uk.ac.standrews.cs.sos.impl.roles.UserImpl;
import uk.ac.standrews.cs.sos.impl.roles.UsersRolesCache;
import uk.ac.standrews.cs.sos.model.Role;
import uk.ac.standrews.cs.sos.model.User;
import uk.ac.standrews.cs.sos.utils.Persistence;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import java.io.IOException;
import java.util.Set;

import static uk.ac.standrews.cs.sos.constants.Internals.*;

/**
 * Service to manage Users and Roles.
 *
 * There is only one activeRole Role at a given time
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSUsersRolesService implements UsersRolesService {

    // 1. inMemory cache
    // 2. DOING - local disk
    // 3. TODO - remote
    private UsersRolesCache inMemoryCache;
    private LocalUsersRolesDirectory localDirectory;

    private LocalStorage localStorage;

    public SOSUsersRolesService(LocalStorage localStorage) {

        this.localStorage = localStorage;

        loadOrCreateCache();
        localDirectory = new LocalUsersRolesDirectory(localStorage);

        try {

            manageDefaultUser();
            manageDefaultRole();

        } catch (SignatureException | UserNotFoundException | ProtectionException | UserRolePersistException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<User> getUsers() {
        return inMemoryCache.getUsers();
    }

    @Override
    public Set<Role> getRoles() {
        return inMemoryCache.getRoles();
    }

    @Override
    public void addUser(User user) throws UserRolePersistException {

        inMemoryCache.addUser(user);
        localDirectory.addUser(user);
    }

    @Override
    public User getUser(IGUID userGUID) throws UserNotFoundException {

        return inMemoryCache.getUser(userGUID);
    }

    @Override
    public void addRole(Role role) throws UserRolePersistException {

        inMemoryCache.addRole(role);
        localDirectory.addRole(role);
    }

    @Override
    public Role getRole(IGUID roleGUID) throws RoleNotFoundException {

        return inMemoryCache.getRole(roleGUID);
    }

    @Override
    public Set<Role> getRoles(IGUID userGUID) {

        return inMemoryCache.getRoles(userGUID);
    }

    @Override
    public Role activeRole() throws RoleNotFoundException {

        return inMemoryCache.activeRole();
    }

    @Override
    public void setActiveRole(Role role) throws UserRolePersistException {

        inMemoryCache.setActiveRole(role);
        localDirectory.setActiveRole(role);
    }

    @Override
    public User activeUser() throws UserNotFoundException {
        return inMemoryCache.activeUser();
    }

    @Override
    public void setActiveUser(User user) throws UserRolePersistException {

        inMemoryCache.setActiveUser(user);
        localDirectory.setActiveUser(user);
    }

    @Override
    public void flush() {

        try {
            IDirectory cacheDir = localStorage.getNodeDirectory();

            IFile cacheFile = localStorage.createFile(cacheDir, USRO_CACHE_FILE);
            Persistence.Persist(inMemoryCache, cacheFile);

        } catch (DataStorageException | IOException e) {
            SOS_LOG.log(LEVEL.ERROR, "Unable to persist the UserRoleService info inMemoryCache and/or index");
        }
    }

    private void loadOrCreateCache() {
        try {
            IDirectory cacheDir = localStorage.getNodeDirectory();
            IFile file = localStorage.createFile(cacheDir, USRO_CACHE_FILE);
            if (file.exists()) {
                inMemoryCache = UsersRolesCache.load(file);
            }
        } catch (DataStorageException | ClassNotFoundException | IOException e) {
            SOS_LOG.log(LEVEL.ERROR, "Unable to load the UserRoleService inMemoryCache");
        }

        if (inMemoryCache == null) {
            inMemoryCache = new UsersRolesCache();
        }
    }

    private void manageDefaultUser() throws SignatureException, UserRolePersistException {

        try {
            activeUser();

        } catch (UserNotFoundException e) {

            User defaultUser = new UserImpl(DEFAULT_USER_NAME);

            addUser(defaultUser);
            setActiveUser(defaultUser);
        }
    }

    private void manageDefaultRole() throws ProtectionException, SignatureException, UserNotFoundException, UserRolePersistException {

        try {
            activeRole();

        } catch (RoleNotFoundException e) {

            User defaultUser = activeUser();
            Role defaultRole = new RoleImpl(defaultUser, DEFAULT_ROLE_NAME);

            addRole(defaultRole);
            setActiveRole(defaultRole);
        }
    }


}
