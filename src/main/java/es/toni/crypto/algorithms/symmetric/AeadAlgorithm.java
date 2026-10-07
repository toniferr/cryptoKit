package es.toni.crypto.algorithms.symmetric;

import java.security.GeneralSecurityException;
import java.util.List;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import es.toni.crypto.core.Codec;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/**
 * Authenticated encryption with associated data: AES-GCM and ChaCha20-Poly1305. The 16-byte tag at the end of the
 * ciphertext makes any change to it (or to the associated data) fail decryption instead of yielding garbage.
 * <p>
 * The nonce must never repeat under the same key. Leave it empty and a random 12-byte one is generated and prepended.
 */
public final class AeadAlgorithm extends SymmetricCipher {

    private final boolean chacha;

    private AeadAlgorithm(String id, String name, String chapter, boolean chacha) {
        super(id, name, Security.RECOMMENDED, chapter, Format.HEX);
        this.chacha = chacha;
    }

    public static AeadAlgorithm aesGcm() {
        return new AeadAlgorithm("aes-gcm", "AES-GCM", "modes#gcm", false);
    }

    public static AeadAlgorithm chacha20Poly1305() {
        return new AeadAlgorithm("chacha20-poly1305", "ChaCha20-Poly1305", "modes#chacha", true);
    }

    @Override
    public List<Param> params() {
        return List.of(
            Param.bytes("key", Format.TEXT, 32),
            Param.bytes("nonce", Format.TEXT, 12).asOptional(),
            Param.bytes("aad", Format.TEXT, 0).asOptional());
    }

    /** Encrypts or decrypts with an explicit nonce; the ciphertext carries the tag at its end. */
    static byte[] seal(boolean chacha, int mode, byte[] key, byte[] nonce, byte[] aad, byte[] data)
        throws GeneralSecurityException {
        Cipher c = Cipher.getInstance(chacha ? "ChaCha20-Poly1305" : "AES/GCM/NoPadding");
        SecretKeySpec k = new SecretKeySpec(key, chacha ? "ChaCha20" : "AES");
        c.init(mode, k, chacha ? new IvParameterSpec(nonce) : new GCMParameterSpec(128, nonce));
        if (aad.length > 0) {
            c.updateAAD(aad);
        }
        return c.doFinal(data);
    }

    @Override
    public Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        byte[] key = values.bytes("key");
        checkKey(key, chacha ? new int[] {32} : new int[] {16, 24, 32});
        byte[] aad = values.bytes("aad");
        boolean embedded = values.blank("nonce");
        byte[] nonce;
        byte[] data = input;
        if (!embedded) {
            nonce = values.bytes("nonce");
            // GCM accepts any nonce length (12 is standard; version 1.0.0 used 16), ChaCha20 exactly 12.
            if (chacha ? nonce.length != 12 : nonce.length < 8) {
                throw new CryptoException("error.nonce.length", nonce.length, chacha ? "12" : "≥ 8 (12)");
            }
        } else if (d == Direction.FORWARD) {
            nonce = Rand.bytes(12);
        } else {
            if (input.length < 12 + 16) {
                throw new CryptoException("error.iv.embedded", 12);
            }
            nonce = slice(input, 0, 12);
            data = slice(input, 12, input.length);
        }
        try {
            if (d == Direction.FORWARD) {
                byte[] out = seal(chacha, Cipher.ENCRYPT_MODE, key, nonce, aad, data);
                Result r = Result.of(embedded ? concat(nonce, out) : out);
                if (embedded) {
                    r.note("note.nonce.prepended", Codec.hex(nonce));
                } else {
                    r.note("note.nonce.reuse");
                }
                return r.note("note.aead.tag");
            }
            return Result.of(seal(chacha, Cipher.DECRYPT_MODE, key, nonce, aad, data)).note("note.aead.ok");
        } catch (AEADBadTagException e) {
            throw new CryptoException("error.decrypt.tag");
        } catch (GeneralSecurityException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }
}
