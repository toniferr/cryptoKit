package es.toni.crypto.algorithms.symmetric;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/**
 * PBEWithHMACSHA512AndAES_256 as Jasypt's {@code StandardPBEStringEncryptor} does it, which is what version 1.0.0
 * used: a 16-byte salt and a 16-byte IV in front of the ciphertext, 1,000 iterations, Base64. Implemented with the
 * JDK alone, so Jasypt (unmaintained since 2019) is no longer needed; the tests check both directions against it.
 */
public final class JasyptPbe extends SymmetricCipher {

    static final String ALGORITHM = "PBEWithHMACSHA512AndAES_256";
    static final int ITERATIONS = 1000;
    static final int SALT = 16;
    static final int IV = 16;

    public JasyptPbe() {
        super("pbe-jasypt", "PBE AES-256 (Jasypt)", Security.LEGACY, "passwords#pbe", Format.BASE64);
    }

    @Override
    public List<Param> params() {
        return List.of(Param.password("password"));
    }

    @Override
    public Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        String password = values.text("password");
        if (password.isEmpty()) {
            throw new CryptoException("error.password.missing");
        }
        if (!StandardCharsets.US_ASCII.newEncoder().canEncode(password)) {
            throw new CryptoException("error.pbe.ascii");
        }
        try {
            SecretKey key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(new PBEKeySpec(password.toCharArray()));
            Cipher c = Cipher.getInstance(ALGORITHM);
            if (d == Direction.FORWARD) {
                byte[] salt = Rand.bytes(SALT);
                byte[] iv = Rand.bytes(IV);
                c.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, ITERATIONS, new IvParameterSpec(iv)));
                return Result.of(concat(salt, iv, c.doFinal(input))).note("note.pbe.legacy");
            }
            if (input.length < SALT + IV + 16) {
                throw new CryptoException("error.pbe.format");
            }
            c.init(Cipher.DECRYPT_MODE, key,
                new PBEParameterSpec(slice(input, 0, SALT), ITERATIONS, new IvParameterSpec(slice(input, SALT, SALT + IV))));
            return Result.of(c.doFinal(slice(input, SALT + IV, input.length)));
        } catch (BadPaddingException e) {
            throw new CryptoException("error.decrypt.password");
        } catch (GeneralSecurityException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }
}
