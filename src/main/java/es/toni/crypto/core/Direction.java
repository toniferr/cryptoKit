package es.toni.crypto.core;

/**
 * Every algorithm has two operations. FORWARD is encrypt, encode, hash, MAC or sign; BACKWARD is its counterpart:
 * decrypt, decode, or verify (for one-way functions and signatures).
 */
public enum Direction {
    FORWARD,
    BACKWARD
}
