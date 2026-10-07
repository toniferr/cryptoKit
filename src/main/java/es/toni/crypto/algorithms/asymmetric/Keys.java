package es.toni.crypto.algorithms.asymmetric;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;

import es.toni.crypto.core.Algorithm.KeyPairPem;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Messages;
import es.toni.crypto.core.Pem;
import es.toni.crypto.core.Values;

/** Reading and writing PEM keys, for the JDK's algorithms and for Bouncy Castle's post-quantum ones. */
public final class Keys {

    private Keys() {
    }

    public static KeyPairPem pem(KeyPair kp) {
        return new KeyPairPem(Pem.encode(Pem.PUBLIC, kp.getPublic().getEncoded()),
            Pem.encode(Pem.PRIVATE, kp.getPrivate().getEncoded()));
    }

    public static PublicKey jdkPublic(String algorithm, Values v, String param) throws CryptoException {
        byte[] der = Pem.decode(Pem.PUBLIC, v.text(param), param);
        try {
            return KeyFactory.getInstance(algorithm).generatePublic(new X509EncodedKeySpec(der));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new CryptoException("error.key.wrongType", Messages.get("param." + param), algorithm);
        }
    }

    public static PrivateKey jdkPrivate(String algorithm, Values v, String param) throws CryptoException {
        byte[] der = Pem.decode(Pem.PRIVATE, v.text(param), param);
        try {
            return KeyFactory.getInstance(algorithm).generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new CryptoException("error.key.wrongType", Messages.get("param." + param), algorithm);
        }
    }

    public static KeyPairPem pem(AsymmetricKeyParameter pub, AsymmetricKeyParameter priv) throws CryptoException {
        try {
            return new KeyPairPem(
                Pem.encode(Pem.PUBLIC, SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(pub).getEncoded()),
                Pem.encode(Pem.PRIVATE, PrivateKeyInfoFactory.createPrivateKeyInfo(priv).getEncoded()));
        } catch (IOException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }

    /** A Bouncy Castle public key of the expected class (e.g. MLKEMPublicKeyParameters). */
    public static <T> T bcPublic(Class<T> type, String name, Values v, String param) throws CryptoException {
        byte[] der = Pem.decode(Pem.PUBLIC, v.text(param), param);
        try {
            AsymmetricKeyParameter k = PublicKeyFactory.createKey(der);
            if (type.isInstance(k)) {
                return type.cast(k);
            }
        } catch (IOException | RuntimeException e) {
            // reported below
        }
        throw new CryptoException("error.key.wrongType", Messages.get("param." + param), name);
    }

    public static <T> T bcPrivate(Class<T> type, String name, Values v, String param) throws CryptoException {
        byte[] der = Pem.decode(Pem.PRIVATE, v.text(param), param);
        try {
            AsymmetricKeyParameter k = PrivateKeyFactory.createKey(der);
            if (type.isInstance(k)) {
                return type.cast(k);
            }
        } catch (IOException | RuntimeException e) {
            // reported below
        }
        throw new CryptoException("error.key.wrongType", Messages.get("param." + param), name);
    }

    public static byte[] concat(byte[]... parts) {
        int n = 0;
        for (byte[] p : parts) {
            n += p.length;
        }
        byte[] out = new byte[n];
        int off = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, off, p.length);
            off += p.length;
        }
        return out;
    }
}
