package uk.ac.standrews.cs.sos.storage.implementations.filesystem;

import uk.ac.standrews.cs.sos.storage.data.Data;
import uk.ac.standrews.cs.sos.storage.exceptions.DataException;
import uk.ac.standrews.cs.sos.storage.exceptions.PersistenceException;
import uk.ac.standrews.cs.sos.storage.interfaces.Directory;
import uk.ac.standrews.cs.sos.storage.interfaces.File;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class FileBasedFile extends FileBasedStatefulObject implements File {

    private Data data;

    public FileBasedFile(Directory parent, String name) {
        super(parent, name);
        realFile = new java.io.File(parent.toFile(), name);
    }

    public FileBasedFile(Directory parent, String name, Data data) {
        this(parent, name);
        this.data = data;
    }

    @Override
    public String getPathname() {
        return logicalParent.getPathname() + "/" + name;
    }

    @Override
    public void persist() throws PersistenceException {
        createFile();
        writeData();
    }

    private void createFile() throws PersistenceException {
        if (realFile.exists()) {
            if (!realFile.isFile()) {
                throw new PersistenceException("The following " + realFile.getAbsolutePath() + " is not a file");
            }
        } else {
            try {
                if (!realFile.createNewFile()) {
                    throw new PersistenceException("Could not create the file at " + realFile.getAbsolutePath());
                }
            } catch (IOException e) {
                throw new PersistenceException("IO Exception while creating the file at " + realFile.getAbsolutePath(), e);
            }
        }
    }

    private void writeData() throws PersistenceException {
        // Write the data to the file.
        byte[] bytes = data.getState();

        try {
            FileOutputStream output_stream = new FileOutputStream(realFile);
            output_stream.write(bytes);
            output_stream.close();
        } catch (IOException e) {
            throw new PersistenceException("IO Exception while writing to the file at " + realFile.getAbsolutePath(), e);
        }
    }

    @Override
    public Data getData() throws DataException {
        if (data == null) {
            throw new DataException("The file " + getPathname() + " does not have any data");
        }

        return data;
    }
}