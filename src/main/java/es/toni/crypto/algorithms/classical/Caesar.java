package es.toni.crypto.algorithms.classical;

import java.util.List;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Values;

/** Shift every letter a fixed number of places along the alphabet. Suetonius says Caesar used a shift of three. */
public final class Caesar extends ClassicalCipher {

    public Caesar() {
        super("caesar", "Caesar", "classical#caesar");
    }

    @Override
    public List<Param> params() {
        return List.of(Param.number("shift", 3));
    }

    @Override
    Result transform(Direction d, String text, Values values) throws CryptoException {
        int k = values.integer("shift", -25, 25);
        return text(shift(text, d == Direction.FORWARD ? k : -k));
    }

    static String shift(String text, int k) {
        StringBuilder out = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            out.append(isLetter(c) ? letter(index(c) + k, Character.isLowerCase(c)) : c);
        }
        return out.toString();
    }
}
