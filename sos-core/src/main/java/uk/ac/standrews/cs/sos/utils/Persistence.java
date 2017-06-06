package uk.ac.standrews.cs.sos.utils;

import uk.ac.standrews.cs.castore.exceptions.PersistenceException;
import uk.ac.standrews.cs.castore.interfaces.IFile;

import java.io.*;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class Persistence {

    public static void Persist(Object object, IFile file) throws IOException {
        if (!file.exists()) {
            try {
                file.persist();
            } catch (PersistenceException e) {
                throw new IOException(e);
            }
        }

        FileOutputStream ostream = new FileOutputStream(file.toFile());
        ObjectOutputStream p = new ObjectOutputStream(ostream);

        p.writeObject(object);
        p.flush();
        ostream.close();
    }

    public static Object Load(IFile file) throws IOException, ClassNotFoundException {

        // Check that file is not empty
        BufferedReader br = new BufferedReader(new FileReader(file.getPathname()));
        if (br.readLine() == null) {
            return null;
        }

        FileInputStream istream = new FileInputStream(file.toFile());
        ObjectInputStream q = new ObjectInputStream(istream);

        return q.readObject();
    }
}