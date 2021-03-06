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
package uk.ac.standrews.cs.sos.impl.datamodel.locations;

import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.guid.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.model.Location;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Objects;

/**
 * sos://machine-id/object-GUID
 *
 * How to register custom schemes in Java
 * http://stackoverflow.com/questions/26363573/registering-and-using-a-custom-java-net-url-protocol
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSLocation implements Location {

    private static final String SOS_SCHEME = "sos";
    private static final String SCHEME_DIVIDER = "://";
    private static final int MACHINE_ID_SEGMENT = 0;
    private static final int ENTIY_ID_SEGMENT = 1;

    private final IGUID machineID;
    private final IGUID entity;
    private final URL url;

    public SOSLocation(IGUID machineID, IGUID entity) throws MalformedURLException {
        this.machineID = machineID;
        this.entity = entity;
        url = new URL(toString());
    }

    public SOSLocation(String location) throws MalformedURLException {
        String[] segments = location.split(SCHEME_DIVIDER)[1].split("/");

        if (segments.length != 2) {
            throw new MalformedURLException();
        }

        try {
            this.machineID = GUIDFactory.recreateGUID(segments[MACHINE_ID_SEGMENT]);
            this.entity = GUIDFactory.recreateGUID(segments[ENTIY_ID_SEGMENT]);
        } catch (GUIDGenerationException e) {
            throw new MalformedURLException();
        }
        url = new URL(location);
    }

    @Override
    public URI getURI() throws URISyntaxException {
        return url.toURI();
    }

    @Override
    public InputStream getSource() throws IOException {
        URLConnection connection = url.openConnection();
        return connection.getInputStream();
    }


    public IGUID getMachineID() {
        return machineID;
    }

    public IGUID getEntityID() {
        return entity;
    }

    @Override
    public String toString() {
        return SOS_SCHEME + SCHEME_DIVIDER +
                machineID.toMultiHash() + "/" + entity.toMultiHash();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SOSLocation that = (SOSLocation) o;
        return Objects.equals(machineID, that.machineID) &&
                Objects.equals(entity, that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(machineID, entity);
    }
}
