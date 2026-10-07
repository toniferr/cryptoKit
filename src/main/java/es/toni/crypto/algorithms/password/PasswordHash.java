package es.toni.crypto.algorithms.password;

import java.util.ArrayList;
import java.util.List;

import es.toni.crypto.core.Algorithm;
import es.toni.crypto.core.Codec;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Family;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/**
 * A deliberately slow, salted password hash. Hashing produces a self-describing string (algorithm, cost, salt and
 * hash, in the usual "$id$params$salt$hash" style); verifying recomputes it from the password and compares.
 */
abstract class PasswordHash extends Algorithm {

    PasswordHash(String id, String name, Security security) {
        super(id, name, Family.PASSWORD, security, "passwords");
    }

    /** The cost parameters, used when hashing (verifying reads them from the encoded hash). */
    abstract List<Param> costParams();

    abstract String hash(byte[] password, Values values) throws CryptoException;

    abstract boolean verify(byte[] password, String encoded) throws CryptoException;

    @Override
    public final List<Param> params() {
        List<Param> ps = new ArrayList<>();
        for (Param p : costParams()) {
            ps.add(p.only(Param.FORWARD));
        }
        ps.add(Param.text("encoded", "").only(Param.BACKWARD));
        return ps;
    }

    @Override
    public String verb(Direction d) {
        return d == Direction.FORWARD ? "verb.hash" : "verb.verify";
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
    public Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        if (d == Direction.FORWARD) {
            return Result.of(Codec.utf8(hash(input, values))).note("note.password.salt");
        }
        String encoded = values.text("encoded").strip();
        if (encoded.isEmpty()) {
            throw new CryptoException("error.encoded.missing");
        }
        return Result.verdict(verify(input, encoded), Codec.utf8(encoded));
    }

    // ------------------------------------------------------------------ helpers for the $...$ format

    /** Unpadded Base64, as in the PHC string format. */
    static String b64(byte[] b) {
        return java.util.Base64.getEncoder().withoutPadding().encodeToString(b);
    }

    static byte[] unb64(String s) throws CryptoException {
        try {
            return Codec.unbase64(s);
        } catch (CryptoException e) {
            throw new CryptoException("error.encoded.invalid");
        }
    }

    /** "m=19456,t=2,p=1" -> the value of one key. */
    static int field(String params, String key) throws CryptoException {
        for (String kv : params.split(",")) {
            String[] p = kv.split("=", 2);
            if (p.length == 2 && p[0].equals(key)) {
                try {
                    return Integer.parseInt(p[1]);
                } catch (NumberFormatException e) {
                    break;
                }
            }
        }
        throw new CryptoException("error.encoded.invalid");
    }

    static String[] parts(String encoded, String prefix, int n) throws CryptoException {
        if (!encoded.startsWith(prefix)) {
            throw new CryptoException("error.encoded.otherAlgorithm");
        }
        String[] p = encoded.split("\\$");
        if (p.length != n) {
            throw new CryptoException("error.encoded.invalid");
        }
        return p;
    }

    /** Constant-time comparison, so the time taken does not leak how many bytes matched. */
    static boolean same(byte[] a, byte[] b) {
        return java.security.MessageDigest.isEqual(a, b);
    }
}
