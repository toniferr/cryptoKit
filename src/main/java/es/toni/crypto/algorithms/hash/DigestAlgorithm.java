package es.toni.crypto.algorithms.hash;

import java.security.MessageDigest;
import java.util.List;
import java.util.function.Supplier;

import org.bouncycastle.crypto.Digest;

import es.toni.crypto.core.Algorithm;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Family;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/**
 * A cryptographic hash function (Bouncy Castle's lightweight digests). There is no way back: the second operation
 * verifies a message against an expected digest instead.
 */
public final class DigestAlgorithm extends Algorithm {

    private final Supplier<Digest> digest;

    public DigestAlgorithm(String id, String name, Security security, Supplier<Digest> digest) {
        super(id, name, Family.HASH, security, "hashing");
        this.digest = digest;
    }

    @Override
    public List<Param> params() {
        return List.of(Param.bytes("expected", Format.HEX, 0).only(Param.BACKWARD));
    }

    @Override
    public String verb(Direction d) {
        return d == Direction.FORWARD ? "verb.hash" : "verb.verify";
    }

    @Override
    public Format inputFormat(Direction d) {
        return Format.TEXT;
    }

    @Override
    public Format outputFormat(Direction d) {
        return Format.HEX;
    }

    public byte[] hash(byte[] input) {
        Digest md = digest.get();
        md.update(input, 0, input.length);
        byte[] out = new byte[md.getDigestSize()];
        md.doFinal(out, 0);
        return out;
    }

    @Override
    public Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        byte[] h = hash(input);
        if (d == Direction.FORWARD) {
            return Result.of(h).note("note.digest.bits", h.length * 8);
        }
        if (values.blank("expected")) {
            throw new CryptoException("error.expected.missing");
        }
        return Result.verdict(MessageDigest.isEqual(h, values.bytes("expected")), h);
    }
}
