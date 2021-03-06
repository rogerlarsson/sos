package uk.ac.standrews.cs.sos.experiments.exceptions;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class ExperimentException extends Exception {

    public ExperimentException() {
        super();
    }

    public ExperimentException(String s) {
        super(s);
    }

    public ExperimentException(Throwable t) {
        super(t);
    }

    public ExperimentException(String s, Throwable t) {
        super(s, t);
    }
}
