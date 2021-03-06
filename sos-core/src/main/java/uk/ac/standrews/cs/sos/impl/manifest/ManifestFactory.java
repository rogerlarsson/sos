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
package uk.ac.standrews.cs.sos.impl.manifest;

import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotMadeException;
import uk.ac.standrews.cs.sos.impl.datamodel.*;
import uk.ac.standrews.cs.sos.impl.datamodel.locations.bundles.LocationBundle;
import uk.ac.standrews.cs.sos.model.*;

import java.util.HashMap;
import java.util.Set;

/**
 * This factory is used to create manifests for atoms, compounds and versions.
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ManifestFactory {

    // Suppresses default constructor, ensuring non-instantiability.
    private ManifestFactory() {}

    /**
     * Creates an AtomManifest given an atom.
     *
     * @param guid of atom
     * @param locations where the atom resides.
     * @return the manifest for the atom
     */
    public static Atom createAtomManifest(IGUID guid, Set<LocationBundle> locations) {

        return new AtomManifest(guid, locations);
    }

    public static SecureAtom createSecureAtomManifest(IGUID guid, Set<LocationBundle> locations, HashMap<IGUID, String> rolesToKeys) {

        return new SecureAtomManifest(guid, locations, rolesToKeys);
    }

    public static SecureAtom createSecureAtomManifest(IGUID guid, Set<LocationBundle> locations) {

        return new SecureAtomManifest(guid, locations);
    }

    /**
     *
     * @param type of compound
     * @param contents of compound
     * @param role to sign compound
     * @return a compound manifest
     * @throws ManifestNotMadeException if compound manifest could not be created
     */
    public static Compound createCompoundManifest(CompoundType type,
                                                          Set<Content> contents,
                                                          Role role) throws ManifestNotMadeException {

        if (type == null) {
            throw new ManifestNotMadeException("No compound type specified");
        }

        return new CompoundManifest(type, contents, role);
    }

    public static SecureCompound createSecureCompoundManifest(CompoundType type,
                                                  Set<Content> contents,
                                                  Role role) throws ManifestNotMadeException {

        if (type == null) {
            throw new ManifestNotMadeException("No compound type specified");
        }

        return new SecureCompoundManifest(type, contents, role);
    }



    /**
     * Creates an VersionManifest given the content of the asset,
     * the GUID of a previous asset's version, the GUID of metadata associated to this
     * asset and an identity which will be used to sign the manifest.
     *
     * @param content - required
     * @param invariant of version
     * @param prevs of version (null is allowed)
     * @param metadata of version (null is allowed)
     * @param role to sign version (null is allowed)
     * @return an asset manifest
     * @throws ManifestNotMadeException if version manifest could not be created
     */
    public static Version createVersionManifest(IGUID content,
                                                        IGUID invariant,
                                                        Set<IGUID> prevs,
                                                        IGUID metadata,
                                                        Role role) throws ManifestNotMadeException {

        if (content == null) {
            throw new ManifestNotMadeException("Content parameters missing or null");
        }

        return new VersionManifest(invariant, content, prevs, metadata, role);
    }

}
