// Known-answer tests for the portal's crypto library: node --test site/tests/
import { test } from "node:test";
import assert from "node:assert/strict";
import { createRequire } from "node:module";

const require = createRequire(import.meta.url);
const K = require("../src/js/cklib.js");

test("AES-128, FIPS-197 appendix C.1", () => {
  const aes = K.aes128(K.unhex("000102030405060708090a0b0c0d0e0f"));
  assert.equal(K.hex(aes.encryptBlock(K.unhex("00112233445566778899aabbccddeeff"))), "69c4e0d86a7b0430d8cdb78070b4c55a");
});

test("AES S-box computed from GF(2^8)", () => {
  assert.equal(K.SBOX[0x00], 0x63);
  assert.equal(K.SBOX[0x53], 0xed);   // FIPS-197, section 5.1.1
  assert.equal(K.gmul(0x57, 0x83), 0xc1);   // FIPS-197, section 4.2
  for (let a = 1; a < 256; a++) assert.equal(K.gmul(a, K.ginv(a)), 1);
});

test("AES trace: FIPS-197 appendix B, round 1 after SubBytes", () => {
  const aes = K.aes128(K.unhex("2b7e151628aed2a6abf7158809cf4f3c"));
  const t = aes.encryptBlock(K.unhex("3243f6a8885a308d313198a2e0370734"), true);
  assert.equal(K.hex(t.out), "3925841d02dc09fbdc118597196a0b32");
  const sb = t.steps.find((s) => s.round === 1 && s.op === "subBytes");
  assert.equal(K.hex(sb.state), "d42711aee0bf98f1b8b45de51e415230");
});

test("AES-CTR and CBC, NIST SP 800-38A F.5.1 / F.2.1 first block", () => {
  const aes = K.aes128(K.unhex("2b7e151628aed2a6abf7158809cf4f3c"));
  const p = K.unhex("6bc1bee22e409f96e93d7e117393172a");
  assert.equal(K.hex(K.ctr(aes, p, K.unhex("f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff"))), "874d6191b620e3261bef6864990db6ce");
  assert.equal(K.hex(K.cbc(aes, p, K.unhex("000102030405060708090a0b0c0d0e0f"))), "7649abac8119b246cee98e9b12e9197d");
});

test("SHA-256", () => {
  assert.equal(K.hex(K.sha256("abc")), "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
  assert.equal(K.hex(K.sha256("")), "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
  assert.equal(K.hex(K.sha256("a".repeat(1000))), "41edece42d63e8d9bf515a9ba6932e1c20cbc9f5a5d134645adb5db1b9737ea3");
});

test("Enigma I, the same answers as the desktop app", () => {
  assert.equal(new K.Enigma().encrypt("AAAAA"), "BDZGO");
  assert.equal(new K.Enigma().encrypt("HELLOWORLD"), "ILBDAAMTAZ");
  const s = { rotors: [3, 1, 4], reflector: "C", rings: [1, 20, 11], positions: [1, 11, 0], plugboard: "AV BS CG DL FU HZ IN KM OW RX" };
  const c = new K.Enigma(s).encrypt("WETTERBERICHT");
  assert.equal(new K.Enigma(s).encrypt(c), "WETTERBERICHT");
  const m = new K.Enigma({ positions: [0, 3, 20] });
  const seen = [];
  for (let i = 0; i < 4; i++) { m.press(0); seen.push(m.window()); }
  assert.deepEqual(seen, ["ADV", "AEW", "BFX", "BFY"]);
});

test("classical ciphers", () => {
  assert.equal(K.caesar("Hello, World!", 3), "Khoor, Zruog!");
  assert.equal(K.vigenere("ATTACKATDAWN", "LEMON"), "LXFOPVEFRNHR");
  assert.equal(K.vigenere("LXFOPVEFRNHR", "LEMON", -1), "ATTACKATDAWN");
  const english = "it was the best of times it was the worst of times it was the age of wisdom it was the age of foolishness";
  assert.ok(K.ic(english) > 0.06);
  assert.ok(K.ic(K.vigenere(english, "CRYPTOKIT")) < 0.05);
});

test("number theory", () => {
  assert.equal(K.modpow(4, 13, 497), 445n);
  assert.equal(K.modinv(17, 3120), 2753n);
  assert.equal(K.modpowTrace(4, 13, 497).result, 445n);
  assert.ok(K.isPrime(2147483647n) && !K.isPrime(2147483649n));
});

test("elliptic curve y^2 = x^3 + 2x + 3 over F_97", () => {
  const E = new K.Curve(2, 3, 97);
  const P = { x: 3, y: 6 };
  assert.ok(E.on(P));
  const Q = E.add(P, P);
  assert.ok(E.on(Q));
  assert.deepEqual(E.mul(5, P), E.add(E.mul(2, P), E.mul(3, P)));
  assert.equal(E.mul(E.order(P), P), null);
});

test("toy LWE decrypts with small noise", () => {
  const r = K.rng(7);
  const k = K.lweKeygen(8, 40, 401, 2, r);
  for (let i = 0; i < 50; i++) {
    const bit = i % 2;
    assert.equal(K.lweDecrypt(k, K.lweEncrypt(k, bit, r)).bit, bit);
  }
});
