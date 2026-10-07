package es.toni.crypto.core;

/**
 * Runs an algorithm on text boxes: decodes the input with its format, applies the operation and encodes the output.
 * Shared by the desktop app and the command line, so both behave the same.
 */
public final class Kit {

    /** The output text plus everything worth telling the user about it. */
    public record Outcome(String text, Format format, Boolean verified, Result result, long nanos, int inBytes) {
    }

    private Kit() {
    }

    public static Outcome run(Algorithm a, Direction d, String input, Format in, Format out, Values values)
        throws CryptoException {
        values.withDefaults(a);
        byte[] bytes;
        try {
            bytes = in.decode(in == Format.TEXT ? input : input.strip());
        } catch (CryptoException e) {
            throw new CryptoException("error.input.format", Messages.get(in.key()));
        }
        if (bytes.length == 0 && !allowsEmpty(a, d)) {
            throw new CryptoException("error.input.empty");
        }
        long t0 = System.nanoTime();
        Result r;
        try {
            r = a.apply(d, bytes, values);
        } catch (CryptoException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new CryptoException(e, "error.unexpected", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        long nanos = System.nanoTime() - t0;
        Format f = out;
        if (f == Format.TEXT && !Codec.isUtf8(r.data())) {
            // Ciphertext is not text: show it in hex rather than as replacement characters.
            f = Format.HEX;
            r.note("note.notText");
        }
        return new Outcome(f.encode(r.data()), f, r.verified(), r, nanos, bytes.length);
    }

    /** Hashing, signing or encrypting the empty message is legitimate; decoding or decrypting nothing is not. */
    private static boolean allowsEmpty(Algorithm a, Direction d) {
        Family f = a.family();
        boolean messageIn = f == Family.HASH || f == Family.MAC || f == Family.SIGNATURE;
        return messageIn || d == Direction.FORWARD && (f == Family.SYMMETRIC || f == Family.ASYMMETRIC);
    }
}
