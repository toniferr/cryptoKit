package es.toni.crypto.core;

/** The groups the algorithms are listed under, in the order the sidebar shows them. */
public enum Family {
    CLASSICAL,
    ENCODING,
    HASH,
    MAC,
    PASSWORD,
    SYMMETRIC,
    ASYMMETRIC,
    SIGNATURE;

    /** Resource-bundle key of the family's display name. */
    public String key() {
        return "family." + name().toLowerCase();
    }
}
