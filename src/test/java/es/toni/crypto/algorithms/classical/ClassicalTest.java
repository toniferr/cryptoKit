package es.toni.crypto.algorithms.classical;

import static es.toni.crypto.TestSupport.backward;
import static es.toni.crypto.TestSupport.forward;
import static es.toni.crypto.TestSupport.v;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Values;

class ClassicalTest {

    @Test
    void caesarKeepsCaseAndPunctuation() throws CryptoException {
        assertEquals("Khoor, Zruog!", forward("caesar", "Hello, World!", v()));
        assertEquals("Hello, World!", backward("caesar", "Khoor, Zruog!", v()));
        assertEquals("NOP", forward("caesar", "ABC", v().set("shift", "13")));
        assertEquals("XYZ", forward("caesar", "ABC", v().set("shift", "-3")));
    }

    @Test
    void vigenereClassicExample() throws CryptoException {
        Values key = v().set("keyword", "LEMON");
        assertEquals("LXFOPVEFRNHR", forward("vigenere", "ATTACKATDAWN", key));
        assertEquals("Lxfopv ef rnhr", forward("vigenere", "Attack at dawn", v().set("keyword", "lemon")));
        assertEquals("ATTACKATDAWN", backward("vigenere", "LXFOPVEFRNHR", key));
        assertThrows(CryptoException.class, () -> forward("vigenere", "abc", v().set("keyword", "123")));
    }

    /** The example in Wikipedia's article on Playfair. */
    @Test
    void playfair() throws CryptoException {
        Values key = v().set("keyword", "playfair example");
        assertEquals("BMODZBXDNABEKUDMUIXMMOUVIF", forward("playfair", "Hide the gold in the tree stump", key));
        assertEquals("HIDETHEGOLDINTHETREXESTUMP", backward("playfair", "BMODZBXDNABEKUDMUIXMMOUVIF", key));
        assertEquals("PLAYFIREXMBCDGHKNOQSTUVWZ", Playfair.square("playfair example"));
    }

    @Test
    void enigmaKnownAnswer() throws CryptoException {
        // Rotors I II III, reflector B, rings AAA, start AAA, no plugs: the textbook test.
        assertEquals("BDZGO", forward("enigma", "AAAAA", v()));
        assertEquals("ILBDAAMTAZ", forward("enigma", "HELLOWORLD", v()));
    }

    @Test
    void enigmaIsItsOwnInverse() throws CryptoException {
        Values s = v().set("rotors", "IV II V").set("reflector", "C").set("rings", "02 21 12")
            .set("positions", "B L A").set("plugboard", "AV BS CG DL FU HZ IN KM OW RX");
        String plain = "Wetterbericht: keine besonderen Ereignisse";
        String c = forward("enigma", plain, s);
        assertEquals(plain.toUpperCase(), backward("enigma", c, s));
    }

    /** The middle rotor turns twice in a row ("double step"): A D U, A D V, A E W, B F X, B F Y. */
    @Test
    void enigmaDoubleStep() throws CryptoException {
        Enigma.Machine m = Enigma.configure(v().set("rotors", "I II III").set("reflector", "B").set("rings", "AAA")
            .set("positions", "ADU").set("plugboard", ""));
        String[] expected = {"A D V", "A E W", "B F X", "B F Y"};
        for (String e : expected) {
            m.press(0);
            assertEquals(e, m.window());
        }
    }

    @Test
    void enigmaNeverEncryptsALetterToItself() throws CryptoException {
        String c = forward("enigma", "A".repeat(500), v());
        assertEquals(-1, c.indexOf('A'));
    }

    @Test
    void enigmaRejectsBadSettings() {
        assertThrows(CryptoException.class, () -> forward("enigma", "A", v().set("rotors", "I I II")));
        assertThrows(CryptoException.class, () -> forward("enigma", "A", v().set("rotors", "I II VI")));
        assertThrows(CryptoException.class, () -> forward("enigma", "A", v().set("plugboard", "AB AC")));
        assertThrows(CryptoException.class, () -> forward("enigma", "A", v().set("positions", "AB")));
    }
}
