package es.toni.crypto.core;

/**
 * A failure the user can act on (a key of the wrong length, malformed hex, a tampered ciphertext...). The message is a
 * resource-bundle key plus arguments, so the desktop app and the command line show it in the user's language.
 */
public class CryptoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String key;
    private final transient Object[] args;

    public CryptoException(String key, Object... args) {
        super(key);
        this.key = key;
        this.args = args;
    }

    public CryptoException(Throwable cause, String key, Object... args) {
        super(key, cause);
        this.key = key;
        this.args = args;
    }

    public String key() {
        return key;
    }

    public Object[] args() {
        return args.clone();
    }

    @Override
    public String getLocalizedMessage() {
        return Messages.get(key, args);
    }
}
