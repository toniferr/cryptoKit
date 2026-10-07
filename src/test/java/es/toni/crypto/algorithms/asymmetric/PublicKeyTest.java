package es.toni.crypto.algorithms.asymmetric;

import static es.toni.crypto.TestSupport.alg;
import static es.toni.crypto.TestSupport.backward;
import static es.toni.crypto.TestSupport.forward;
import static es.toni.crypto.TestSupport.run;
import static es.toni.crypto.TestSupport.v;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.toni.crypto.core.Algorithm.KeyPairPem;
import es.toni.crypto.core.Codec;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Values;

class PublicKeyTest {

    static final String MESSAGE = "Meet me at the usual place. ¿Vale?";

    @ParameterizedTest
    @ValueSource(strings = {"rsa-oaep", "x25519-aes-gcm", "ml-kem-768"})
    void encryptWithPublicDecryptWithPrivate(String id) throws CryptoException {
        KeyPairPem kp = alg(id).generateKeyPair();
        assertTrue(kp.publicPem().startsWith("-----BEGIN PUBLIC KEY-----"));
        assertTrue(kp.privatePem().startsWith("-----BEGIN PRIVATE KEY-----"));
        Values keys = v().set("public", kp.publicPem()).set("private", kp.privatePem());
        String a = forward(id, MESSAGE, keys);
        String b = forward(id, MESSAGE, keys);
        assertNotEquals(a, b, "public-key encryption is randomised");
        assertEquals(MESSAGE, backward(id, a, keys));

        KeyPairPem other = alg(id).generateKeyPair();
        assertThrows(CryptoException.class, () -> backward(id, a, v().set("private", other.privatePem())));
    }

    @Test
    void rsaRefusesLongMessages() throws CryptoException {
        KeyPairPem kp = alg("rsa-oaep").generateKeyPair();
        CryptoException e = assertThrows(CryptoException.class,
            () -> forward("rsa-oaep", "x".repeat(400), v().set("public", kp.publicPem())));
        assertEquals("error.rsa.long", e.key());
    }

    @Test
    void keysOfAnotherAlgorithmAreRejected() throws CryptoException {
        KeyPairPem rsa = alg("rsa-oaep").generateKeyPair();
        assertThrows(CryptoException.class, () -> forward("x25519-aes-gcm", MESSAGE, v().set("public", rsa.publicPem())));
        assertThrows(CryptoException.class, () -> forward("ml-kem-768", MESSAGE, v().set("public", rsa.publicPem())));
        assertThrows(CryptoException.class, () -> forward("rsa-oaep", MESSAGE, v().set("public", "not a key")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ed25519", "ecdsa-p256", "rsa-pss", "ml-dsa-65"})
    void signAndVerify(String id) throws CryptoException {
        KeyPairPem kp = alg(id).generateKeyPair();
        String sig = forward(id, MESSAGE, v().set("private", kp.privatePem()));
        Values check = v().set("public", kp.publicPem()).set("signature", sig, Format.BASE64);
        assertTrue(run(id, Direction.BACKWARD, MESSAGE, check).verified());
        assertFalse(run(id, Direction.BACKWARD, MESSAGE + ".", check).verified());
        KeyPairPem other = alg(id).generateKeyPair();
        assertFalse(run(id, Direction.BACKWARD, MESSAGE,
            v().set("public", other.publicPem()).set("signature", sig, Format.BASE64)).verified());
    }

    /** RFC 8032, section 7.1, test 1: the empty message. */
    @Test
    void ed25519Rfc8032() throws CryptoException {
        String seed = "9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60";
        String pub = "d75a980182b10ab7d54bfed3c964073a0ee172f3daa62325af021a68f707511a";
        String priv = "-----BEGIN PRIVATE KEY-----\n" + Codec.base64(Codec.unhex("302e020100300506032b657004220420" + seed))
            + "\n-----END PRIVATE KEY-----\n";
        String pubPem = Codec.base64(Codec.unhex("302a300506032b6570032100" + pub));
        String expected = "e5564300c360ac729086e2cc806e828a84877f1eb8e5d974d873e065224901555fb8821590a33bacc61e39701cf9b46bd25bf5f0595bbe24655141438e7a100b";
        assertEquals(expected, run("ed25519", Direction.FORWARD, "", Format.TEXT, Format.HEX, v().set("private", priv)).text());
        assertTrue(run("ed25519", Direction.BACKWARD, "", v().set("public", pubPem).set("signature", expected, Format.HEX))
            .verified());
    }
}
