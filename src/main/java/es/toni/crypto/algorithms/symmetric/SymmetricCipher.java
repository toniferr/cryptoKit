package es.toni.crypto.algorithms.symmetric;

import java.util.Arrays;

import es.toni.crypto.core.Algorithm;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Family;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Messages;
import es.toni.crypto.core.Security;

/**
 * Shared shape of the symmetric ciphers: text in, ciphertext out as hex (Base64 for the password-based ones, as
 * in version 1.0.0). Encryption uses the JDK's own provider, which is always available and signed.
 */
abstract class SymmetricCipher extends Algorithm {

    private final Format cipherFormat;

    SymmetricCipher(String id, String name, Security security, String chapter, Format cipherFormat) {
        super(id, name, Family.SYMMETRIC, security, chapter);
        this.cipherFormat = cipherFormat;
    }

    @Override
    public String verb(Direction d) {
        return d == Direction.FORWARD ? "verb.encrypt" : "verb.decrypt";
    }

    @Override
    public Format inputFormat(Direction d) {
        return d == Direction.FORWARD ? Format.TEXT : cipherFormat;
    }

    @Override
    public Format outputFormat(Direction d) {
        return d == Direction.FORWARD ? cipherFormat : Format.TEXT;
    }

    static byte[] concat(byte[]... parts) {
        int n = 0;
        for (byte[] p : parts) {
            n += p.length;
        }
        byte[] out = new byte[n];
        int off = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, off, p.length);
            off += p.length;
        }
        return out;
    }

    static byte[] slice(byte[] b, int from, int to) {
        return Arrays.copyOfRange(b, from, to);
    }

    /** Rejects keys of the wrong size with a message that lists the sizes that work. */
    static void checkKey(byte[] key, int[] sizes) throws CryptoException {
        if (key.length == 0) {
            throw new CryptoException("error.key.missing", Messages.get("param.key"));
        }
        for (int s : sizes) {
            if (key.length == s) {
                return;
            }
        }
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < sizes.length; i++) {
            list.append(i == 0 ? "" : i == sizes.length - 1 ? " " + Messages.get("word.or") + " " : ", ").append(sizes[i]);
        }
        throw new CryptoException("error.key.length", key.length, list);
    }

    static void checkKeyRange(byte[] key, int min, int max) throws CryptoException {
        if (key.length == 0) {
            throw new CryptoException("error.key.missing", Messages.get("param.key"));
        }
        if (key.length < min || key.length > max) {
            throw new CryptoException("error.key.range", key.length, min, max);
        }
    }
}
