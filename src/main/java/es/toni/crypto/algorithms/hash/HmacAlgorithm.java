package es.toni.crypto.algorithms.hash;

import java.security.MessageDigest;
import java.util.List;
import java.util.function.Supplier;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

import es.toni.crypto.core.Algorithm;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Family;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/** HMAC (Bellare, Canetti and Krawczyk, 1996): a keyed hash that proves who wrote a message and that it is intact. */
public final class HmacAlgorithm extends Algorithm {

    private final Supplier<Digest> digest;

    public HmacAlgorithm(String id, String name, Supplier<Digest> digest) {
        super(id, name, Family.MAC, Security.RECOMMENDED, "hashing#hmac");
        this.digest = digest;
    }

    @Override
    public List<Param> params() {
        return List.of(
            Param.bytes("key", Format.TEXT, 32),
            Param.bytes("expected", Format.HEX, 0).only(Param.BACKWARD));
    }

    @Override
    public String verb(Direction d) {
        return d == Direction.FORWARD ? "verb.mac" : "verb.verify";
    }

    @Override
    public Format inputFormat(Direction d) {
        return Format.TEXT;
    }

    @Override
    public Format outputFormat(Direction d) {
        return Format.HEX;
    }

    @Override
    public Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        byte[] key = values.bytes("key");
        if (key.length == 0) {
            throw new CryptoException("error.key.missing", es.toni.crypto.core.Messages.get("param.key"));
        }
        HMac mac = new HMac(digest.get());
        mac.init(new KeyParameter(key));
        mac.update(input, 0, input.length);
        byte[] tag = new byte[mac.getMacSize()];
        mac.doFinal(tag, 0);
        if (d == Direction.FORWARD) {
            Result r = Result.of(tag);
            if (key.length < 16) {
                r.note("note.key.short", key.length);
            }
            return r;
        }
        if (values.blank("expected")) {
            throw new CryptoException("error.expected.missing");
        }
        return Result.verdict(MessageDigest.isEqual(tag, values.bytes("expected")), tag);
    }
}
