package es.toni.crypto.core;

import java.util.HashMap;
import java.util.Map;

/** The parameter values typed by the user, with the format of each BYTES field. */
public final class Values {

    private final Map<String, String> raw = new HashMap<>();
    private final Map<String, Format> formats = new HashMap<>();

    public Values set(String id, String value) {
        raw.put(id, value == null ? "" : value);
        return this;
    }

    public Values set(String id, String value, Format format) {
        set(id, value);
        formats.put(id, format);
        return this;
    }

    public Values format(String id, Format format) {
        formats.put(id, format);
        return this;
    }

    public String text(String id) {
        return raw.getOrDefault(id, "");
    }

    public boolean blank(String id) {
        return text(id).isBlank();
    }

    public Format formatOf(String id, Format fallback) {
        return formats.getOrDefault(id, fallback);
    }

    /** The field decoded as bytes, with the format chosen for it (text if none was chosen). */
    public byte[] bytes(String id) throws CryptoException {
        Format f = formatOf(id, Format.TEXT);
        String v = text(id);
        try {
            return f.decode(f == Format.TEXT ? v : v.strip());
        } catch (CryptoException e) {
            throw new CryptoException("error.param.format", Messages.get("param." + id), Messages.get(f.key()));
        }
    }

    public int integer(String id, int min, int max) throws CryptoException {
        int v;
        try {
            v = Integer.parseInt(text(id).strip());
        } catch (NumberFormatException e) {
            throw new CryptoException("error.param.number", Messages.get("param." + id));
        }
        if (v < min || v > max) {
            throw new CryptoException("error.param.range", Messages.get("param." + id), min, max);
        }
        return v;
    }

    /** Fills in the defaults of the parameters that were not given. */
    public Values withDefaults(Algorithm a) {
        for (Param p : a.params()) {
            raw.putIfAbsent(p.id(), p.defaultValue());
            formats.putIfAbsent(p.id(), p.format());
        }
        return this;
    }
}
