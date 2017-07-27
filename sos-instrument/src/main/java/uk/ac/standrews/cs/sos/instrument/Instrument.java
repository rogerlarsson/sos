package uk.ac.standrews.cs.sos.instrument;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface Instrument {

    void measure(String message);

    void measure(StatsTYPE statsTYPE, String message);

    // TODO - maybe pass specific objects to collect stats from sos-core????
    // for example, we could have a contextInstrument object to pass to the instrument
}
