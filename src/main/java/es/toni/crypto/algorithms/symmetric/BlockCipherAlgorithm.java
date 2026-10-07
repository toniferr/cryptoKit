package es.toni.crypto.algorithms.symmetric;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import es.toni.crypto.core.Codec;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Rand;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Security;
import es.toni.crypto.core.Values;

/**
 * A block cipher (AES, DES, Triple DES, Blowfish) in ECB, CBC or CTR mode, with PKCS#5/#7 padding for ECB and CBC.
 * <p>
 * Keys and IVs are typed as text by default, exactly as in version 1.0.0, so old ciphertexts still decrypt. Leaving
 * the IV empty is better: a random one is generated and written in front of the ciphertext, where decryption finds it.
 */
public final class BlockCipherAlgorithm extends SymmetricCipher {

    public enum Mode { ECB, CBC, CTR }

    private final String cipher;
    private final Mode mode;
    private final int block;
    private final int[] keySizes;   // allowed sizes; if empty, any size in [minKey, maxKey]
    private final int minKey;
    private final int maxKey;
    private final int defaultKey;

    private BlockCipherAlgorithm(String id, String name, Security security, String chapter, String cipher, Mode mode,
                                 int block, int[] keySizes, int minKey, int maxKey, int defaultKey) {
        super(id, name, security, chapter, Format.HEX);
        this.cipher = cipher;
        this.mode = mode;
        this.block = block;
        this.keySizes = keySizes;
        this.minKey = minKey;
        this.maxKey = maxKey;
        this.defaultKey = defaultKey;
    }

    public static BlockCipherAlgorithm aes(Mode mode, Security security) {
        String chapter = mode == Mode.ECB ? "modes#ecb" : mode == Mode.CBC ? "modes#cbc" : "modes#ctr";
        return new BlockCipherAlgorithm("aes-" + mode.name().toLowerCase(), "AES-" + mode, security, chapter, "AES",
            mode, 16, new int[] {16, 24, 32}, 0, 0, 32);
    }

    public static BlockCipherAlgorithm des(Mode mode) {
        return new BlockCipherAlgorithm("des-" + mode.name().toLowerCase(), "DES-" + mode, Security.BROKEN,
            "block#des", "DES", mode, 8, new int[] {8}, 0, 0, 8);
    }

    public static BlockCipherAlgorithm tripleDes() {
        return new BlockCipherAlgorithm("3des-cbc", "Triple DES-CBC", Security.LEGACY, "block#des", "DESede",
            Mode.CBC, 8, new int[] {16, 24}, 0, 0, 24);
    }

    public static BlockCipherAlgorithm blowfish(Mode mode) {
        return new BlockCipherAlgorithm("blowfish-" + mode.name().toLowerCase(), "Blowfish-" + mode, Security.LEGACY,
            "block#blowfish", "Blowfish", mode, 8, new int[0], 4, 56, 16);
    }

    @Override
    public List<Param> params() {
        List<Param> ps = new ArrayList<>();
        ps.add(Param.bytes("key", Format.TEXT, defaultKey));
        if (mode != Mode.ECB) {
            ps.add(Param.bytes("iv", Format.TEXT, block).asOptional());
        }
        return ps;
    }

    private String transformation() {
        return cipher + "/" + mode + "/" + (mode == Mode.CTR ? "NoPadding" : "PKCS5Padding");
    }

    private SecretKeySpec key(Values values) throws CryptoException {
        byte[] key = values.bytes("key");
        if (keySizes.length > 0) {
            checkKey(key, keySizes);
        } else {
            checkKeyRange(key, minKey, maxKey);
        }
        if (cipher.equals("DESede") && key.length == 16) {
            key = concat(key, slice(key, 0, 8));   // two-key Triple DES: K1 K2 K1
        }
        return new SecretKeySpec(key, cipher);
    }

    @Override
    public Result apply(Direction d, byte[] input, Values values) throws CryptoException {
        SecretKeySpec key = key(values);
        try {
            Cipher c = Cipher.getInstance(transformation());
            if (mode == Mode.ECB) {
                c.init(d == Direction.FORWARD ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key);
                Result r = Result.of(c.doFinal(input));
                return d == Direction.FORWARD ? r.note("note.ecb") : r;
            }
            boolean embedded = values.blank("iv");
            byte[] iv;
            byte[] data = input;
            if (!embedded) {
                iv = values.bytes("iv");
                if (iv.length != block) {
                    throw new CryptoException("error.iv.length", iv.length, block);
                }
            } else if (d == Direction.FORWARD) {
                iv = Rand.bytes(block);
            } else {
                if (input.length <= block) {
                    throw new CryptoException("error.iv.embedded", block);
                }
                iv = slice(input, 0, block);
                data = slice(input, block, input.length);
            }
            c.init(d == Direction.FORWARD ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] out = c.doFinal(data);
            if (d == Direction.FORWARD && embedded) {
                return Result.of(concat(iv, out)).note("note.iv.prepended", block, Codec.hex(iv));
            }
            Result r = Result.of(out);
            if (d == Direction.FORWARD && mode == Mode.CTR) {
                r.note("note.ctr.reuse");
            }
            return r;
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new CryptoException("error.decrypt.padding");
        } catch (GeneralSecurityException e) {
            throw new CryptoException(e, "error.crypto", e.getMessage());
        }
    }
}
