package es.toni.crypto.algorithms.classical;

import java.util.List;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Values;

/**
 * Bellaso's polyalphabetic cipher (1553), later credited to Vigenère: the i-th letter is shifted by the i-th letter
 * of a repeating keyword. The keyword only advances on letters.
 */
public final class Vigenere extends ClassicalCipher {

    public Vigenere() {
        super("vigenere", "Vigenère", "classical#vigenere");
    }

    @Override
    public List<Param> params() {
        return List.of(Param.text("keyword", "LEMON"));
    }

    @Override
    Result transform(Direction d, String text, Values values) throws CryptoException {
        String key = keyLetters(values.text("keyword"));
        if (key.isEmpty()) {
            throw new CryptoException("error.keyword.empty");
        }
        int sign = d == Direction.FORWARD ? 1 : -1;
        StringBuilder out = new StringBuilder(text.length());
        int j = 0;
        for (char c : text.toCharArray()) {
            if (isLetter(c)) {
                int k = key.charAt(j++ % key.length()) - 'A';
                out.append(letter(index(c) + sign * k, Character.isLowerCase(c)));
            } else {
                out.append(c);
            }
        }
        return text(out.toString());
    }
}
