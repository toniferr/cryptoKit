package es.toni.crypto.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/** Every text the code asks for exists, in both languages. */
class MessagesTest {

    private static Properties load(String name) throws IOException {
        Properties p = new Properties();
        try (InputStream in = MessagesTest.class.getResourceAsStream("/es/toni/crypto/i18n/" + name)) {
            p.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
        }
        return p;
    }

    @Test
    void bothLanguagesHaveTheSameKeys() throws IOException {
        Set<String> en = new TreeSet<>(load("messages.properties").stringPropertyNames());
        Set<String> es = new TreeSet<>(load("messages_es.properties").stringPropertyNames());
        Set<String> onlyEn = new TreeSet<>(en);
        onlyEn.removeAll(es);
        Set<String> onlyEs = new TreeSet<>(es);
        onlyEs.removeAll(en);
        assertEquals(Set.of(), onlyEn, "missing in Spanish");
        assertEquals(Set.of(), onlyEs, "missing in English");
    }

    @Test
    void everyAlgorithmAndParameterIsDescribed() throws IOException {
        Properties en = load("messages.properties");
        for (Algorithm a : Algorithms.all()) {
            assertTrue(en.containsKey("alg." + a.id() + ".desc"), a.id());
            assertTrue(en.containsKey(a.verb(Direction.FORWARD)) && en.containsKey(a.verb(Direction.BACKWARD)), a.id());
            String chapter = a.chapter().split("#")[0];
            assertTrue(en.containsKey("url." + chapter), "url." + chapter);
            for (Param p : a.params()) {
                assertTrue(en.containsKey(p.labelKey()), p.labelKey());
            }
        }
        for (Family f : Family.values()) {
            assertTrue(en.containsKey(f.key()));
        }
        for (Security s : Security.values()) {
            assertTrue(en.containsKey(s.key()) && en.containsKey(s.key() + ".hint"));
        }
    }

    /** Scans the sources for "error.x", "note.x", "ui.x"... literals: each must be a key. */
    @Test
    void everyKeyUsedInTheCodeExists() throws IOException {
        Properties en = load("messages.properties");
        Pattern key = Pattern.compile("\"((?:error|note|ui|menu|cli|verdict|about)\\.[A-Za-z0-9.]+)\"");
        Set<String> missing = new TreeSet<>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path f : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher m = key.matcher(Files.readString(f));
                while (m.find()) {
                    if (!en.containsKey(m.group(1))) {
                        missing.add(m.group(1) + " (" + f.getFileName() + ")");
                    }
                }
            }
        }
        assertEquals(Set.of(), missing);
    }

    @Test
    void placeholdersAreReplaced() {
        Messages.setLocale(Messages.SPANISH);
        try {
            assertEquals("La clave tiene 5 bytes; este cifrado necesita 16 bytes.", Messages.get("error.key.length", 5, 16));
        } finally {
            Messages.setLocale(Messages.ENGLISH);
        }
        assertEquals("The key is 5 bytes long; this cipher needs 16 bytes.", Messages.get("error.key.length", 5, 16));
    }
}
