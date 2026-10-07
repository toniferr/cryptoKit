package es.toni.crypto.core;

import java.util.Base64;

/** PEM armour for keys: {@code -----BEGIN PUBLIC KEY-----} (X.509 SubjectPublicKeyInfo) and PKCS#8 private keys. */
public final class Pem {

    public static final String PUBLIC = "PUBLIC KEY";
    public static final String PRIVATE = "PRIVATE KEY";

    private Pem() {
    }

    public static String encode(String type, byte[] der) {
        String body = Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(der);
        return "-----BEGIN " + type + "-----\n" + body + "\n-----END " + type + "-----\n";
    }

    /** The DER bytes inside a PEM block of the given type. Bare Base64 (no armour) is accepted too. */
    public static byte[] decode(String type, String pem, String paramId) throws CryptoException {
        String text = pem.strip();
        if (text.isEmpty()) {
            throw new CryptoException("error.key.missing", Messages.get("param." + paramId));
        }
        if (text.startsWith("-----BEGIN")) {
            String begin = "-----BEGIN " + type + "-----";
            int start = text.indexOf(begin);
            int end = text.indexOf("-----END " + type + "-----");
            if (start < 0 || end < 0) {
                throw new CryptoException("error.key.pemType", Messages.get("param." + paramId), type);
            }
            text = text.substring(start + begin.length(), end);
        }
        try {
            return Base64.getMimeDecoder().decode(text);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("error.key.invalid", Messages.get("param." + paramId));
        }
    }
}
