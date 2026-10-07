package es.toni.crypto.algorithms.hash;

import static es.toni.crypto.TestSupport.forward;
import static es.toni.crypto.TestSupport.run;
import static es.toni.crypto.TestSupport.v;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Format;

class HashTest {

    /** The published digests of "abc". */
    @ParameterizedTest
    @CsvSource({
        "md5, 900150983cd24fb0d6963f7d28e17f72",
        "sha1, a9993e364706816aba3e25717850c26c9cd0d89d",
        "sha256, ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
        "sha512, ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f",
        "sha3-256, 3a985da74fe225b2045c172d6bd390bd855f086e3e9d525b46bfe24511431532",
        "sha3-512, b751850b1a57168a5693cd924b6b096e08f621827444f70d884f5d0240d2712e10e116e9192af3c91a7ec57647e3934057340b4cf408d5a56592f8274eec53f0",
        "blake2b, ba80a53f981c4d0d6a2797b69f12f6e94c212f14685ac4b74b12bb6fdbffa2d17d87c5392aab792dc252d5de4533cc9518d38aa8dbf1925ab92386edd4009923",
        "blake3, 6437b3ac38465133ffb63b75273a8db548c558465d79db03fd359c6cd5bd9d85",
        "ripemd160, 8eb208f7e05d987a9b044a8e98c6b087f15a0bfc",
    })
    void abc(String id, String digest) throws CryptoException {
        assertEquals(digest, forward(id, "abc", v()));
    }

    /** Digests produced by cryptoKit 1.0.0 for the same text (UTF-8 with accents). */
    @ParameterizedTest
    @CsvSource({
        "md5, f2e9417f95d0bcb332cf09b00fd0b7fe",
        "sha1, 21a5cf5a0a0e8b22cffda7e307bad53988104b9a",
        "sha256, 3d8714bad46da219c1d9fe52829dc90d7257f5739aa57f2f3f761561739d8fe8",
        "sha512, 367dc8470628f58497811b66c1ea1430b82930ab8947a60a899589c228f25c3fa4a86d4959dc58cd138ad804b0dc8c05cf30075313a32d4468b8a3ddeeb585bf",
        "sha3-256, 9346314e1145fd5dc6c2255ae0f518095be2f65564b07e42b38161a36e66b623",
        "sha3-512, e09eae333873669f65159449402a153d45e69765fa904ed74c11c27590288efb726e80b01639ff2b950e21163f9d183c4c3ce4f2d3f13ad8ae516c95051e7344",
    })
    void sameAsVersion1(String id, String digest) throws CryptoException {
        assertEquals(digest, forward(id, "Hola, cryptoKit! ¿Qué tal?", v()));
    }

    @Test
    void emptyMessageHasADigest() throws CryptoException {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", forward("sha256", "", v()));
    }

    @Test
    void verify() throws CryptoException {
        String d = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
        assertTrue(run("sha256", Direction.BACKWARD, "abc", v().set("expected", d, Format.HEX)).verified());
        assertTrue(run("sha256", Direction.BACKWARD, "abc", v().set("expected", d.toUpperCase(), Format.HEX)).verified());
        assertFalse(run("sha256", Direction.BACKWARD, "abd", v().set("expected", d, Format.HEX)).verified());
        assertThrows(CryptoException.class, () -> run("sha256", Direction.BACKWARD, "abc", v()));
    }

    /** RFC 4231, test case 2. */
    @Test
    void hmacRfc4231() throws CryptoException {
        String msg = "what do ya want for nothing?";
        assertEquals("5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843",
            forward("hmac-sha256", msg, v().set("key", "Jefe")));
        assertEquals("164b7a7bfcf819e2e395fbe73b56e0a387bd64222e831fd610270cd7ea2505549758bf75c05a994a6d034f65f8f0e6fdcaeab1a34d4a6b4b636e070a38bce737",
            forward("hmac-sha512", msg, v().set("key", "4a656665", Format.HEX)));
        assertTrue(run("hmac-sha256", Direction.BACKWARD, msg, v().set("key", "Jefe")
            .set("expected", "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843", Format.HEX)).verified());
        assertFalse(run("hmac-sha256", Direction.BACKWARD, msg, v().set("key", "Jeff")
            .set("expected", "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843", Format.HEX)).verified());
        assertThrows(CryptoException.class, () -> forward("hmac-sha256", msg, v()));
    }
}
