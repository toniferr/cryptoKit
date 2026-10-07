package es.toni.crypto.core;

/** How bytes are written in a text box: as UTF-8 text, as hexadecimal or as Base64. */
public enum Format {
    TEXT,
    HEX,
    BASE64;

    public byte[] decode(String s) throws CryptoException {
        return switch (this) {
            case TEXT -> Codec.utf8(s);
            case HEX -> Codec.unhex(s);
            case BASE64 -> Codec.unbase64(s);
        };
    }

    public String encode(byte[] b) {
        return switch (this) {
            case TEXT -> Codec.utf8(b);
            case HEX -> Codec.hex(b);
            case BASE64 -> Codec.base64(b);
        };
    }

    public String key() {
        return "format." + name().toLowerCase();
    }

    /** Parses "text", "hex" or "base64" (as typed on the command line). */
    public static Format parse(String s) throws CryptoException {
        return switch (s.toLowerCase()) {
            case "text", "utf8", "utf-8" -> TEXT;
            case "hex" -> HEX;
            case "base64", "b64" -> BASE64;
            default -> throw new CryptoException("error.format.unknown", s);
        };
    }
}
