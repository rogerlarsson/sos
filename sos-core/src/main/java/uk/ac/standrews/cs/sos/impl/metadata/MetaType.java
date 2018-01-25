package uk.ac.standrews.cs.sos.impl.metadata;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public enum MetaType {

    LONG("long"), DOUBLE("double"), BOOLEAN("boolean"), STRING("string"), GUID("guid"), ANY("any");

    private final String type;
    MetaType(String type) {
        this.type = type;
    }

    public static MetaType get(String value) {
        for(MetaType v : values())
            if(v.toString().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return type;
    }
}
