package uk.ac.standrews.cs.sos.web.agents;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.impl.util.Base64;
import spark.Request;
import spark.Response;
import uk.ac.standrews.cs.castore.data.Data;
import uk.ac.standrews.cs.castore.data.InputStreamData;
import uk.ac.standrews.cs.guid.GUIDFactory;
import uk.ac.standrews.cs.guid.IGUID;
import uk.ac.standrews.cs.guid.exceptions.GUIDGenerationException;
import uk.ac.standrews.cs.sos.exceptions.DataNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.AtomNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.metadata.MetadataNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.userrole.RoleNotFoundException;
import uk.ac.standrews.cs.sos.impl.manifests.builders.AtomBuilder;
import uk.ac.standrews.cs.sos.impl.manifests.builders.VersionBuilder;
import uk.ac.standrews.cs.sos.impl.node.SOSLocalNode;
import uk.ac.standrews.cs.sos.model.*;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

import static uk.ac.standrews.cs.sos.web.WebApp.DATA_LIMIT;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class WData {

    public static String AddVersion(Request request, SOSLocalNode sos) throws GUIDGenerationException, RoleNotFoundException, IOException, ServletException, ManifestNotFoundException {

        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

        String roleidSign = request.params("roleidsign");
        String roleid = request.params("roleid");
        String prev = request.params("prev");

        boolean sign = !roleidSign.equals("0");
        boolean protect = !roleid.equals("0");
        boolean update = !prev.equals("0");

        try (InputStream is = request.raw().getPart("file").getInputStream()) {
            // Use the input stream to create a file
            AtomBuilder atomBuilder = new AtomBuilder()
                    .setData(new InputStreamData(is));


            if (protect) {
                IGUID roleGUID = GUIDFactory.recreateGUID(roleid);
                Role roleToProtect = sos.getRMS().getRole(roleGUID);
                atomBuilder.setRole(roleToProtect);
            }

            VersionBuilder versionBuilder = new VersionBuilder().setAtomBuilder(atomBuilder);

            if (sign) {
                IGUID signerGUID = GUIDFactory.recreateGUID(roleidSign);
                Role roleToSign = sos.getRMS().getRole(signerGUID);
                versionBuilder.setRole(roleToSign);
            }

            if (update) {
                IGUID prevGUID = GUIDFactory.recreateGUID(prev);
                Version version = (Version) sos.getDDS().getManifest(prevGUID);

                Set<IGUID> prevs = new LinkedHashSet<>();
                prevs.add(prevGUID);

                versionBuilder.setInvariant(version.getInvariantGUID())
                                .setPrevious(prevs);
            }

            sos.getAgent().addData(versionBuilder);
        }


        return "";
    }

    public static String GetData(Request req, SOSLocalNode sos) throws GUIDGenerationException, ManifestNotFoundException, MetadataNotFoundException, AtomNotFoundException {

        String guidParam = req.params("id");
        IGUID guid = GUIDFactory.recreateGUID(guidParam);
        Manifest manifest = sos.getAgent().getManifest(guid);

        if (manifest.getType().equals(ManifestType.VERSION)) {
            Version version = (Version) manifest;
            Manifest contentManifest = sos.getDDS().getManifest(version.getContentGUID());
            if (contentManifest.getType().equals(ManifestType.ATOM)) {

                return GetData(sos, version);
            }
        }

        return "N/A";
    }

    public static String GetDataDownload(Request req, Response response, SOSLocalNode sos) throws GUIDGenerationException, ManifestNotFoundException, MetadataNotFoundException, AtomNotFoundException, IOException {

        String guidParam = req.params("id");
        IGUID guid = GUIDFactory.recreateGUID(guidParam);
        Manifest manifest = sos.getAgent().getManifest(guid);

        if (manifest.getType().equals(ManifestType.VERSION)) {
            Version version = (Version) manifest;
            Manifest contentManifest = sos.getDDS().getManifest(version.getContentGUID());
            if (contentManifest.getType().equals(ManifestType.ATOM)) {

                String extension = "";
                if (version.getMetadata() != null && !version.getMetadata().isInvalid()) {
                    Metadata metadata = sos.getMMS().getMetadata(version.getMetadata());
                    String contentType = metadata.getPropertyAsString("Content-Type");
                    response.type(contentType);

                    extension = GetExtension(contentType);
                }

                response.header("Content-Disposition", "attachment; filename=\"Version-" + version.guid().toMultiHash() + extension + "\"");
                Data data = sos.getStorage().getAtomContent(version.getContentGUID());
                try(OutputStream out = response.raw().getOutputStream()) {
                    IOUtils.copy(data.getInputStream(), out);
                }
                return "";
            }
        }

        return "N/A";
    }

    public static String GetProtectedData(Request req, SOSLocalNode sos) throws GUIDGenerationException, ManifestNotFoundException, MetadataNotFoundException, AtomNotFoundException, RoleNotFoundException {

        String guidParam = req.params("id");
        IGUID guid = GUIDFactory.recreateGUID(guidParam);
        Manifest manifest = sos.getAgent().getManifest(guid);

        IGUID roleid = GUIDFactory.recreateGUID(req.params("roleid"));
        Role role = sos.getRMS().getRole(roleid);

        if (manifest.getType().equals(ManifestType.VERSION)) {
            Version version = (Version) manifest;
            Manifest contentManifest = sos.getDDS().getManifest(version.getContentGUID());
            if (contentManifest.getType().equals(ManifestType.ATOM_PROTECTED)) {

                try {
                    return GetProtectedData(sos, version, (SecureAtom) contentManifest, role);
                } catch (DataNotFoundException e) {

                    return "Unable to get Protected Data";
                }
            }
        }

        return "N/A";
    }

    public static String GetProtectedDataDownload(Request req, Response response, SOSLocalNode sos) throws GUIDGenerationException, ManifestNotFoundException, MetadataNotFoundException, AtomNotFoundException, IOException, RoleNotFoundException {

        String guidParam = req.params("id");
        IGUID guid = GUIDFactory.recreateGUID(guidParam);
        Manifest manifest = sos.getAgent().getManifest(guid);

        IGUID roleid = GUIDFactory.recreateGUID(req.params("roleid"));
        Role role = sos.getRMS().getRole(roleid);

        if (manifest.getType().equals(ManifestType.VERSION)) {
            Version version = (Version) manifest;
            Manifest contentManifest = sos.getDDS().getManifest(version.getContentGUID());
            if (contentManifest.getType().equals(ManifestType.ATOM_PROTECTED)) {

                String extension = "";
                if (version.getMetadata() != null && !version.getMetadata().isInvalid()) {
                    Metadata metadata = sos.getMMS().getMetadata(version.getMetadata());
                    String contentType = metadata.getPropertyAsString("Content-Type");
                    response.type(contentType);

                    extension = GetExtension(contentType);
                }

                response.header("Content-Disposition", "attachment; filename=\"Version-" + version.guid().toMultiHash() + extension + "\"");
                try(OutputStream out = response.raw().getOutputStream()) {
                    Data data = sos.getStorage().getSecureAtomContent((SecureAtom) contentManifest, role);
                    IOUtils.copy(data.getInputStream(), out);


                } catch (DataNotFoundException e) {
                    response.status(404);
                    return "";
                }
                return "";
            }
        }

        return "N/A";
    }

    public static String GetData(SOSLocalNode sos, Version version) throws AtomNotFoundException {

        Data data = sos.getStorage().getAtomContent(version.getContentGUID());

        String type = "Raw";
        try {
            if (version.getMetadata() != null && !version.getMetadata().isInvalid()) {
                Metadata metadata = sos.getMMS().getMetadata(version.getMetadata());
                type = metadata.getPropertyAsString("Content-Type");
            }
        } catch (MetadataNotFoundException ignored) { }

        return GetData(type, data);
    }

    public static String GetProtectedData(SOSLocalNode sos, Version version, SecureAtom atom, Role role) throws AtomNotFoundException, DataNotFoundException, ManifestNotFoundException {

        Data data = sos.getStorage().getSecureAtomContent(atom, role);

        String type = "Raw";
        try {
            if (version.getMetadata() != null && !version.getMetadata().isInvalid()) {
                Metadata metadata = sos.getMMS().getMetadata(version.getMetadata());
                type = metadata.getPropertyAsString("Content-Type");
            }
        } catch (MetadataNotFoundException ignored) { }

        return GetData(type, data);
    }

    private static String GetData(String type, Data data) {

        String outputData = "Cannot render this data type";

        switch (type) {
            case "Raw":
            case "application/octet-stream":
            case "multipart/appledouble":
            case "text/plain":
            case "text/plain; charset=ISO-8859-1":
            case "text/plain; charset=UTF-8":
            case "text/plain; charset=windows-1252":
                outputData = "<pre style=\"white-space: pre-wrap; word-wrap: break-word;\">";
                outputData += (data.toString().length() > DATA_LIMIT ? data.toString().substring(0, DATA_LIMIT) + " <br><strong>.... OTHER DATA FOLLOWING</strong>" : data.toString());
                outputData += "</pre>";
                break;
            case "image/png":
            case "image/jpeg":
            case "image/jpg":
            case "image/gif":
            case "image/tiff": // NOT TESTED
                byte b[] = data.getState();
                byte[] encodeBase64 = Base64.encode(b);
                String encodedData = new String(encodeBase64, StandardCharsets.UTF_8);
                outputData = "<img class=\"img-fluid\" src=\"data:" + type + ";base64," + encodedData + "\">";
                break;
            case "application/pdf":
                return "<i class=\"fa fa-file-pdf-o fa-5x\" aria-hidden=\"true\"></i>";
            case "application/vnd.ms-powerpoin":
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return "<i class=\"fa fa-file-powerpoint-o fa-5x\" aria-hidden=\"true\"></i>";
            case "application/msword":
                return "<i class=\"fa fa-file-word-o fa-5x\" aria-hidden=\"true\"></i>";
            case "application/zip":
            case "application/x-bzip":
            case "application/x-bzip2":
            case "application/x-tar":
            case "application/x-7z-compressed":
            case "application/x-rar-compressed":
                return "<i class=\"fa fa-file-archive-o fa-5x\" aria-hidden=\"true\"></i>";
            case "application/xhtml+xml; charset=ISO-8859-1":
                return "<i class=\"fa fa-globe fa-5x\" aria-hidden=\"true\"></i>";
        }

        return outputData;
    }

    private static String GetExtension(String type) {

        switch (type) {
            case "text/plain":
            case "text/plain; charset=ISO-8859-1":
            case "text/plain; charset=UTF-8":
            case "text/plain; charset=windows-1252":
                return ".txt";

            case "application/xhtml+xml; charset=ISO-8859-1":
                return ".html";

            case "image/png":
                return ".png";

            case "image/jpeg":
                return ".jpeg";

            case "image/jpg":
                return ".jpg";

            case "image/gif":
                return ".gif";

            case "image/tiff": // NOT TESTED
                return ".tiff";

            case "application/pdf":
                return ".pdf";

            case "application/vnd.ms-powerpoin":
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return ".pptx";

            case "application/msword":
                return ".docx";

            case "application/zip":
                return "zip";

            case "application/x-bzip":
                return ".bz";

            case "application/x-bzip2":
                return ".bz2";

            case "application/x-tar":
                return ".tar";

            case "application/x-7z-compressed":
                return ".7z";

            case "application/x-rar-compressed":
                return ".rar";

            case "Raw":
            case "application/octet-stream":
            case "multipart/appledouble":
            default:
                return "";
        }

    }

}
