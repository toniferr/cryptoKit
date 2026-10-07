package es.toni.crypto.core;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The user-visible strings, in English (default) and Spanish. Placeholders are {0}, {1}... and are replaced literally
 * (no MessageFormat), so apostrophes in the texts need no escaping.
 */
public final class Messages {

    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale SPANISH = Locale.forLanguageTag("es");

    private static final String BUNDLE = "es.toni.crypto.i18n.messages";
    private static volatile Locale locale = defaultLocale();
    private static volatile ResourceBundle bundle = load(locale);

    private Messages() {
    }

    /** Spanish if the system speaks Spanish, English otherwise. */
    public static Locale defaultLocale() {
        return "es".equals(Locale.getDefault().getLanguage()) ? SPANISH : ENGLISH;
    }

    public static Locale locale() {
        return locale;
    }

    public static boolean spanish() {
        return "es".equals(locale.getLanguage());
    }

    public static void setLocale(Locale l) {
        locale = "es".equals(l.getLanguage()) ? SPANISH : ENGLISH;
        bundle = load(locale);
    }

    private static ResourceBundle load(Locale l) {
        // The root bundle is English; ROOT fallback keeps an English JVM from picking messages_es by accident.
        return ResourceBundle.getBundle(BUNDLE, l, ResourceBundle.Control.getNoFallbackControl(
            ResourceBundle.Control.FORMAT_PROPERTIES));
    }

    public static boolean has(String key) {
        return bundle.containsKey(key);
    }

    public static String get(String key, Object... args) {
        String text;
        try {
            text = bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
        for (int i = 0; i < args.length; i++) {
            text = text.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return text;
    }

    /** The key's text if it exists, otherwise {@code fallback}. */
    public static String getOr(String key, String fallback) {
        return has(key) ? get(key) : fallback;
    }
}
