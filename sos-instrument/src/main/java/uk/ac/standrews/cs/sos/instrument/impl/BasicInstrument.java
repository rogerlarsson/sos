package uk.ac.standrews.cs.sos.instrument.impl;

import uk.ac.standrews.cs.sos.instrument.Instrument;
import uk.ac.standrews.cs.sos.instrument.Metrics;
import uk.ac.standrews.cs.sos.instrument.OutputTYPE;
import uk.ac.standrews.cs.sos.instrument.StatsTYPE;

import java.io.*;

import static uk.ac.standrews.cs.sos.instrument.Metrics.COMMA;
import static uk.ac.standrews.cs.sos.instrument.Metrics.TAB;

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
            try (FileWriter fileWriter = new FileWriter(new File(filename + "." + outputTYPE.name().toLowerCase()), true);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

                switch (outputTYPE) {
                    case CSV:
                        bufferedWriter.write("StatsTYPE" + COMMA + "Subtype" + COMMA);
                        break;
                    case TSV:
                        bufferedWriter.write("StatsTYPE" + TAB + "Subtype" + TAB);
                }

                writeHeader(bufferedWriter, new AppMetrics(), true);
            }
        }

        System.out.println("Instrumentation output will be collected at the file: " + filename + "." + outputTYPE.name().toLowerCase() +
                " - The output will be of type: " + outputTYPE);
    }

    @Override
    public void measureDataset(File directory) throws IOException {

        try (FileWriter fileWriter = new FileWriter(new File(filename + "_node"), true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            DatasetMetrics datasetMetrics = DatasetMetrics.measure(directory);
            bufferedWriter.write(datasetMetrics.toString());
        }

        try (FileWriter fileWriter = new FileWriter(new File(filename + "_dataset.tsv"), true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            DatasetMetrics datasetMetrics = DatasetMetrics.measure(directory);
            bufferedWriter.write(datasetMetrics.tsvHeader());
            bufferedWriter.newLine();
            bufferedWriter.write(datasetMetrics.tsv());
        }
    }

    @Override
    public void measure(String message) {

       measure(StatsTYPE.any, StatsTYPE.none, message);
    }

    @Override
    public void measure(StatsTYPE statsTYPE, StatsTYPE subtype, String message) {
        measure(statsTYPE, subtype, message, -1);
    }

    @Override
    public void measure(StatsTYPE statsTYPE, StatsTYPE subtype, String message, long measure) {

        if (statistics.isEnabled(statsTYPE)) {

            try (FileWriter fileWriter = new FileWriter(new File(filename + "." + outputTYPE.name().toLowerCase()), true);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

                switch (outputTYPE) {
                    case CSV:
                        bufferedWriter.write(statsTYPE.toString() + COMMA + subtype.toString() + TAB);
                        break;
                    case TSV:
                        bufferedWriter.write(statsTYPE.toString() + TAB + subtype.toString() + TAB);
                }

                AppMetrics appMeasure = AppMetrics.measure(message);
                appMeasure.setUserMeasure(measure);
                write(bufferedWriter, appMeasure, true);

                // TODO - measure other things...network?
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void write(BufferedWriter bufferedWriter, Metrics metrics, boolean last) throws IOException {

        switch (outputTYPE) {
            case STRING:
                bufferedWriter.write(metrics.toString());
                if (last) bufferedWriter.newLine();
                break;
            case CSV:
                bufferedWriter.write(metrics.csv());
                if (!last) bufferedWriter.write(COMMA);
                break;
            case TSV:
                bufferedWriter.write(metrics.tsv());
                if (!last) bufferedWriter.write(TAB);
                break;
        }

        if (last) bufferedWriter.newLine();
    }

    private void writeHeader(BufferedWriter bufferedWriter, Metrics metrics, boolean last) throws IOException {

        switch (outputTYPE) {
            case STRING:
                break;
            case CSV:
                bufferedWriter.write(metrics.csvHeader());
                if (!last) bufferedWriter.write(COMMA);
                break;
            case TSV:
                bufferedWriter.write(metrics.tsvHeader());
                if (!last) bufferedWriter.write(TAB);
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

        Instrument instrument = new BasicInstrument(new Statistics(), OutputTYPE.TSV, "TEST");
        instrument.measure("test message");
    }

}
