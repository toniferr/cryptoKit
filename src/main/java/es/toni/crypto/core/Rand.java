package es.toni.crypto.core;

import java.security.SecureRandom;

/** The one source of randomness: keys, IVs, nonces and salts. */
public final class Rand {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Rand() {
    }

    public static SecureRandom secure() {
        return RANDOM;
    }

    public static byte[] bytes(int n) {
        byte[] b = new byte[n];
        RANDOM.nextBytes(b);
        return b;
    }
}
