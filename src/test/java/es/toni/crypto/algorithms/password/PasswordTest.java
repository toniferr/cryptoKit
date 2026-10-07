package es.toni.crypto.algorithms.password;

import static es.toni.crypto.TestSupport.forward;
import static es.toni.crypto.TestSupport.run;
import static es.toni.crypto.TestSupport.v;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import es.toni.crypto.core.Codec;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Values;

class PasswordTest {

    /** Cheap costs so the tests run fast; the format and the verification are what matter here. */
    @ParameterizedTest
    @CsvSource({
        "argon2id, memory, 64, $argon2id$v=19$m=64,t=2,p=1$",
        "scrypt, ln, 10, $scrypt$ln=10,r=8,p=1$",
        "bcrypt, cost, 4, $2b$04$",
        "pbkdf2, iterations, 1000, $pbkdf2-sha256$i=1000$",
    })
    void hashThenVerify(String id, String costParam, String cost, String prefix) throws CryptoException {
        Values params = v().set(costParam, cost);
        String a = forward(id, "correct horse battery staple", params);
        String b = forward(id, "correct horse battery staple", params);
        assertTrue(a.startsWith(prefix), a);
        assertNotEquals(a, b, "a random salt makes every hash different");
        assertTrue(run(id, Direction.BACKWARD, "correct horse battery staple", v().set("encoded", a)).verified());
        assertFalse(run(id, Direction.BACKWARD, "correct horse battery stapler", v().set("encoded", a)).verified());
    }

    /** PBKDF2-HMAC-SHA256("password", "salt", 1 iteration), the well-known test vector. */
    @Test
    void pbkdf2KnownAnswer() throws CryptoException {
        byte[] dk = Pbkdf2.derive(Codec.utf8("password"), Codec.utf8("salt"), 1, 32);
        assertEquals("120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b", Codec.hex(dk));
        String encoded = "$pbkdf2-sha256$i=1$" + PasswordHash.b64(Codec.utf8("salt")) + "$" + PasswordHash.b64(dk);
        assertTrue(run("pbkdf2", Direction.BACKWARD, "password", v().set("encoded", encoded)).verified());
    }

    @Test
    void rejectsMalformedOrForeignHashes() {
        assertThrows(CryptoException.class,
            () -> run("argon2id", Direction.BACKWARD, "x", v().set("encoded", "$scrypt$ln=10,r=8,p=1$c2FsdA$aGFzaA")));
        assertThrows(CryptoException.class,
            () -> run("argon2id", Direction.BACKWARD, "x", v().set("encoded", "$argon2id$v=19$m=bad$c2FsdA$aGFzaA")));
        assertThrows(CryptoException.class, () -> run("bcrypt", Direction.BACKWARD, "x", v().set("encoded", "$2b$04$short")));
        assertThrows(CryptoException.class, () -> run("pbkdf2", Direction.BACKWARD, "x", v()));
    }

    @Test
    void bcryptRejectsLongPasswords() {
        assertThrows(CryptoException.class, () -> forward("bcrypt", "x".repeat(73), v().set("cost", "4")));
    }
}
