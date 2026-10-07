package es.toni.crypto.algorithms.classical;

import es.toni.crypto.core.Algorithm;
import es.toni.crypto.core.Codec;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Family;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/**
 * A pen-and-paper (or rotor) cipher on the 26 letters A–Z. Text in, text out; letters keep their case where the
 * cipher allows it and everything else passes through untouched.
 */
abstract class ClassicalCipher extends Algorithm {

    ClassicalCipher(String id, String name, String chapter) {
        super(id, name, Family.CLASSICAL, Security.EDUCATIONAL, chapter);
    }

    @Override
    public String verb(Direction d) {
        return d == Direction.FORWARD ? "verb.encrypt" : "verb.decrypt";
    }

    @Override
    public Format inputFormat(Direction d) {
        return Format.TEXT;
    }

    @Override
    public Format outputFormat(Direction d) {
        return Format.TEXT;
    }

    @Override
    public final Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        return transform(d, Codec.utf8(input), values);
    }

    abstract Result transform(Direction d, String text, Values values) throws CryptoException;

    static boolean isLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    /** 0..25 for a letter of either case. */
    static int index(char c) {
        return Character.toUpperCase(c) - 'A';
    }

    static char letter(int i, boolean lower) {
        char c = (char) ('A' + Math.floorMod(i, 26));
        return lower ? Character.toLowerCase(c) : c;
    }

    /** The letters of a keyword, upper-cased; everything else dropped. */
    static String keyLetters(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (isLetter(c)) {
                b.append(Character.toUpperCase(c));
            }
        }
        return b.toString();
    }

    static Result text(String s) {
        return Result.of(Codec.utf8(s));
    }
}
