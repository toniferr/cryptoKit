package es.toni.crypto.core;

import java.util.ArrayList;
import java.util.List;

/**
 * What an operation produced: the output bytes, a verdict for verifications (null otherwise) and notes worth showing
 * to the user ("a random IV was generated and prepended", "rotor positions are now B F X"...).
 */
public final class Result {

    /** A resource-bundle key plus its arguments. */
    public record Note(String key, Object... args) {
        public String text() {
            return Messages.get(key, args);
        }
    }

    private final byte[] data;
    private final Boolean verified;
    private final List<Note> notes = new ArrayList<>();

    private Result(byte[] data, Boolean verified) {
        this.data = data;
        this.verified = verified;
    }

    public static Result of(byte[] data) {
        return new Result(data, null);
    }

    public static Result verdict(boolean ok, byte[] data) {
        return new Result(data, ok);
    }

    public Result note(String key, Object... args) {
        notes.add(new Note(key, args));
        return this;
    }

    public byte[] data() {
        return data;
    }

    /** TRUE or FALSE for a verification, null for every other operation. */
    public Boolean verified() {
        return verified;
    }

    public List<Note> notes() {
        return List.copyOf(notes);
    }
}
