package es.toni.crypto.core;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Text encodings of bytes: hexadecimal, Base32 (RFC 4648), Base58 (Bitcoin alphabet) and Base64 (standard and
 * URL-safe). Decoders are lenient with whitespace, so pasted, wrapped text works.
 */
public final class Codec {

    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final String BASE58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final BigInteger FIFTY_EIGHT = BigInteger.valueOf(58);

    private Codec() {
    }

    // ------------------------------------------------------------------ UTF-8

    public static byte[] utf8(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static String utf8(byte[] b) {
        return new String(b, StandardCharsets.UTF_8);
    }

    /** True if the bytes are well-formed UTF-8, i.e. they can be shown as text without losing anything. */
    public static boolean isUtf8(byte[] b) {
        try {
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(b));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }

    private static String compact(String s) {
        return s.replaceAll("\\s+", "");
    }

    // ------------------------------------------------------------------ hex

    public static String hex(byte[] b) {
        return HexFormat.of().formatHex(b);
    }

    /** Accepts upper or lower case, whitespace, ':' separators and an optional 0x prefix. */
    public static byte[] unhex(String s) throws CryptoException {
        String t = compact(s).replace(":", "");
        if (t.startsWith("0x") || t.startsWith("0X")) {
            t = t.substring(2);
        }
        if (t.length() % 2 != 0) {
            throw new CryptoException("error.hex.odd");
        }
        try {
            return HexFormat.of().parseHex(t);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("error.hex.invalid");
        }
    }

    // ------------------------------------------------------------------ Base64

    public static String base64(byte[] b) {
        return Base64.getEncoder().encodeToString(b);
    }

    public static String base64Url(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    /** Accepts the standard and the URL-safe alphabet, with or without padding. */
    public static byte[] unbase64(String s) throws CryptoException {
        String t = compact(s).replace('-', '+').replace('_', '/');
        t = t.replaceAll("=+$", "");
        if (t.length() % 4 == 1) {
            throw new CryptoException("error.base64.invalid");
        }
        try {
            return Base64.getDecoder().decode(t + "==".substring(0, (4 - t.length() % 4) % 4));
        } catch (IllegalArgumentException e) {
            throw new CryptoException("error.base64.invalid");
        }
    }

    // ------------------------------------------------------------------ Base32

    public static String base32(byte[] b) {
        StringBuilder out = new StringBuilder((b.length + 4) / 5 * 8);
        int buffer = 0;
        int bits = 0;
        for (byte x : b) {
            buffer = (buffer << 8) | (x & 0xff);
            bits += 8;
            while (bits >= 5) {
                out.append(BASE32.charAt((buffer >>> (bits - 5)) & 31));
                bits -= 5;
            }
        }
        if (bits > 0) {
            out.append(BASE32.charAt((buffer << (5 - bits)) & 31));
        }
        while (out.length() % 8 != 0) {
            out.append('=');
        }
        return out.toString();
    }

    public static byte[] unbase32(String s) throws CryptoException {
        String t = compact(s).toUpperCase().replaceAll("=+$", "");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int buffer = 0;
        int bits = 0;
        for (int i = 0; i < t.length(); i++) {
            int v = BASE32.indexOf(t.charAt(i));
            if (v < 0) {
                throw new CryptoException("error.base32.invalid");
            }
            buffer = (buffer << 5) | v;
            bits += 5;
            if (bits >= 8) {
                out.write((buffer >>> (bits - 8)) & 0xff);
                bits -= 8;
            }
        }
        return out.toByteArray();
    }

    // ------------------------------------------------------------------ Base58

    /** Bitcoin's alphabet: no 0, O, I or l. Every leading zero byte becomes a leading '1'. */
    public static String base58(byte[] b) {
        StringBuilder out = new StringBuilder();
        BigInteger n = new BigInteger(1, b);
        while (n.signum() > 0) {
            BigInteger[] qr = n.divideAndRemainder(FIFTY_EIGHT);
            out.append(BASE58.charAt(qr[1].intValue()));
            n = qr[0];
        }
        for (int i = 0; i < b.length && b[i] == 0; i++) {
            out.append('1');
        }
        return out.reverse().toString();
    }

    public static byte[] unbase58(String s) throws CryptoException {
        String t = compact(s);
        BigInteger n = BigInteger.ZERO;
        for (int i = 0; i < t.length(); i++) {
            int v = BASE58.indexOf(t.charAt(i));
            if (v < 0) {
                throw new CryptoException("error.base58.invalid");
            }
            n = n.multiply(FIFTY_EIGHT).add(BigInteger.valueOf(v));
        }
        byte[] body = n.signum() == 0 ? new byte[0] : n.toByteArray();
        if (body.length > 1 && body[0] == 0) {
            body = java.util.Arrays.copyOfRange(body, 1, body.length);
        }
        int zeros = 0;
        while (zeros < t.length() && t.charAt(zeros) == '1') {
            zeros++;
        }
        byte[] out = new byte[zeros + body.length];
        System.arraycopy(body, 0, out, zeros, body.length);
        return out;
    }
}
