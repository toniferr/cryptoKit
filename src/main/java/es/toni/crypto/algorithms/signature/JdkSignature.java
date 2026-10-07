package es.toni.crypto.algorithms.signature;

import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import es.toni.crypto.algorithms.asymmetric.Keys;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Values;

/** Signatures the JDK implements itself: Ed25519, ECDSA over P-256 and RSA-PSS. */
public final class JdkSignature extends SignatureAlgorithm {

    private enum Kind { ED25519, ECDSA, RSA_PSS }

    private static final PSSParameterSpec PSS = new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1);

    private final Kind kind;

    private JdkSignature(String id, String name, String chapter, Kind kind) {
        super(id, name, chapter);
        this.kind = kind;
    }

    /** Edwards-curve signatures (Bernstein et al., 2011): fast, deterministic, no nonce to get wrong. */
    public static JdkSignature ed25519() {
        return new JdkSignature("ed25519", "Ed25519", "curves#eddsa", Kind.ED25519);
    }

    public static JdkSignature ecdsaP256() {
        return new JdkSignature("ecdsa-p256", "ECDSA P-256", "curves#ecdsa", Kind.ECDSA);
    }

    public static JdkSignature rsaPss() {
        return new JdkSignature("rsa-pss", "RSA-PSS", "publickey#signatures", Kind.RSA_PSS);
    }

    private String keyAlgorithm() {
        return switch (kind) {
            case ED25519 -> "Ed25519";
            case ECDSA -> "EC";
            case RSA_PSS -> "RSASSA-PSS";
        };
    }

    private Signature instance() throws GeneralSecurityException {
        return switch (kind) {
            case ED25519 -> Signature.getInstance("Ed25519");
            case ECDSA -> Signature.getInstance("SHA256withECDSA");
            case RSA_PSS -> {
                Signature s = Signature.getInstance("RSASSA-PSS");
                s.setParameter(PSS);
                yield s;
            }
        };
    }

    @Override
    public KeyPairPem generateKeyPair() throws CryptoException {
        try {
            KeyPairGenerator g = KeyPairGenerator.getInstance(keyAlgorithm());
            if (kind == Kind.ECDSA) {
                g.initialize(new ECGenParameterSpec("secp256r1"), Rand.secure());
            } else if (kind == Kind.RSA_PSS) {
                g.initialize(3072, Rand.secure());
            }
            return Keys.pem(g.generateKeyPair());
        } catch (GeneralSecurityException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }

    @Override
    protected byte[] sign(byte[] message, Values values) throws CryptoException {
        try {
            Signature s = instance();
            s.initSign(Keys.jdkPrivate(keyAlgorithm(), values, "private"), Rand.secure());
            s.update(message);
            return s.sign();
        } catch (GeneralSecurityException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }

    @Override
    protected boolean verify(byte[] message, byte[] signature, Values values) throws CryptoException {
        try {
            Signature s = instance();
            s.initVerify(Keys.jdkPublic(keyAlgorithm(), values, "public"));
            s.update(message);
            return s.verify(signature);
        } catch (SignatureException e) {
            return false;   // a malformed signature is simply not valid
        } catch (GeneralSecurityException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }
}
