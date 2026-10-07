package es.toni.crypto.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.toni.crypto.core.Messages;

class CliTest {

    private String out;
    private String err;

    private int cli(String stdin, String... args) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ByteArrayOutputStream e = new ByteArrayOutputStream();
        int code = Cli.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
            new PrintStream(o, true, StandardCharsets.UTF_8), new PrintStream(e, true, StandardCharsets.UTF_8));
        out = o.toString(StandardCharsets.UTF_8).strip();
        err = e.toString(StandardCharsets.UTF_8).strip();
        return code;
    }

    @AfterEach
    void english() {
        Messages.setLocale(Messages.ENGLISH);
    }

    @Test
    void helpVersionAndList() {
        assertEquals(0, cli("", "--help"));
        assertTrue(out.contains("cryptokit list"));
        assertEquals(0, cli("", "--version"));
        assertTrue(out.startsWith("cryptoKit "));
        assertEquals(0, cli("", "list", "--lang=en"));
        assertTrue(out.contains("ml-kem-768") && out.contains("enigma") && out.contains("Broken"));
        assertEquals(0, cli("", "info", "aes-gcm"));
        assertTrue(out.contains("--nonce"));
    }

    @Test
    void hashFromArgumentOrStdin() {
        assertEquals(0, cli("", "sha256", "hash", "abc"));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", out);
        assertEquals(0, cli("abc", "sha256", "hash"));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", out);
        assertEquals(0, cli("", "sha256", "hash", "--out=base64", "abc"));
        assertEquals("ungWv48Bz+pBQUDeXa4iI7ADYaOWF3qctBD/YfIAFa0=", out);
    }

    @Test
    void encryptThenDecrypt() {
        String key = "--key=00112233445566778899aabbccddeeff";
        assertEquals(0, cli("", "aes-gcm", "encrypt", key, "--key-format=hex", "attack at dawn"));
        String c = out;
        assertEquals(0, cli("", "aes-gcm", "decrypt", key, "--key-format", "hex", c));
        assertEquals("attack at dawn", out);
        assertEquals(2, cli("", "aes-gcm", "decrypt", "--key=00112233445566778899aabbccddeeee", "--key-format=hex", c));
        assertTrue(err.contains("Authentication failed"));
    }

    @Test
    void verificationsUseTheExitStatus() {
        assertEquals(0, cli("", "sha256", "verify", "--expected=ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", "abc"));
        assertEquals(1, cli("", "sha256", "verify", "--expected=00", "abc"));
    }

    @Test
    void keysFromFiles(@TempDir Path dir) throws Exception {
        assertEquals(0, cli("", "keygen", "ed25519"));
        String pem = out;
        int split = pem.indexOf("-----BEGIN PRIVATE KEY-----");
        Files.writeString(dir.resolve("pub.pem"), pem.substring(0, split));
        Files.writeString(dir.resolve("key.pem"), pem.substring(split));
        assertEquals(0, cli("", "ed25519", "sign", "--private=@" + dir.resolve("key.pem"), "I owe you 10 euros"));
        String sig = out;
        assertEquals(0, cli("", "ed25519", "verify", "--public=@" + dir.resolve("pub.pem"), "--signature=" + sig,
            "I owe you 10 euros"));
        assertEquals(1, cli("", "ed25519", "verify", "--public=@" + dir.resolve("pub.pem"), "--signature=" + sig,
            "I owe you 100 euros"));
    }

    @Test
    void errorsAreTranslatedAndExplained() {
        assertEquals(2, cli("", "rot47", "encrypt", "x"));
        assertTrue(err.contains("Unknown algorithm"));
        assertEquals(2, cli("", "--lang=es", "aes-cbc", "encrypt", "--key=corta", "x"));
        assertTrue(err.contains("La clave tiene 5 bytes"), err);
        assertEquals(2, cli("", "caesar", "encrypt", "--colour=red", "x"));
        assertEquals(2, cli("", "caesar", "frobnicate", "x"));
    }
}
