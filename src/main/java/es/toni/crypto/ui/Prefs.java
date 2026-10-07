package es.toni.crypto.ui;

import java.util.Locale;
import java.util.prefs.Preferences;

import es.toni.crypto.core.Messages;

/** What the app remembers between runs: theme, language and the last algorithm used. */
final class Prefs {

    private static final Preferences NODE = Preferences.userRoot().node("es/toni/cryptokit");

    private Prefs() {
    }

    static boolean dark() {
        return NODE.getBoolean("dark", false);
    }

    static void dark(boolean on) {
        NODE.putBoolean("dark", on);
    }

    static Locale locale() {
        String tag = NODE.get("lang", "");
        return tag.isEmpty() ? Messages.defaultLocale() : Locale.forLanguageTag(tag);
    }

    static void locale(Locale l) {
        NODE.put("lang", l.getLanguage());
    }

    static String algorithm() {
        return NODE.get("algorithm", "aes-gcm");
    }

    static void algorithm(String id) {
        NODE.put("algorithm", id);
    }
}
