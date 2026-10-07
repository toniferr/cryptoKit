package es.toni.crypto.algorithms.asymmetric;

import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.SecretWithEncapsulation;
import org.bouncycastle.crypto.generators.MLKEMKeyPairGenerator;
import org.bouncycastle.crypto.kems.MLKEMExtractor;
import org.bouncycastle.crypto.kems.MLKEMGenerator;
import org.bouncycastle.crypto.params.MLKEMKeyGenerationParameters;
import org.bouncycastle.crypto.params.MLKEMParameters;
import org.bouncycastle.crypto.params.MLKEMPrivateKeyParameters;
import org.bouncycastle.crypto.params.MLKEMPublicKeyParameters;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Values;

/**
 * Post-quantum hybrid encryption: ML-KEM-768 (FIPS 203, 2024, formerly CRYSTALS-Kyber) encapsulates a fresh 32-byte
 * secret to the recipient's public key; HKDF and AES-256-GCM encrypt the message with it. Output:
 * {@code encapsulation (1088) | nonce (12) | ciphertext and tag}.
 */
public final class MlKemAead extends PublicKeyCipher {

    private static final MLKEMParameters PARAMS = MLKEMParameters.ml_kem_768;
    private static final String INFO = "cryptoKit ML-KEM-768 AES-256-GCM";
    private static final int ENCAPSULATION = 1088;

    public MlKemAead() {
        super("ml-kem-768", "ML-KEM-768 + AES-GCM", "postquantum#mlkem");
    }

    @Override
    public KeyPairPem generateKeyPair() throws CryptoException {
        MLKEMKeyPairGenerator g = new MLKEMKeyPairGenerator();
        g.init(new MLKEMKeyGenerationParameters(Rand.secure(), PARAMS));
        AsymmetricCipherKeyPair kp = g.generateKeyPair();
        return Keys.pem(kp.getPublic(), kp.getPrivate());
    }

    @Override
    public Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        try {
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            if (d == Direction.FORWARD) {
                MLKEMPublicKeyParameters pub = Keys.bcPublic(MLKEMPublicKeyParameters.class, "ML-KEM-768", values, "public");
                if (!PARAMS.getName().equals(pub.getParameters().getName())) {
                    throw new CryptoException("error.key.wrongType", es.toni.crypto.core.Messages.get("param.public"), "ML-KEM-768");
                }
                SecretWithEncapsulation s = new MLKEMGenerator(Rand.secure()).generateEncapsulated(pub);
                byte[] enc = s.getEncapsulation();
                byte[] key = hkdf(s.getSecret(), enc, INFO);
                byte[] nonce = Rand.bytes(12);
                c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
                return Result.of(Keys.concat(enc, nonce, c.doFinal(input))).note("note.mlkem", enc.length);
            }
            if (input.length < ENCAPSULATION + 12 + 16) {
                throw new CryptoException("error.hybrid.format");
            }
            MLKEMPrivateKeyParameters priv = Keys.bcPrivate(MLKEMPrivateKeyParameters.class, "ML-KEM-768", values, "private");
            byte[] enc = Arrays.copyOfRange(input, 0, ENCAPSULATION);
            byte[] key = hkdf(new MLKEMExtractor(priv).extractSecret(enc), enc, INFO);
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                new GCMParameterSpec(128, Arrays.copyOfRange(input, ENCAPSULATION, ENCAPSULATION + 12)));
            return Result.of(c.doFinal(Arrays.copyOfRange(input, ENCAPSULATION + 12, input.length)));
        } catch (AEADBadTagException e) {
            throw new CryptoException("error.decrypt.key");
        } catch (GeneralSecurityException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }
}
