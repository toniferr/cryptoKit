# Changelog

All notable changes to cryptoKit. The format follows [Keep a Changelog](https://keepachangelog.com/) and versions follow
[Semantic Versioning](https://semver.org/). Each section is also the text of its GitHub release.

## [2.0.0] - 2026-10-07

A rewrite from the core, with a portal on the mathematics and history of cryptography:
**https://toniferr.github.io/cryptoKit/**

**Requirements:** Java 21 or newer for the jar (`java -jar cryptoKit-2.0.0.jar`), or download the portable package for
your system, which includes its own Java. The macOS package is not signed: open it with right-click → Open the first
time.

### Added
- 29 new algorithms, 44 in total, in eight families, each with a security badge (recommended, legacy, broken,
  historical, encoding), a description and a link to the portal chapter that explains it:
  - classical ciphers: Caesar, Vigenère, Playfair and an Enigma I simulator (rotors I–V, reflectors B and C, ring
    settings, plugboard, double step);
  - encodings: Base32, Base58 and Base64URL;
  - hashes: BLAKE2b-512, BLAKE3 and RIPEMD-160; verification of an expected digest;
  - message authentication: HMAC-SHA256, HMAC-SHA512 and HMAC-SHA3-256;
  - password hashing: Argon2id, scrypt, bcrypt and PBKDF2-HMAC-SHA256, with verification;
  - symmetric: AES-CTR, ChaCha20-Poly1305, associated data for AEAD modes, and AES-GCM with a password (Argon2id);
  - public-key encryption: RSA-OAEP, X25519 + AES-GCM and post-quantum ML-KEM-768 + AES-GCM (FIPS 203);
  - signatures: Ed25519, ECDSA P-256, RSA-PSS and post-quantum ML-DSA-65 (FIPS 204).
- New interface (FlatLaf): light and dark themes, English and Spanish, searchable algorithm list, form built for each
  algorithm, random key and IV generators, key-pair generation in PEM, input and output as text, hex or Base64,
  load and save files, “use the result” to decrypt or verify what was just produced, operations in the background.
- Command-line interface in the same jar: `list`, `info`, `keygen`, and every algorithm's operations, with options,
  standard input, `@file` values and meaningful exit codes.
- Portable packages for Windows, macOS and Linux with a bundled Java runtime.
- Portal (GitHub Pages): ten chapters with theorems, proofs, history and 25 interactive figures running real
  algorithms in the browser, an introduction, a 4,000-year timeline and a page about the app, in English and Spanish.
- 89 automated tests: official vectors (NIST FIPS-197 and GCM, RFC 4231, 4648, 8032, 8439), the Enigma textbook
  vectors, the CLI, the translations and the user interface.

### Changed
- Java 21 (was 11); Bouncy Castle 1.86 (`bcprov-jdk18on`, was `bcprov-jdk15on` 1.70), used only through its
  lightweight API, so the jar works on every JDK, including Oracle's.
- Leaving the IV or nonce empty now generates a random one and prepends it to the ciphertext; decryption reads it back.
  Typing an IV still works exactly as in 1.0.
- Results no longer end in “(hexadecimal)”: the output format is chosen in a selector.
- Errors are explained in a banner instead of being printed as the result.
- Text is always encoded as UTF-8, whatever the system's default charset.
- The runnable jar is now `cryptoKit-2.0.0.jar` (it was `cryptoKit-1.0.0-jar-with-dependencies.jar`).
- Continuous integration moved to GitHub Actions.

### Compatibility
- Ciphertexts produced by 1.0 still decrypt: DES, AES and Blowfish (ECB and CBC), AES-GCM and Jasypt PBE, with keys
  and IVs typed as text. The test suite checks this against output of the real 1.0 jar.

### Removed
- The Jasypt dependency: `PBEWithHMACSHA512AndAES_256` is now implemented with the JDK and tested against Jasypt in both
  directions.
- Travis CI, CircleCI and Codecov configuration (those services no longer build this project).

### Fixed
- The tests of 1.0 never ran (the test sources were outside Maven's test directory) and did not compile (they imported
  `es.toni.crytpo`).
- The symmetric output said “(hexadecimal)” twice.
- PBE (Jasypt) failed with non-ASCII passwords; it now explains that the legacy scheme only accepts ASCII.
- Wrong key or IV sizes, malformed hex or Base64 and wrong passwords give clear messages instead of Java exceptions.

## [1.0.0] - 2022-05-30

First release: a runnable jar with dependencies (`java -jar cryptoKit-1.0.0-jar-with-dependencies.jar`, Java 11).

### Added
- Hexadecimal and Base64 encoding.
- Hashes: MD5, SHA-1, SHA-256, SHA-512, SHA3-256 and SHA3-512.
- Symmetric ciphers: DES (ECB, CBC), AES (ECB, CBC, GCM), Blowfish (ECB, CBC) and PBEWithHMACSHA512AndAES_256 with
  Jasypt.
- Swing interface in Spanish; Bouncy Castle 1.70 as provider.

### Before 1.0.0
- 2017: first version, an Eclipse project with symmetric ciphers, hashes and Base64 on Bouncy Castle 1.57.
- 2020: Java 11, Maven, SHA-2 and SHA-3, AES-GCM, Blowfish, Travis CI, CircleCI and Codecov, and a wiki in Spanish.

[2.0.0]: https://github.com/toniferr/cryptoKit/compare/1.0.0...2.0.0
[1.0.0]: https://github.com/toniferr/cryptoKit/releases/tag/1.0.0
