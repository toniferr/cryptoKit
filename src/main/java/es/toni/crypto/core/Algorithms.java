package es.toni.crypto.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.digests.Blake3Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;

import es.toni.crypto.algorithms.asymmetric.MlKemAead;
import es.toni.crypto.algorithms.asymmetric.RsaOaep;
import es.toni.crypto.algorithms.asymmetric.X25519Aead;
import es.toni.crypto.algorithms.classical.Caesar;
import es.toni.crypto.algorithms.classical.Enigma;
import es.toni.crypto.algorithms.classical.Playfair;
import es.toni.crypto.algorithms.classical.Vigenere;
import es.toni.crypto.algorithms.encoding.Encoding;
import es.toni.crypto.algorithms.hash.DigestAlgorithm;
import es.toni.crypto.algorithms.hash.HmacAlgorithm;
import es.toni.crypto.algorithms.password.Argon2id;
import es.toni.crypto.algorithms.password.Bcrypt;
import es.toni.crypto.algorithms.password.Pbkdf2;
import es.toni.crypto.algorithms.password.Scrypt;
import es.toni.crypto.algorithms.signature.JdkSignature;
import es.toni.crypto.algorithms.signature.MlDsa;
import es.toni.crypto.algorithms.symmetric.AeadAlgorithm;
import es.toni.crypto.algorithms.symmetric.BlockCipherAlgorithm;
import es.toni.crypto.algorithms.symmetric.BlockCipherAlgorithm.Mode;
import es.toni.crypto.algorithms.symmetric.JasyptPbe;
import es.toni.crypto.algorithms.symmetric.PasswordAead;

/** Every algorithm in the kit, in the order the app lists them (family by family, best choice first). */
public final class Algorithms {

    private static final Map<String, Algorithm> BY_ID = new LinkedHashMap<>();

    static {
        add(new Caesar());
        add(new Vigenere());
        add(new Playfair());
        add(new Enigma());

        for (Encoding.Kind k : Encoding.Kind.values()) {
            add(new Encoding(k));
        }

        add(new DigestAlgorithm("sha256", "SHA-256", Security.RECOMMENDED, SHA256Digest::new));
        add(new DigestAlgorithm("sha512", "SHA-512", Security.RECOMMENDED, SHA512Digest::new));
        add(new DigestAlgorithm("sha3-256", "SHA3-256", Security.RECOMMENDED, () -> new SHA3Digest(256)));
        add(new DigestAlgorithm("sha3-512", "SHA3-512", Security.RECOMMENDED, () -> new SHA3Digest(512)));
        add(new DigestAlgorithm("blake2b", "BLAKE2b-512", Security.RECOMMENDED, () -> new Blake2bDigest(512)));
        add(new DigestAlgorithm("blake3", "BLAKE3", Security.RECOMMENDED, () -> new Blake3Digest(256)));
        add(new DigestAlgorithm("ripemd160", "RIPEMD-160", Security.LEGACY, RIPEMD160Digest::new));
        add(new DigestAlgorithm("sha1", "SHA-1", Security.BROKEN, SHA1Digest::new));
        add(new DigestAlgorithm("md5", "MD5", Security.BROKEN, MD5Digest::new));

        add(new HmacAlgorithm("hmac-sha256", "HMAC-SHA256", SHA256Digest::new));
        add(new HmacAlgorithm("hmac-sha512", "HMAC-SHA512", SHA512Digest::new));
        add(new HmacAlgorithm("hmac-sha3-256", "HMAC-SHA3-256", () -> new SHA3Digest(256)));

        add(new Argon2id());
        add(new Scrypt());
        add(new Bcrypt());
        add(new Pbkdf2());

        add(AeadAlgorithm.aesGcm());
        add(AeadAlgorithm.chacha20Poly1305());
        add(new PasswordAead());
        add(BlockCipherAlgorithm.aes(Mode.CTR, Security.LEGACY));
        add(BlockCipherAlgorithm.aes(Mode.CBC, Security.LEGACY));
        add(BlockCipherAlgorithm.aes(Mode.ECB, Security.BROKEN));
        add(BlockCipherAlgorithm.tripleDes());
        add(BlockCipherAlgorithm.blowfish(Mode.CBC));
        add(BlockCipherAlgorithm.blowfish(Mode.ECB));
        add(BlockCipherAlgorithm.des(Mode.CBC));
        add(BlockCipherAlgorithm.des(Mode.ECB));
        add(new JasyptPbe());

        add(new MlKemAead());
        add(new X25519Aead());
        add(new RsaOaep());

        add(new MlDsa());
        add(JdkSignature.ed25519());
        add(JdkSignature.ecdsaP256());
        add(JdkSignature.rsaPss());
    }

    private Algorithms() {
    }

    private static void add(Algorithm a) {
        if (BY_ID.put(a.id(), a) != null) {
            throw new IllegalStateException("duplicate algorithm id " + a.id());
        }
    }

    public static List<Algorithm> all() {
        return Collections.unmodifiableList(new ArrayList<>(BY_ID.values()));
    }

    public static List<Algorithm> of(Family f) {
        return BY_ID.values().stream().filter(a -> a.family() == f).toList();
    }

    public static Optional<Algorithm> byId(String id) {
        return Optional.ofNullable(BY_ID.get(id.toLowerCase()));
    }
}
