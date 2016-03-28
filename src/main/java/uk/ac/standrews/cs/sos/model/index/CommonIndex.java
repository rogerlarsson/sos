package uk.ac.standrews.cs.sos.model.index;

import uk.ac.standrews.cs.sos.exceptions.IndexException;
import uk.ac.standrews.cs.sos.interfaces.Index;
import uk.ac.standrews.cs.sos.interfaces.Manifest;
import uk.ac.standrews.cs.sos.model.manifests.AssetManifest;
import uk.ac.standrews.cs.sos.model.manifests.AtomManifest;
import uk.ac.standrews.cs.sos.model.manifests.CompoundManifest;
import uk.ac.standrews.cs.sos.model.manifests.ManifestConstants;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public abstract class CommonIndex implements Index {

    @Override
    public void addManifest(Manifest manifest) throws IndexException {
        String type = manifest.getManifestType();
        try {
            switch(type) {
                case ManifestConstants.ATOM:
                    addAtomManifest((AtomManifest) manifest);
                    break;
                case ManifestConstants.COMPOUND:
                    addCompoundManifest((CompoundManifest) manifest);
                    break;
                case ManifestConstants.ASSET:
                    addAssetManifest((AssetManifest) manifest);
                    break;
                default:
                    throw new IndexException();
            }
        } catch (IndexException e) {
            throw new IndexException(e);
        }
    }

    protected abstract void addAtomManifest(AtomManifest manifest) throws IndexException;

    protected abstract void addCompoundManifest(CompoundManifest manifest) throws IndexException;

    protected abstract void addAssetManifest(AssetManifest manifest) throws IndexException;

}
