package es.toni.crypto.algorithms.classical;

import java.util.List;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Values;

/**
 * Wheatstone's digraph cipher (1854), promoted by Lord Playfair. A 5×5 square holds the keyword's letters first
 * (I and J share a cell); pairs of letters are replaced by the opposite corners of their rectangle, or by their
 * neighbours when they share a row or a column. Repeated letters in a pair are split with an X.
 */
public final class Playfair extends ClassicalCipher {

    public Playfair() {
        super("playfair", "Playfair", "classical#playfair");
    }

    @Override
    public List<Param> params() {
        return List.of(Param.text("keyword", "PLAYFAIR EXAMPLE"));
    }

    /** The 25-letter square, row by row. */
    static String square(String keyword) {
        StringBuilder sq = new StringBuilder(25);
        String all = keyLetters(keyword).replace('J', 'I') + "ABCDEFGHIKLMNOPQRSTUVWXYZ";
        for (char c : all.toCharArray()) {
            if (sq.indexOf(String.valueOf(c)) < 0) {
                sq.append(c);
            }
        }
        return sq.toString();
    }

    /** The plaintext as digraphs: letters only, J→I, an X between doubled letters and at the end if odd. */
    static String prepare(String text) {
        String letters = keyLetters(text).replace('J', 'I');
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < letters.length()) {
            char a = letters.charAt(i);
            char b = i + 1 < letters.length() ? letters.charAt(i + 1) : 0;
            if (b == 0 || a == b) {
                out.append(a).append(a == 'X' ? 'Q' : 'X');
                i += 1;
            } else {
                out.append(a).append(b);
                i += 2;
            }
        }
        return out.toString();
    }

    @Override
    Result transform(Direction d, String text, Values values) throws CryptoException {
        String sq = square(values.text("keyword"));
        String pairs = d == Direction.FORWARD ? prepare(text) : keyLetters(text).replace('J', 'I');
        if (pairs.isEmpty()) {
            throw new CryptoException("error.input.noLetters");
        }
        if (pairs.length() % 2 != 0) {
            throw new CryptoException("error.playfair.odd");
        }
        int step = d == Direction.FORWARD ? 1 : 4;   // +1 or −1 modulo 5
        StringBuilder out = new StringBuilder(pairs.length());
        for (int i = 0; i < pairs.length(); i += 2) {
            int a = sq.indexOf(pairs.charAt(i));
            int b = sq.indexOf(pairs.charAt(i + 1));
            int ra = a / 5, ca = a % 5, rb = b / 5, cb = b % 5;
            if (ra == rb) {
                out.append(sq.charAt(ra * 5 + (ca + step) % 5)).append(sq.charAt(rb * 5 + (cb + step) % 5));
            } else if (ca == cb) {
                out.append(sq.charAt(((ra + step) % 5) * 5 + ca)).append(sq.charAt(((rb + step) % 5) * 5 + cb));
            } else {
                out.append(sq.charAt(ra * 5 + cb)).append(sq.charAt(rb * 5 + ca));
            }
        }
        return text(out.toString()).note("note.playfair.square", sq.replaceAll("(.{5})(?!$)", "$1 "));
    }
}
