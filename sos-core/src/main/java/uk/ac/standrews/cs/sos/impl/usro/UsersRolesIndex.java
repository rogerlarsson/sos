/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module core.
 *
 * core is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * core is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with core. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.sos.impl.usro;

import uk.ac.standrews.cs.castore.interfaces.IFile;
import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.guid.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.exceptions.IgnoreException;
import uk.ac.standrews.cs.sos.exceptions.userrole.RoleNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.userrole.UserNotFoundException;
import uk.ac.standrews.cs.sos.model.Role;
import uk.ac.standrews.cs.sos.model.User;
import uk.ac.standrews.cs.sos.utils.Persistence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static uk.ac.standrews.cs.sos.utils.FileUtils.RoleFromString;
import static uk.ac.standrews.cs.sos.utils.FileUtils.UserFromString;

/**
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class UsersRolesIndex implements Serializable {

    private transient HashMap<IGUID, Set<IGUID>> usersToRoles;
    private transient Role activeRole;
    private transient User activeUser;

    private static final long serialVersionUID = 1L;
    public UsersRolesIndex() {

        usersToRoles = new HashMap<>();
    }

    public void addRole(Role role) {

        if (!usersToRoles.containsKey(role.getUser())) {
            usersToRoles.put(role.getUser(), new LinkedHashSet<>());
        }

        usersToRoles.get(role.getUser()).add(role.guid());
    }

    public Set<IGUID> getRoles(IGUID userGUID) {

        return usersToRoles.get(userGUID);
    }

    public void delete(IGUID guid) {

        if (usersToRoles.containsKey(guid)) {
            usersToRoles.remove(guid);
        } else {

            for(Map.Entry<IGUID, Set<IGUID>> entry:usersToRoles.entrySet()) {

                if (entry.getValue().contains(guid)) {
                    entry.getValue().remove(guid);
                    break;
                }
            }
        }

        if (activeUser.guid().equals(guid)) {
            activeUser = null;
        }

        if (activeRole.guid().equals(guid)) {
            activeRole = null;
        }
    }

    public Role activeRole() throws RoleNotFoundException {

        if (activeRole == null) throw new RoleNotFoundException();

        return activeRole;
    }

    public void setActiveRole(Role role) {

        addRole(role);
        this.activeRole = role;
    }

    public User activeUser() throws UserNotFoundException {

        if (activeUser == null) throw new UserNotFoundException();

        return activeUser;
    }

    public void setActiveUser(User user) {

        this.activeUser = user;
    }

    public static UsersRolesIndex load(IFile file) throws IOException, ClassNotFoundException, IgnoreException {

        UsersRolesIndex persistedCache = (UsersRolesIndex) Persistence.load(file);
        if (persistedCache == null) throw new ClassNotFoundException();

        return persistedCache;
    }

    public void clear() {
        usersToRoles = new HashMap<>();
        activeRole = null;
        activeUser = null;
    }

    // This method defines how the cache is serialised
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        if (activeUser != null) {
            out.writeBoolean(true);
            out.writeUTF(activeUser.toString());
        } else {
            out.writeBoolean(false);
        }

        if (activeRole != null) {
            out.writeBoolean(true);
            out.writeUTF(activeRole.toString());
        } else {
            out.writeBoolean(false);
        }

        out.writeInt(usersToRoles.size());
        for(Map.Entry<IGUID, Set<IGUID>> u2r:usersToRoles.entrySet()) {
            out.writeUTF(u2r.getKey().toMultiHash());
            out.writeInt(u2r.getValue().size());
            for(IGUID role:u2r.getValue()) {
                out.writeUTF(role.toMultiHash());
            }
        }
    }

    // This method defines how the cache is de-serialised
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        try {
            if (in.readBoolean()) activeUser = UserFromString(in.readUTF());
            if (in.readBoolean()) activeRole = RoleFromString(in.readUTF());

            usersToRoles = new HashMap<>();
            int numberOfUsers = in.readInt();
            for(int i = 0; i < numberOfUsers; i++) {
                IGUID userGUID = GUIDFactory.recreateGUID(in.readUTF());
                usersToRoles.put(userGUID, new LinkedHashSet<>());

                int numberOfRoles = in.readInt();
                for(int j = 0; j < numberOfRoles; j++) {
                    IGUID roleGUID = GUIDFactory.recreateGUID(in.readUTF());
                    usersToRoles.get(userGUID).add(roleGUID);
                }
            }

        } catch (GUIDGenerationException | UserNotFoundException | RoleNotFoundException e) {
            throw new IOException(e);
        }

    }
}
