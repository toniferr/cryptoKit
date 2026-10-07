package es.toni.crypto.algorithms.password;

import java.util.List;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/** PBKDF2 with HMAC-SHA-256 (RFC 8018). 600,000 iterations is OWASP's 2023 recommendation. */
public final class Pbkdf2 extends PasswordHash {

    private static final String PREFIX = "$pbkdf2-sha256$";

    public Pbkdf2() {
        super("pbkdf2", "PBKDF2-HMAC-SHA256", Security.RECOMMENDED);
    }

    @Override
    List<Param> costParams() {
        return List.of(Param.number("iterations", 600_000));
    }

    static byte[] derive(byte[] password, byte[] salt, int iterations, int length) {
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(password, salt, iterations);
        return ((KeyParameter) gen.generateDerivedParameters(length * 8)).getKey();
    }

    @Override
    String hash(byte[] password, Values values) throws CryptoException {
        int iterations = values.integer("iterations", 1, 10_000_000);
        byte[] salt = Rand.bytes(16);
        return PREFIX + "i=" + iterations + "$" + b64(salt) + "$" + b64(derive(password, salt, iterations, 32));
    }

    @Override
    boolean verify(byte[] password, String encoded) throws CryptoException {
        String[] p = parts(encoded, PREFIX, 5);
        int iterations = field(p[2], "i");
        if (iterations < 1 || iterations > 10_000_000) {
            throw new CryptoException("error.encoded.invalid");
        }
        byte[] expected = unb64(p[4]);
        return same(derive(password, unb64(p[3]), iterations, expected.length), expected);
    }
}
