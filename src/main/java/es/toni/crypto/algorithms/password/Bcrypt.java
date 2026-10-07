package es.toni.crypto.algorithms.password;

import java.util.List;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/**
 * bcrypt (Provos and Mazières, 1999): Blowfish's expensive key schedule, repeated 2^cost times. Passwords longer than
 * 72 bytes are rejected rather than silently truncated.
 */
public final class Bcrypt extends PasswordHash {

    public Bcrypt() {
        super("bcrypt", "bcrypt", Security.RECOMMENDED);
    }

    @Override
    List<Param> costParams() {
        return List.of(Param.number("cost", 12));
    }

    @Override
    String hash(byte[] password, Values values) throws CryptoException {
        int cost = values.integer("cost", 4, 20);
        if (password.length > 72) {
            throw new CryptoException("error.bcrypt.long");
        }
        return OpenBSDBCrypt.generate("2b", password, Rand.bytes(16), cost);
    }

    @Override
    boolean verify(byte[] password, String encoded) throws CryptoException {
        if (!encoded.matches("\\$2[abxy]?\\$\\d\\d\\$[./A-Za-z0-9]{53}")) {
            throw new CryptoException(encoded.startsWith("$2") ? "error.encoded.invalid" : "error.encoded.otherAlgorithm");
        }
        if (password.length > 72) {
            return false;
        }
        try {
            return OpenBSDBCrypt.checkPassword(encoded, password);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("error.encoded.invalid");
        }
    }
}
