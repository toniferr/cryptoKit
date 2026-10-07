package es.toni.crypto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import es.toni.crypto.core.Algorithm;
import es.toni.crypto.core.Algorithms;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Kit;
import es.toni.crypto.core.Values;

/** Small helpers so the tests read like the app is used. */
public final class TestSupport {

    private TestSupport() {
    }

    public static Algorithm alg(String id) {
        Algorithm a = Algorithms.byId(id).orElse(null);
        assertNotNull(a, "algorithm " + id);
        return a;
    }

    /** Runs with the algorithm's default formats. */
    public static Kit.Outcome run(String id, Direction d, String input, Values v) throws CryptoException {
        Algorithm a = alg(id);
        return Kit.run(a, d, input, a.inputFormat(d), a.outputFormat(d), v);
    }

    public static String forward(String id, String input, Values v) throws CryptoException {
        return run(id, Direction.FORWARD, input, v).text();
    }

    public static String backward(String id, String input, Values v) throws CryptoException {
        return run(id, Direction.BACKWARD, input, v).text();
    }

    public static Kit.Outcome run(String id, Direction d, String input, Format in, Format out, Values v)
        throws CryptoException {
        return Kit.run(alg(id), d, input, in, out, v);
    }

    public static Values v() {
        return new Values();
    }
}
