package es.toni.crypto.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class CodecTest {

    /** RFC 4648, section 10. */
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "''|''|''",
        "f|Zg==|MY======",
        "fo|Zm8=|MZXQ====",
        "foo|Zm9v|MZXW6===",
        "foob|Zm9vYg==|MZXW6YQ=",
        "fooba|Zm9vYmE=|MZXW6YTB",
        "foobar|Zm9vYmFy|MZXW6YTBOI======",
    })
    void rfc4648(String plain, String b64, String b32) throws CryptoException {
        byte[] p = Codec.utf8(plain);
        assertEquals(b64, Codec.base64(p));
        assertEquals(b32, Codec.base32(p));
        assertArrayEquals(p, Codec.unbase64(b64));
        assertArrayEquals(p, Codec.unbase32(b32));
    }

    @Test
    void hexIsLenient() throws CryptoException {
        assertArrayEquals(new byte[] {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef}, Codec.unhex("0xDE:AD be ef"));
        assertEquals("deadbeef", Codec.hex(Codec.unhex("DEADBEEF")));
        assertThrows(CryptoException.class, () -> Codec.unhex("abc"));
        assertThrows(CryptoException.class, () -> Codec.unhex("zz"));
    }

    @Test
    void base64AcceptsUrlAlphabetAndMissingPadding() throws CryptoException {
        assertArrayEquals(new byte[] {(byte) 0xfb, (byte) 0xff}, Codec.unbase64("-_8"));
        assertArrayEquals(new byte[] {(byte) 0xfb, (byte) 0xff}, Codec.unbase64("+/8="));
        assertEquals("-_8", Codec.base64Url(new byte[] {(byte) 0xfb, (byte) 0xff}));
        assertThrows(CryptoException.class, () -> Codec.unbase64("a"));
    }

    @Test
    void base58() throws CryptoException {
        assertEquals("2NEpo7TZRRrLZSi2U", Codec.base58(Codec.utf8("Hello World!")));
        assertArrayEquals(Codec.utf8("Hello World!"), Codec.unbase58("2NEpo7TZRRrLZSi2U"));
        // Leading zero bytes become leading 1s and come back.
        assertEquals("112", Codec.base58(new byte[] {0, 0, 1}));
        assertArrayEquals(new byte[] {0, 0, 1}, Codec.unbase58("112"));
        assertArrayEquals(new byte[] {0}, Codec.unbase58("1"));
        assertThrows(CryptoException.class, () -> Codec.unbase58("0OIl"));
    }

    @Test
    void utf8Detection() {
        assertTrue(Codec.isUtf8(Codec.utf8("¿Qué tal? 🔐")));
        assertFalse(Codec.isUtf8(new byte[] {(byte) 0xc3, 0x28}));
    }
}
