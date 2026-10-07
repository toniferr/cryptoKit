package es.toni.crypto.algorithms.password;

import java.util.List;

import org.bouncycastle.crypto.generators.SCrypt;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/**
 * scrypt (Percival, 2009): memory-hard, so guessing in parallel on GPUs or ASICs costs memory as well as time.
 * N = 2^ln, block size r = 8, parallelism p = 1; with ln = 17 each guess needs 128 MiB.
 */
public final class Scrypt extends PasswordHash {

    private static final String PREFIX = "$scrypt$";

    public Scrypt() {
        super("scrypt", "scrypt", Security.RECOMMENDED);
    }

    @Override
    List<Param> costParams() {
        return List.of(Param.number("ln", 17));
    }

    @Override
    String hash(byte[] password, Values values) throws CryptoException {
        int ln = values.integer("ln", 10, 20);
        byte[] salt = Rand.bytes(16);
        byte[] h = SCrypt.generate(password, salt, 1 << ln, 8, 1, 32);
        return PREFIX + "ln=" + ln + ",r=8,p=1$" + b64(salt) + "$" + b64(h);
    }

    @Override
    boolean verify(byte[] password, String encoded) throws CryptoException {
        String[] p = parts(encoded, PREFIX, 5);
        int ln = field(p[2], "ln");
        int r = field(p[2], "r");
        int par = field(p[2], "p");
        if (ln < 1 || ln > 22 || r < 1 || r > 32 || par < 1 || par > 16) {
            throw new CryptoException("error.encoded.invalid");
        }
        byte[] expected = unb64(p[4]);
        return same(SCrypt.generate(password, unb64(p[3]), 1 << ln, r, par, expected.length), expected);
    }
}
