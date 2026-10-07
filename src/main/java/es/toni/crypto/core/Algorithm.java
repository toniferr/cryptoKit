package es.toni.crypto.core;

import java.util.List;

/**
 * An algorithm the kit offers. Implementations work on bytes; the UI and the CLI turn text boxes into bytes with the
 * formats in {@link #inputFormat} and {@link #outputFormat}, which the user can change.
 */
public abstract class Algorithm {

    /** A freshly generated key pair, PEM-encoded. */
    public record KeyPairPem(String publicPem, String privatePem) {
    }

    private final String id;
    private final String name;
    private final Family family;
    private final Security security;
    private final String chapter;

    protected Algorithm(String id, String name, Family family, Security security, String chapter) {
        this.id = id;
        this.name = name;
        this.family = family;
        this.security = security;
        this.chapter = chapter;
    }

    /** Stable identifier, used on the command line and in preferences (e.g. {@code aes-gcm}). */
    public final String id() {
        return id;
    }

    /** Display name (e.g. {@code AES-GCM}); the same in every language. */
    public final String name() {
        return name;
    }

    public final Family family() {
        return family;
    }

    public final Security security() {
        return security;
    }

    /** The portal page that explains it: a chapter id, optionally with a {@code #fragment}. */
    public final String chapter() {
        return chapter;
    }

    /** One-paragraph description, from the resource bundle. */
    public String description() {
        return Messages.get("alg." + id + ".desc");
    }

    public List<Param> params() {
        return List.of();
    }

    /** Resource-bundle key of the verb of each direction: encrypt/decrypt, encode/decode, hash/verify, sign/verify. */
    public abstract String verb(Direction d);

    public abstract Format inputFormat(Direction d);

    public abstract Format outputFormat(Direction d);

    /** Runs the operation. Throws {@link CryptoException} for anything the user can fix. */
    public abstract Result apply(Direction d, byte[] input, Values values) throws CryptoException;

    /** True for public-key algorithms, which can generate a key pair. */
    public boolean hasKeyPair() {
        return false;
    }

    public KeyPairPem generateKeyPair() throws CryptoException {
        throw new CryptoException("error.nokeypair");
    }

    @Override
    public String toString() {
        return name;
    }
}
