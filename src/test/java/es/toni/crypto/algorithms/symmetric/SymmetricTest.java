package es.toni.crypto.algorithms.symmetric;

import static es.toni.crypto.TestSupport.backward;
import static es.toni.crypto.TestSupport.forward;
import static es.toni.crypto.TestSupport.run;
import static es.toni.crypto.TestSupport.v;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import es.toni.crypto.core.Codec;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Values;

class SymmetricTest {

    static final String MESSAGE = "Hola, cryptoKit! ¿Qué tal?";

    /**
     * Ciphertexts produced by the real cryptoKit 1.0.0 jar (keys and IVs typed as text). Version 2 must decrypt them,
     * and encrypting with the same key and IV must give the same bytes.
     */
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "des-cbc|12345678|abcdefgh|722aaf7eecf0a045f90772860fd7548bf83543cabf482f5d4291e99496e8bb03",
        "des-ecb|12345678||a4f10acc723bea2b6fec9e8bdfd6f2b282848c35895ceab7b2b1f98d7b5ca989",
        "aes-cbc|0123456789abcdef|fedcba9876543210|76fac088a2739174344ebc174fe369afb3b3cabe31ab1aadc5a43db7b641e907",
        "aes-cbc|0123456789abcdef0123456789abcdef|fedcba9876543210|b7f05143c74384883da287eb4e573a66ee65a2e3cc913ab225bb38eeda731b2b",
        "aes-ecb|0123456789abcdef||306edd3c2c7fd300df175b6b02517561b91a3a7947d4298b8ab248e374eb63e1",
        "aes-gcm|0123456789abcdef|fedcba9876543210|f4784e3395cfed3d4d4e0c207ecddf3c71be9f1b4351ffa6483b76268faf0d63bc5a784b552ee52919e59cb3",
        "blowfish-cbc|secretkey|12345678|8e8779fd6aeebdd47d49f1bec12ceeabf2a7e709d234ea6c3952238d20ea3e49",
        "blowfish-ecb|secretkey||a2aaf942e36cdd9a74c0dc2439422a1b49670f0e75a00709594c8483357e64e3",
    })
    void compatibleWithVersion1(String id, String key, String iv, String cipherHex) throws CryptoException {
        Values vals = v().set("key", key, Format.TEXT);
        if (iv != null) {
            vals.set(id.equals("aes-gcm") ? "nonce" : "iv", iv, Format.TEXT);
        }
        assertEquals(MESSAGE, backward(id, cipherHex, vals));
        assertEquals(cipherHex, forward(id, MESSAGE, vals));
    }

    @Test
    void jasyptCiphertextFromVersion1() throws CryptoException {
        String v1 = "OPG4gONd6UYxN1HMZ1utRIlB3ycKqLES/EIxiObxCTiGhLYPK1a7bipKMdSQ57pVUvXb2eZrEDFmE7qDHgXiGQ==";
        assertEquals(MESSAGE, backward("pbe-jasypt", v1, v().set("password", "secreto123")));
    }

    /** Our JDK-only implementation and the real Jasypt understand each other in both directions. */
    @Test
    void jasyptBothWays() throws CryptoException {
        StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
        jasypt.setPassword("secreto123");
        jasypt.setAlgorithm("PBEWithHMACSHA512AndAES_256");
        jasypt.setIvGenerator(new RandomIvGenerator());
        String ours = forward("pbe-jasypt", MESSAGE, v().set("password", "secreto123"));
        assertEquals(MESSAGE, jasypt.decrypt(ours));
        String theirs = jasypt.encrypt(MESSAGE);
        assertEquals(MESSAGE, backward("pbe-jasypt", theirs, v().set("password", "secreto123")));
        assertThrows(CryptoException.class, () -> forward("pbe-jasypt", MESSAGE, v().set("password", "contraseña")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"aes-gcm", "chacha20-poly1305", "aes-ctr", "aes-cbc", "3des-cbc", "blowfish-cbc", "des-cbc"})
    void emptyIvIsRandomAndPrepended(String id) throws CryptoException {
        String key = switch (id) {
            case "des-cbc" -> "00112233deadbeef";
            case "3des-cbc" -> "00112233445566778899aabbccddeeff0011223344556677";
            default -> "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff";
        };
        Values vals = v().set("key", key, Format.HEX);
        String a = forward(id, MESSAGE, vals);
        String b = forward(id, MESSAGE, vals);
        assertNotEquals(a, b, "a fresh IV each time");
        assertEquals(MESSAGE, backward(id, a, vals));
        assertEquals(MESSAGE, backward(id, b, vals));
    }

    /** "The Galois/Counter Mode of Operation", test case 2. */
    @Test
    void aesGcmNistVector() throws CryptoException {
        Values vals = v().set("key", "00000000000000000000000000000000", Format.HEX)
            .set("nonce", "000000000000000000000000", Format.HEX);
        String out = run("aes-gcm", Direction.FORWARD, "00000000000000000000000000000000", Format.HEX, Format.HEX, vals).text();
        assertEquals("0388dace60b6a392f328c2b971b2fe78" + "ab6e47d42cec13bdf53a67b21257bddf", out);
    }

    /** RFC 8439, section 2.8.2. */
    @Test
    void chachaRfc8439Vector() throws CryptoException {
        Values vals = v().set("key", "808182838485868788898a8b8c8d8e8f909192939495969798999a9b9c9d9e9f", Format.HEX)
            .set("nonce", "070000004041424344454647", Format.HEX)
            .set("aad", "50515253c0c1c2c3c4c5c6c7", Format.HEX);
        String plain = "Ladies and Gentlemen of the class of '99: If I could offer you only one tip for the future,"
            + " sunscreen would be it.";
        String expected = "d31a8d34648e60db7b86afbc53ef7ec2a4aded51296e08fea9e2b5a736ee62d63dbea45e8ca9671282fafb69da92728b"
            + "1a71de0a9e060b2905d6a5b67ecd3b3692ddbd7f2d778b8c9803aee328091b58fab324e4fad675945585808b4831d7bc"
            + "3ff4def08e4b7a9de576d26586cec64b6116" + "1ae10b594f09e26a7e902ecbd0600691";
        assertEquals(expected, forward("chacha20-poly1305", plain, vals));
        assertEquals(plain, backward("chacha20-poly1305", expected, vals));
    }

    @Test
    void aeadDetectsTampering() throws CryptoException {
        Values vals = v().set("key", "00112233445566778899aabbccddeeff", Format.HEX);
        String c = forward("aes-gcm", MESSAGE, vals);
        char last = c.charAt(c.length() - 1);
        String tampered = c.substring(0, c.length() - 1) + (last == '0' ? '1' : '0');
        CryptoException e = assertThrows(CryptoException.class, () -> backward("aes-gcm", tampered, vals));
        assertEquals("error.decrypt.tag", e.key());
        Values wrongAad = v().set("key", "00112233445566778899aabbccddeeff", Format.HEX).set("aad", "header");
        assertThrows(CryptoException.class, () -> backward("aes-gcm", c, wrongAad));
    }

    @Test
    void passwordAead() throws CryptoException {
        String c = forward("aes-gcm-password", MESSAGE, v().set("password", "contraseña larga y buena"));
        assertEquals(1, Codec.unbase64(c)[0]);
        assertEquals(MESSAGE, backward("aes-gcm-password", c, v().set("password", "contraseña larga y buena")));
        CryptoException e = assertThrows(CryptoException.class,
            () -> backward("aes-gcm-password", c, v().set("password", "otra")));
        assertEquals("error.decrypt.password", e.key());
    }

    @Test
    void wrongKeySizesAreExplained() {
        CryptoException e = assertThrows(CryptoException.class, () -> forward("aes-cbc", MESSAGE, v().set("key", "short")));
        assertEquals("error.key.length", e.key());
        assertThrows(CryptoException.class, () -> forward("aes-cbc", MESSAGE,
            v().set("key", "0123456789abcdef").set("iv", "short")));
        assertThrows(CryptoException.class, () -> forward("blowfish-ecb", MESSAGE, v().set("key", "abc")));
        assertThrows(CryptoException.class, () -> forward("chacha20-poly1305", MESSAGE, v().set("key", "0123456789abcdef")));
    }

    @Test
    void wrongKeyFailsCleanly() throws CryptoException {
        String c = forward("aes-cbc", MESSAGE, v().set("key", "0123456789abcdef"));
        CryptoException e = assertThrows(CryptoException.class, () -> backward("aes-cbc", c, v().set("key", "fedcba9876543210")));
        assertTrue(e.key().startsWith("error.decrypt"));
    }

    @Test
    void ecbLeaksRepeatedBlocks() throws CryptoException {
        String c = forward("aes-ecb", "YELLOW SUBMARINEYELLOW SUBMARINE", v().set("key", "0123456789abcdef"));
        assertEquals(c.substring(0, 32), c.substring(32, 64));
    }
}
