package es.toni.crypto.algorithms.signature;

import java.util.List;

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
 * A digital signature: the private key signs, anyone with the public key verifies. Signing outputs the signature
 * (Base64); verifying takes the message, the public key and the signature, and answers valid or not.
 */
public abstract class SignatureAlgorithm extends Algorithm {

    protected SignatureAlgorithm(String id, String name, String chapter) {
        super(id, name, Family.SIGNATURE, Security.RECOMMENDED, chapter);
    }

    @Override
    public List<Param> params() {
        return List.of(
            Param.pem("private", Param.FORWARD),
            Param.pem("public", Param.BACKWARD),
            Param.bytes("signature", Format.BASE64, 0).only(Param.BACKWARD));
    }

    @Override
    public String verb(Direction d) {
        return d == Direction.FORWARD ? "verb.sign" : "verb.verify";
    }

    @Override
    public Format inputFormat(Direction d) {
        return Format.TEXT;
    }

    @Override
    public Format outputFormat(Direction d) {
        return d == Direction.FORWARD ? Format.BASE64 : Format.TEXT;
    }

    @Override
    public boolean hasKeyPair() {
        return true;
    }

    protected abstract byte[] sign(byte[] message, Values values) throws CryptoException;

    protected abstract boolean verify(byte[] message, byte[] signature, Values values) throws CryptoException;

    @Override
    public final Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        if (d == Direction.FORWARD) {
            byte[] sig = sign(input, values);
            return Result.of(sig).note("note.signature.size", sig.length);
        }
        if (values.blank("signature")) {
            throw new CryptoException("error.signature.missing");
        }
        return Result.verdict(verify(input, values.bytes("signature"), values), input);
    }
}
