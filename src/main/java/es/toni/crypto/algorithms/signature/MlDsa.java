package es.toni.crypto.algorithms.signature;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.MLDSAKeyPairGenerator;
import org.bouncycastle.crypto.params.MLDSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.MLDSAParameters;
import org.bouncycastle.crypto.params.MLDSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.MLDSAPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.MLDSASigner;

import es.toni.crypto.algorithms.asymmetric.Keys;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Values;

/** ML-DSA-65 (FIPS 204, 2024, formerly CRYSTALS-Dilithium): a lattice signature meant to survive quantum computers. */
public final class MlDsa extends SignatureAlgorithm {

    private static final MLDSAParameters PARAMS = MLDSAParameters.ml_dsa_65;

    public MlDsa() {
        super("ml-dsa-65", "ML-DSA-65", "postquantum#mldsa");
    }

    @Override
    public KeyPairPem generateKeyPair() throws CryptoException {
        MLDSAKeyPairGenerator g = new MLDSAKeyPairGenerator();
        g.init(new MLDSAKeyGenerationParameters(Rand.secure(), PARAMS));
        AsymmetricCipherKeyPair kp = g.generateKeyPair();
        return Keys.pem(kp.getPublic(), kp.getPrivate());
    }

    @Override
    protected byte[] sign(byte[] message, Values values) throws CryptoException {
        MLDSAPrivateKeyParameters key = Keys.bcPrivate(MLDSAPrivateKeyParameters.class, "ML-DSA-65", values, "private");
        MLDSASigner s = new MLDSASigner();
        s.init(true, new ParametersWithRandom(key, Rand.secure()));
        s.update(message, 0, message.length);
        try {
            return s.generateSignature();
        } catch (org.bouncycastle.crypto.CryptoException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }

    @Override
    protected boolean verify(byte[] message, byte[] signature, Values values) throws CryptoException {
        MLDSAPublicKeyParameters key = Keys.bcPublic(MLDSAPublicKeyParameters.class, "ML-DSA-65", values, "public");
        MLDSASigner s = new MLDSASigner();
        s.init(false, key);
        s.update(message, 0, message.length);
        return s.verifySignature(signature);
    }
}
