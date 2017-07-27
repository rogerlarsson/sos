package uk.ac.standrews.cs.sos.instrument.impl;

import uk.ac.standrews.cs.sos.instrument.Instrument;
import uk.ac.standrews.cs.sos.instrument.Measure;
import uk.ac.standrews.cs.sos.instrument.OutputTYPE;
import uk.ac.standrews.cs.sos.instrument.StatsTYPE;

import java.io.*;

import static uk.ac.standrews.cs.sos.instrument.Measure.COMMA;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class BasicInstrument implements Instrument {

    private Statistics statistics;
    private OutputTYPE outputTYPE;
    private String filename;

    public BasicInstrument(Statistics statistics, OutputTYPE outputTYPE, String filename) throws IOException {
        this.statistics = statistics;
        this.outputTYPE = outputTYPE;
        this.filename = filename;

        boolean fileIsEmpty = fileIsEmpty(filename);
        if (fileIsEmpty) {
            try (FileWriter fileWriter = new FileWriter(new File(filename), true);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

                bufferedWriter.write("StatsTYPE" + COMMA);
                writeHeader(bufferedWriter, new AppMeasure(), false);
                writeHeader(bufferedWriter, new OSMeasures(), true);
            }
        }
    }

    @Override
    public void measure(String message) {

       measure(StatsTYPE.any, message);
    }

    @Override
    public void measure(StatsTYPE statsTYPE, String message) {

        if (statistics.isEnabled(statsTYPE)) {

            try (FileWriter fileWriter = new FileWriter(new File(filename), true);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

                bufferedWriter.write(statsTYPE.toString() + COMMA);

                AppMeasure appMeasure = AppMeasure.measure(message);
                write(bufferedWriter, appMeasure, false);

                OSMeasures osMeasures = OSMeasures.measure();
                write(bufferedWriter, osMeasures, true);

                // TODO - measure other things...network?
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void write(BufferedWriter bufferedWriter, Measure measure, boolean last) throws IOException {

        switch (outputTYPE) {
            case STRING:
                bufferedWriter.write(measure.toString());
                if (last) bufferedWriter.newLine();
                break;
            case CSV:
                bufferedWriter.write(measure.csv());
                if (!last) bufferedWriter.write(COMMA);
                break;
        }

        if (last) bufferedWriter.newLine();
    }

    private void writeHeader(BufferedWriter bufferedWriter, Measure measure, boolean last) throws IOException {

        switch (outputTYPE) {
            case STRING:
                break;
            case CSV:
                bufferedWriter.write(measure.csvHeader());
                if (!last) bufferedWriter.write(COMMA);
                break;
        }

        if (last) bufferedWriter.newLine();
    }

    private boolean fileIsEmpty(String filename) throws IOException {

        if (!new File(filename).exists()) return true;


        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            if (br.readLine() == null) {
                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) throws IOException {

        new BasicInstrument(new Statistics(), OutputTYPE.CSV, "TEST.json").measure("test message");
    }

}