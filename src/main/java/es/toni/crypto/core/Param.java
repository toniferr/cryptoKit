package es.toni.crypto.core;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * One input an algorithm needs besides the message: a key, an IV, a keyword, a cost factor... The UI builds its form
 * from these, and the command line accepts them as {@code --id=value}.
 *
 * @param id          stable identifier (also the CLI option and the resource-bundle key {@code param.<id>})
 * @param kind        what sort of field it is
 * @param when        the directions in which the field is used
 * @param defaultValue initial value ("" for none)
 * @param format      for BYTES fields, the format the value is typed in by default
 * @param choices     for CHOICE fields, the allowed values
 * @param randomBytes for BYTES fields, how many random bytes the "generate" button makes (0: no button)
 * @param optional    true if the field may be left empty
 */
public record Param(String id, Kind kind, Set<Direction> when, String defaultValue, Format format,
                    List<String> choices, int randomBytes, boolean optional) {

    public enum Kind {
        /** A line of text. */
        TEXT,
        /** A whole number. */
        NUMBER,
        /** One value out of a fixed list. */
        CHOICE,
        /** Bytes typed as text, hex or Base64 (keys, IVs, expected digests, signatures). */
        BYTES,
        /** A secret typed as text and hidden on screen. */
        PASSWORD,
        /** Several lines, e.g. a PEM key. */
        MULTILINE
    }

    public static final Set<Direction> BOTH = EnumSet.allOf(Direction.class);
    public static final Set<Direction> FORWARD = EnumSet.of(Direction.FORWARD);
    public static final Set<Direction> BACKWARD = EnumSet.of(Direction.BACKWARD);

    public static Param text(String id, String def) {
        return new Param(id, Kind.TEXT, BOTH, def, Format.TEXT, List.of(), 0, false);
    }

    public static Param number(String id, int def) {
        return new Param(id, Kind.NUMBER, BOTH, Integer.toString(def), Format.TEXT, List.of(), 0, false);
    }

    public static Param choice(String id, String def, String... choices) {
        return new Param(id, Kind.CHOICE, BOTH, def, Format.TEXT, List.of(choices), 0, false);
    }

    public static Param bytes(String id, Format format, int randomBytes) {
        return new Param(id, Kind.BYTES, BOTH, "", format, List.of(), randomBytes, false);
    }

    public static Param password(String id) {
        return new Param(id, Kind.PASSWORD, BOTH, "", Format.TEXT, List.of(), 0, false);
    }

    public static Param pem(String id, Set<Direction> when) {
        return new Param(id, Kind.MULTILINE, when, "", Format.TEXT, List.of(), 0, false);
    }

    public Param only(Set<Direction> directions) {
        return new Param(id, kind, directions, defaultValue, format, choices, randomBytes, optional);
    }

    public Param withDefault(String value) {
        return new Param(id, kind, when, value, format, choices, randomBytes, optional);
    }

    public Param asOptional() {
        return new Param(id, kind, when, defaultValue, format, choices, randomBytes, true);
    }

    public boolean usedIn(Direction d) {
        return when.contains(d);
    }

    public String labelKey() {
        return "param." + id;
    }
}
