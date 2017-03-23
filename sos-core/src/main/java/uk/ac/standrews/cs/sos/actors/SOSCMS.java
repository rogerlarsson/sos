package uk.ac.standrews.cs.sos.actors;

import uk.ac.standrews.cs.IGUID;
import uk.ac.standrews.cs.LEVEL;
import uk.ac.standrews.cs.sos.exceptions.context.ContextException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestNotFoundException;
import uk.ac.standrews.cs.sos.exceptions.manifest.ManifestPersistException;
import uk.ac.standrews.cs.sos.interfaces.actors.CMS;
import uk.ac.standrews.cs.sos.interfaces.actors.DDS;
import uk.ac.standrews.cs.sos.interfaces.model.Context;
import uk.ac.standrews.cs.sos.interfaces.model.Manifest;
import uk.ac.standrews.cs.sos.interfaces.model.PredicateComputationType;
import uk.ac.standrews.cs.sos.interfaces.model.Version;
import uk.ac.standrews.cs.sos.model.manifests.ManifestFactory;
import uk.ac.standrews.cs.sos.utils.SOS_LOG;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class SOSCMS implements CMS {

    private DDS dds;
    private List<IGUID> contexts;
    private HashMap<IGUID, List<IGUID>> mappings;

    public SOSCMS(DDS dds) {
        this.dds = dds;

        contexts = new LinkedList<>();
        mappings = new HashMap<>();

        process();
    }

    @Override
    public Version addContext(Context context) throws Exception {

        try {
            Version version = ManifestFactory.createVersionManifest(context.guid(), null, null, null, null);

            dds.addManifest(context, false);
            dds.addManifest(version, false);

            contexts.add(version.guid());

            return version;
        } catch (ManifestPersistException e) {
            throw new ContextException(e);
        }
    }

    @Override
    public Context getContext(IGUID version) {

        try {
            Manifest manifest = dds.getManifest(version);
            return (Context) dds.getManifest(((Version) manifest).getContentGUID());
        } catch (ManifestNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Iterator<IGUID> getContexts(PredicateComputationType type) {

        return contexts.iterator();
    }

    @Override
    public Iterator<IGUID> getContents(IGUID context) {

        List<IGUID> contents = mappings.get(context);
        if (contents == null) {
            return Collections.emptyIterator();
        } else {
            return contents.iterator();
        }

    }

    private void process() {

        ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);
        service.scheduleWithFixedDelay(() -> {
            SOS_LOG.log(LEVEL.INFO, "Running background contexts");

            Iterator<IGUID> it = getContexts(PredicateComputationType.PERIODICALLY);
            while (it.hasNext()) {
                IGUID c = it.next();
                Context context = getContext(c);

                // TODO - run context against all assets. Check if assets has already been run against this context!
            }

        }, 1, 1, TimeUnit.MINUTES);
    }
}
