package es.toni.crypto.algorithms.symmetric;

import java.security.GeneralSecurityException;
import java.util.List;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;

import es.toni.crypto.algorithms.password.Argon2id;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/**
 * Encrypt with a password, the modern way: Argon2id turns the password and a random salt into a 256-bit key, and
 * AES-256-GCM encrypts and authenticates. The output is self-contained:
 * {@code version (1 byte, 0x01) | salt (16) | nonce (12) | ciphertext and tag}, in Base64.
 */
public final class PasswordAead extends SymmetricCipher {

    static final int VERSION = 1;
    static final int SALT = 16;
    static final int NONCE = 12;
    // OWASP's minimum Argon2id configuration: 19 MiB, 2 passes, 1 lane.
    static final int MEMORY = 19_456;
    static final int PASSES = 2;

    public PasswordAead() {
        super("aes-gcm-password", "AES-GCM + Argon2id", Security.RECOMMENDED, "passwords#encrypt", Format.BASE64);
    }

    @Override
    public List<Param> params() {
        return List.of(Param.password("password"));
    }

    @Override
    public Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        byte[] password = values.bytes("password");
        if (password.length == 0) {
            throw new CryptoException("error.password.missing");
        }
        try {
            if (d == Direction.FORWARD) {
                byte[] salt = Rand.bytes(SALT);
                byte[] nonce = Rand.bytes(NONCE);
                byte[] key = Argon2id.derive(password, salt, MEMORY, PASSES, 1, 32);
                byte[] ct = AeadAlgorithm.seal(false, Cipher.ENCRYPT_MODE, key, nonce, new byte[0], input);
                return Result.of(concat(new byte[] {VERSION}, salt, nonce, ct)).note("note.password.aead");
            }
            if (input.length < 1 + SALT + NONCE + 16 || input[0] != VERSION) {
                throw new CryptoException("error.password.aead.format");
            }
            byte[] salt = slice(input, 1, 1 + SALT);
            byte[] nonce = slice(input, 1 + SALT, 1 + SALT + NONCE);
            byte[] key = Argon2id.derive(password, salt, MEMORY, PASSES, 1, 32);
            return Result.of(AeadAlgorithm.seal(false, Cipher.DECRYPT_MODE, key, nonce, new byte[0],
                slice(input, 1 + SALT + NONCE, input.length)));
        } catch (AEADBadTagException e) {
            throw new CryptoException("error.decrypt.password");
        } catch (GeneralSecurityException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }
}
