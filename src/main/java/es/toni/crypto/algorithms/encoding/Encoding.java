package es.toni.crypto.algorithms.encoding;

import es.toni.crypto.core.Algorithm;
import es.toni.crypto.core.Codec;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Family;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/** Writing bytes with a printable alphabet. Not encryption: there is no key, and anyone can decode it. */
public final class Encoding extends Algorithm {

    public enum Kind {
        HEX("hex", "Hexadecimal"),
        BASE32("base32", "Base32"),
        BASE58("base58", "Base58"),
        BASE64("base64", "Base64"),
        BASE64URL("base64url", "Base64URL");

        final String id;
        final String name;

        Kind(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private final Kind kind;

    public Encoding(Kind kind) {
        super(kind.id, kind.name, Family.ENCODING, Security.ENCODING, "intro#encodings");
        this.kind = kind;
    }

    @Override
    public String verb(Direction d) {
        return d == Direction.FORWARD ? "verb.encode" : "verb.decode";
    }

    @Override
    public Format inputFormat(Direction d) {
        return Format.TEXT;
    }

    @Override
    public Format outputFormat(Direction d) {
        return Format.TEXT;
    }

    @Override
    public Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        if (d == Direction.FORWARD) {
            String s = switch (kind) {
                case HEX -> Codec.hex(input);
                case BASE32 -> Codec.base32(input);
                case BASE58 -> Codec.base58(input);
                case BASE64 -> Codec.base64(input);
                case BASE64URL -> Codec.base64Url(input);
            };
            return Result.of(Codec.utf8(s));
        }
        String s = Codec.utf8(input);
        return Result.of(switch (kind) {
            case HEX -> Codec.unhex(s);
            case BASE32 -> Codec.unbase32(s);
            case BASE58 -> Codec.unbase58(s);
            case BASE64, BASE64URL -> Codec.unbase64(s);
        });
    }
}
