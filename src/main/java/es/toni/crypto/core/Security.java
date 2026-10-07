package es.toni.crypto.core;

/** How much an algorithm can be trusted today. Shown as a coloured badge next to every algorithm. */
public enum Security {
    /** A sound, current choice for new work. */
    RECOMMENDED,
    /** Still unbroken, but superseded or easy to misuse (no integrity, small block, low iteration count...). */
    LEGACY,
    /** Practical attacks exist: never use it to protect anything. */
    BROKEN,
    /** A historical cipher, here to learn from: breakable by hand or with a laptop. */
    EDUCATIONAL,
    /** Not encryption at all: anyone can reverse it without a key. */
    ENCODING;

    public String key() {
        return "security." + name().toLowerCase();
    }
}
