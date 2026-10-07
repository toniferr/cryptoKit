package es.toni.crypto.algorithms.asymmetric;

import java.util.List;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

import es.toni.crypto.core.Algorithm;
import es.toni.crypto.core.Codec;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Family;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Security;

/** Public-key encryption: anyone with the public key encrypts, only the private key decrypts. Output in Base64. */
abstract class PublicKeyCipher extends Algorithm {

    PublicKeyCipher(String id, String name, String chapter) {
        super(id, name, Family.ASYMMETRIC, Security.RECOMMENDED, chapter);
    }

    @Override
    public List<Param> params() {
        return List.of(Param.pem("public", Param.FORWARD), Param.pem("private", Param.BACKWARD));
    }

    @Override
    public String verb(Direction d) {
        return d == Direction.FORWARD ? "verb.encrypt" : "verb.decrypt";
    }

    @Override
    public Format inputFormat(Direction d) {
        return d == Direction.FORWARD ? Format.TEXT : Format.BASE64;
    }

    @Override
    public Format outputFormat(Direction d) {
        return d == Direction.FORWARD ? Format.BASE64 : Format.TEXT;
    }

    @Override
    public boolean hasKeyPair() {
        return true;
    }

    /** HKDF-SHA-256 (RFC 5869): turns a shared secret into an AES-256 key bound to this scheme. */
    static byte[] hkdf(byte[] secret, byte[] salt, String info) {
        HKDFBytesGenerator gen = new HKDFBytesGenerator(new SHA256Digest());
        gen.init(new HKDFParameters(secret, salt, Codec.utf8(info)));
        byte[] key = new byte[32];
        gen.generateBytes(key, 0, key.length);
        return key;
    }
}
