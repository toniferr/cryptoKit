package es.toni.crypto.algorithms.asymmetric;

import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.spec.MGF1ParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Values;

/**
 * RSA (Rivest, Shamir and Adleman, 1977) with OAEP padding, SHA-256 and MGF1-SHA-256, and 3072-bit keys (about
 * 128-bit security). RSA encrypts at most a few hundred bytes: real systems encrypt a symmetric key with it.
 */
public final class RsaOaep extends PublicKeyCipher {

    private static final OAEPParameterSpec OAEP =
        new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    public RsaOaep() {
        super("rsa-oaep", "RSA-OAEP", "publickey#rsa");
    }

    @Override
    public KeyPairPem generateKeyPair() throws CryptoException {
        try {
            KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
            g.initialize(3072, Rand.secure());
            return Keys.pem(g.generateKeyPair());
        } catch (GeneralSecurityException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }

    @Override
    public Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        try {
            Cipher c = Cipher.getInstance("RSA/ECB/OAEPPadding");
            if (d == Direction.FORWARD) {
                PublicKey key = Keys.jdkPublic("RSA", values, "public");
                int max = ((RSAKey) key).getModulus().bitLength() / 8 - 2 * 32 - 2;
                if (input.length > max) {
                    throw new CryptoException("error.rsa.long", input.length, max);
                }
                c.init(Cipher.ENCRYPT_MODE, key, OAEP, Rand.secure());
                return Result.of(c.doFinal(input)).note("note.rsa.random");
            }
            c.init(Cipher.DECRYPT_MODE, Keys.jdkPrivate("RSA", values, "private"), OAEP);
            return Result.of(c.doFinal(input));
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new CryptoException("error.decrypt.key");
        } catch (GeneralSecurityException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }
}
