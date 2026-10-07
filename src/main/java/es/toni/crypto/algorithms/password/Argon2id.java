package es.toni.crypto.algorithms.password;

import java.util.List;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/**
 * Argon2id (Biryukov, Dinu and Khovratovich), winner of the Password Hashing Competition in 2015 and RFC 9106. The
 * defaults (19 MiB, 2 passes, 1 lane) are OWASP's minimum recommendation.
 */
public final class Argon2id extends PasswordHash {

    private static final String PREFIX = "$argon2id$";

    public Argon2id() {
        super("argon2id", "Argon2id", Security.RECOMMENDED);
    }

    @Override
    List<Param> costParams() {
        return List.of(Param.number("memory", 19_456), Param.number("passes", 2), Param.number("lanes", 1));
    }

    /** Raw Argon2id, also used to turn a password into an AES key. */
    public static byte[] derive(byte[] password, byte[] salt, int memoryKiB, int passes, int lanes, int length) {
        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withMemoryAsKB(memoryKiB)
            .withIterations(passes)
            .withParallelism(lanes)
            .withSalt(salt)
            .build();
        Argon2BytesGenerator gen = new Argon2BytesGenerator();
        gen.init(params);
        byte[] out = new byte[length];
        gen.generateBytes(password, out);
        return out;
    }

    @Override
    String hash(byte[] password, Values values) throws CryptoException {
        int lanes = values.integer("lanes", 1, 16);
        int m = values.integer("memory", 8 * lanes, 4 * 1024 * 1024);
        int t = values.integer("passes", 1, 100);
        byte[] salt = Rand.bytes(16);
        byte[] h = derive(password, salt, m, t, lanes, 32);
        return PREFIX + "v=19$m=" + m + ",t=" + t + ",p=" + lanes + "$" + b64(salt) + "$" + b64(h);
    }

    @Override
    boolean verify(byte[] password, String encoded) throws CryptoException {
        String[] p = parts(encoded, PREFIX, 6);
        if (!"v=19".equals(p[2])) {
            throw new CryptoException("error.encoded.invalid");
        }
        int m = field(p[3], "m");
        int t = field(p[3], "t");
        int lanes = field(p[3], "p");
        if (lanes < 1 || lanes > 16 || m < 8 * lanes || m > 4 * 1024 * 1024 || t < 1 || t > 100) {
            throw new CryptoException("error.encoded.invalid");
        }
        byte[] expected = unb64(p[5]);
        return same(derive(password, unb64(p[4]), m, t, lanes, expected.length), expected);
    }
}
