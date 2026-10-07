package es.toni.crypto.algorithms.classical;

import java.util.List;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Values;

/**
 * The Wehrmacht's Enigma I: three rotors chosen from I–V, reflector B or C, ring settings, start positions and a
 * plugboard. Stepping includes the middle rotor's double step. Because of the reflector the machine is its own
 * inverse: decrypting is encrypting again from the same start positions.
 */
public final class Enigma extends ClassicalCipher {

    static final String[] ROTORS = {
        "EKMFLGDQVZNTOWYHXUSPAIBRCJ", // I
        "AJDKSIRUXBLHWTMCQGZNPYFVOE", // II
        "BDFHJLCPRTXVZNYEIWGAKMUSQO", // III
        "ESOVPZJAYQUIRHXLNFTGKDCMWB", // IV
        "VZBRGITYUPSDNHLXAWMJQOFECK", // V
    };
    static final String NOTCHES = "QEVJZ";
    static final String[] NAMES = {"I", "II", "III", "IV", "V"};
    static final String REFLECTOR_B = "YRUHQSLDPXNGOKMIEBFZCWVJAT";
    static final String REFLECTOR_C = "FVPJIAOYEDRZXWGCTKUQSBNMHL";

    public Enigma() {
        super("enigma", "Enigma I", "enigma");
    }

    @Override
    public List<Param> params() {
        return List.of(
            Param.text("rotors", "I II III"),
            Param.choice("reflector", "B", "B", "C"),
            Param.text("rings", "A A A"),
            Param.text("positions", "A A A"),
            Param.text("plugboard", "").asOptional());
    }

    /** A configured machine; {@link #press} advances it like a key press does. */
    static final class Machine {
        final int[] rotor = new int[3];   // left, middle, right: indexes into ROTORS
        final int[] ring = new int[3];
        final int[] pos = new int[3];
        final int[] plug = new int[26];
        final String reflector;

        Machine(int[] rotor, String reflector, int[] ring, int[] pos, int[] plug) {
            System.arraycopy(rotor, 0, this.rotor, 0, 3);
            System.arraycopy(ring, 0, this.ring, 0, 3);
            System.arraycopy(pos, 0, this.pos, 0, 3);
            System.arraycopy(plug, 0, this.plug, 0, 26);
            this.reflector = reflector;
        }

        private boolean atNotch(int i) {
            return pos[i] == NOTCHES.charAt(rotor[i]) - 'A';
        }

        void step() {
            boolean middle = atNotch(1);
            boolean right = atNotch(2);
            if (middle) {
                pos[0] = (pos[0] + 1) % 26;   // the double step: the middle rotor turns the left one and itself
            }
            if (middle || right) {
                pos[1] = (pos[1] + 1) % 26;
            }
            pos[2] = (pos[2] + 1) % 26;
        }

        private int through(int c, int i, boolean back) {
            String w = ROTORS[rotor[i]];
            int shift = pos[i] - ring[i];
            int in = Math.floorMod(c + shift, 26);
            int out = back ? w.indexOf((char) ('A' + in)) : w.charAt(in) - 'A';
            return Math.floorMod(out - shift, 26);
        }

        int press(int c) {
            step();
            c = plug[c];
            for (int i = 2; i >= 0; i--) {
                c = through(c, i, false);
            }
            c = reflector.charAt(c) - 'A';
            for (int i = 0; i < 3; i++) {
                c = through(c, i, true);
            }
            return plug[c];
        }

        String window() {
            return "" + (char) ('A' + pos[0]) + ' ' + (char) ('A' + pos[1]) + ' ' + (char) ('A' + pos[2]);
        }
    }

    static Machine configure(Values v) throws CryptoException {
        String[] names = v.text("rotors").trim().toUpperCase().split("[\\s,\\-]+");
        if (names.length != 3) {
            throw new CryptoException("error.enigma.rotors");
        }
        int[] rotor = new int[3];
        for (int i = 0; i < 3; i++) {
            rotor[i] = List.of(NAMES).indexOf(names[i]);
            if (rotor[i] < 0) {
                throw new CryptoException("error.enigma.rotors");
            }
            for (int j = 0; j < i; j++) {
                if (rotor[j] == rotor[i]) {
                    throw new CryptoException("error.enigma.repeated");
                }
            }
        }
        String reflector = "C".equalsIgnoreCase(v.text("reflector").trim()) ? REFLECTOR_C : REFLECTOR_B;
        int[] ring = settings(v.text("rings"), "rings");
        int[] pos = settings(v.text("positions"), "positions");
        int[] plug = new int[26];
        for (int i = 0; i < 26; i++) {
            plug[i] = i;
        }
        String pairs = v.text("plugboard").trim().toUpperCase();
        if (!pairs.isEmpty()) {
            for (String p : pairs.split("[\\s,]+")) {
                if (p.length() != 2 || !isLetter(p.charAt(0)) || !isLetter(p.charAt(1))) {
                    throw new CryptoException("error.enigma.plugboard", p);
                }
                int a = p.charAt(0) - 'A', b = p.charAt(1) - 'A';
                if (a == b || plug[a] != a || plug[b] != b) {
                    throw new CryptoException("error.enigma.plugboard", p);
                }
                plug[a] = b;
                plug[b] = a;
            }
        }
        return new Machine(rotor, reflector, ring, pos, plug);
    }

    /** Three settings given as letters (A–Z) or numbers (1–26), e.g. "A A A", "AAA" or "01 01 01". */
    private static int[] settings(String s, String param) throws CryptoException {
        String t = s.trim().toUpperCase();
        String[] parts = t.contains(" ") || t.contains(",") || t.matches(".*\\d.*") ? t.split("[\\s,]+") : t.split("");
        if (parts.length != 3) {
            throw new CryptoException("error.enigma.settings", es.toni.crypto.core.Messages.get("param." + param));
        }
        int[] out = new int[3];
        for (int i = 0; i < 3; i++) {
            String p = parts[i];
            if (p.matches("\\d{1,2}") && Integer.parseInt(p) >= 1 && Integer.parseInt(p) <= 26) {
                out[i] = Integer.parseInt(p) - 1;
            } else if (p.length() == 1 && isLetter(p.charAt(0))) {
                out[i] = p.charAt(0) - 'A';
            } else {
                throw new CryptoException("error.enigma.settings", es.toni.crypto.core.Messages.get("param." + param));
            }
        }
        return out;
    }

    @Override
    Result transform(Direction d, String text, Values values) throws CryptoException {
        Machine m = configure(values);
        StringBuilder out = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            out.append(isLetter(c) ? (char) ('A' + m.press(index(c))) : c);
        }
        return text(out.toString()).note("note.enigma.window", m.window());
    }
}
