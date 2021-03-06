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
package uk.ac.standrews.cs.sos.impl.json;

import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.Test;
import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.guid.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.SetUpTest;
import uk.ac.standrews.cs.sos.exceptions.crypto.ProtectionException;
import uk.ac.standrews.cs.sos.exceptions.crypto.SignatureException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotMadeException;
import uk.ac.standrews.cs.sos.exceptions.node.NodesCollectionException;
import uk.ac.standrews.cs.sos.impl.context.ContextManifest;
import uk.ac.standrews.cs.sos.impl.node.NodesCollectionImpl;
import uk.ac.standrews.cs.sos.impl.usro.RoleImpl;
import uk.ac.standrews.cs.sos.impl.usro.UserImpl;
import uk.ac.standrews.cs.sos.model.*;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ContextSerializerTest extends SetUpTest {

    @Test
    public void basicContextSerializer() throws NodesCollectionException, GUIDGenerationException, ManifestNotMadeException, SignatureException, ProtectionException {

        String expectedContextJSON = "" +
                "{\n" +
                "  \"type\": \"Context\",\n" +
                "  \"guid\": \"SHA256_16_950aeae4470f710fa31b5b19c165aa04f3d74f673c7625f830d1443491e9e036\",\n" +
                "  \"name\": \"TEST\",\n" +
                "  \"invariant\": \"SHA256_16_5d6419bef5cda98e2ecf2c57b9539c71b350136da3b240c0a8820024939ce5d5\",\n" +
                "  \"content\": \"SHA256_16_e85f9770df500fb74794d429dd8d32238340c845fdac48bb17fb6a87bde86547\",\n" +
                "  \"domain\": {\n" +
                "    \"type\": \"LOCAL\",\n" +
                "    \"nodes\": []\n" +
                "  },\n" +
                "  \"codomain\": {\n" +
                "    \"type\": \"SPECIFIED\",\n" +
                "    \"nodes\": [\"SHA256_16_29497892317a98d1299808516f7456fae992b88b2e50682ce31ff25c76f02caa\", \"SHA256_16_d3720e9346c08abaf7017b57cab422fc6ae7055886162bb92ca4a6cbb386c0d1\"]\n" +
                "  },\n" +
                "  \"predicate\": \"SHA256_16_57daa6858e8bdcc0e2e1ab93a1a782f2cd566186aff620fe0e7d1a545d681cab\",\n" +
                "  \"max_age\": 1,\n" +
                "  \"policies\": [\"SHA256_16_d9e4b085724893ff91d4666cd0fc63dbf98fd38b1e05952dc7b836ece28d2a84\", \"SHA256_16_acad290a502ea13384879c68d9cc481604190c46a31508ccf8bb93a4a74ee8e2\"]\n" +
                "}";

        NodesCollection domain = new NodesCollectionImpl(NodesCollectionType.LOCAL);

        Set<IGUID> codomainRefs = new LinkedHashSet<>();
        codomainRefs.add(GUIDFactory.recreateGUID("SHA256_16_29497892317a98d1299808516f7456fae992b88b2e50682ce31ff25c76f02caa"));
        codomainRefs.add(GUIDFactory.recreateGUID("SHA256_16_d3720e9346c08abaf7017b57cab422fc6ae7055886162bb92ca4a6cbb386c0d1"));
        NodesCollection codomain = new NodesCollectionImpl(codomainRefs);

        IGUID predicate = GUIDFactory.recreateGUID("SHA256_16_57daa6858e8bdcc0e2e1ab93a1a782f2cd566186aff620fe0e7d1a545d681cab");

        Set<IGUID> policies = new LinkedHashSet<>();
        policies.add(GUIDFactory.recreateGUID("SHA256_16_d9e4b085724893ff91d4666cd0fc63dbf98fd38b1e05952dc7b836ece28d2a84"));
        policies.add(GUIDFactory.recreateGUID("SHA256_16_acad290a502ea13384879c68d9cc481604190c46a31508ccf8bb93a4a74ee8e2"));

        IGUID content = GUIDFactory.recreateGUID("SHA256_16_e85f9770df500fb74794d429dd8d32238340c845fdac48bb17fb6a87bde86547");

        User user = new UserImpl("TEST");
        Role role = new RoleImpl(user, "ROLE_TEST");
        Context basicContext = new ContextManifest("TEST", domain, codomain, predicate, 1, policies, role, content);

        JSONAssert.assertEquals(expectedContextJSON, basicContext.toString(), false);
    }
}