package es.toni.crypto.algorithms.asymmetric;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import es.toni.crypto.core.Codec;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Values;

/**
 * Elliptic-curve hybrid encryption (ECIES style): every message gets an ephemeral X25519 key pair; Diffie–Hellman with
 * the recipient's public key gives a shared secret, and HKDF-SHA-256 turns it into an AES-256-GCM key. Output:
 * {@code ephemeral public key (32) | nonce (12) | ciphertext and tag}.
 */
public final class X25519Aead extends PublicKeyCipher {

    /** DER prefix of an X.509 X25519 public key; the raw 32-byte key follows it. */
    private static final String X509_PREFIX = "302a300506032b656e032100";
    private static final String INFO = "cryptoKit X25519 AES-256-GCM";

    public X25519Aead() {
        super("x25519-aes-gcm", "X25519 + AES-GCM", "curves#ecdh");
    }

    @Override
    public KeyPairPem generateKeyPair() throws CryptoException {
        return Keys.pem(keyPair());
    }

    private static KeyPair keyPair() throws CryptoException {
        try {
            return KeyPairGenerator.getInstance("X25519").generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }

    private static byte[] raw(PublicKey k) {
        byte[] enc = k.getEncoded();
        return Arrays.copyOfRange(enc, enc.length - 32, enc.length);
    }

    private static PublicKey fromRaw(byte[] raw) throws GeneralSecurityException, CryptoException {
        byte[] der = Codec.unhex(X509_PREFIX + Codec.hex(raw));
        return KeyFactory.getInstance("X25519").generatePublic(new X509EncodedKeySpec(der));
    }

    private static byte[] agree(PrivateKey priv, PublicKey pub) throws GeneralSecurityException {
        KeyAgreement ka = KeyAgreement.getInstance("X25519");
        ka.init(priv);
        ka.doPhase(pub, true);
        return ka.generateSecret();
    }

    /** The public key of a private key: its scalar times the base point u = 9. */
    private static PublicKey publicOf(PrivateKey priv) throws GeneralSecurityException, CryptoException {
        byte[] nine = new byte[32];
        nine[0] = 9;
        return fromRaw(agree(priv, fromRaw(nine)));
    }

    @Override
    public Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        try {
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            if (d == Direction.FORWARD) {
                PublicKey recipient = Keys.jdkPublic("X25519", values, "public");
                KeyPair eph = keyPair();
                byte[] ephRaw = raw(eph.getPublic());
                byte[] key = hkdf(agree(eph.getPrivate(), recipient), Keys.concat(ephRaw, raw(recipient)), INFO);
                byte[] nonce = Rand.bytes(12);
                c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
                return Result.of(Keys.concat(ephRaw, nonce, c.doFinal(input))).note("note.ecies");
            }
            if (input.length < 32 + 12 + 16) {
                throw new CryptoException("error.hybrid.format");
            }
            PrivateKey priv = Keys.jdkPrivate("X25519", values, "private");
            byte[] ephRaw = Arrays.copyOfRange(input, 0, 32);
            byte[] key = hkdf(agree(priv, fromRaw(ephRaw)), Keys.concat(ephRaw, raw(publicOf(priv))), INFO);
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                new GCMParameterSpec(128, Arrays.copyOfRange(input, 32, 44)));
            return Result.of(c.doFinal(Arrays.copyOfRange(input, 44, input.length)));
        } catch (AEADBadTagException e) {
            throw new CryptoException("error.decrypt.key");
        } catch (GeneralSecurityException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }
}
