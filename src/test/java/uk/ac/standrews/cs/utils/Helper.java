package uk.ac.standrews.cs.utils;

import uk.ac.standrews.cs.sos.configurations.SeaConfiguration;
import uk.ac.standrews.cs.sos.model.implementations.utils.Location;

import java.io.*;
import java.net.URISyntaxException;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class Helper {

    public static String localURItoPath(Location location) throws URISyntaxException {
        return location.getLocationPath().getPath();
    }

    public static Location createDummyDataFile(SeaConfiguration configuration) throws FileNotFoundException, UnsupportedEncodingException, URISyntaxException {
        return createDummyDataFile(configuration, "testData.txt");
    }

    public static Location createDummyDataFile(SeaConfiguration configuration, String filename) throws FileNotFoundException, UnsupportedEncodingException, URISyntaxException {
        String location = configuration.getDataPath() + filename;

        File file = new File(location);
        File parent = file.getParentFile();
        if(!parent.exists() && !parent.mkdirs()){
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }

        PrintWriter writer = new PrintWriter(file);
        writer.println("The first line");
        writer.println("The second line");
        writer.close();

        return new Location("file://"+location);
    }

    public static void appendToFile(Location location, String text) throws URISyntaxException, FileNotFoundException {
        PrintWriter writer = new PrintWriter(new FileOutputStream(
                new File(Helper.localURItoPath(location)),
                true));

        writer.append(text);
        writer.close();
    }

}