// cklib — the small cryptographic toolkit behind the portal's figures. Real algorithms (AES-128, SHA-256, Enigma I)
// written for reading and tracing, not for production: no constant-time code, no side-channel care. The same file is
// loaded by node for the tests in site/tests (module.exports) and by the browser as window.CKL.
(function (root, factory) {
  "use strict";
  var lib = factory();
  if (typeof module === "object" && module.exports) module.exports = lib;
  else root.CKL = lib;
})(this, function () {
  "use strict";

  // ------------------------------------------------------------------ bytes

  function utf8(s) { return new TextEncoder().encode(s); }
  function fromUtf8(b) { return new TextDecoder().decode(b); }
  function hex(b) {
    var out = "";
    for (var i = 0; i < b.length; i++) out += (b[i] < 16 ? "0" : "") + b[i].toString(16);
    return out;
  }
  function unhex(s) {
    s = s.replace(/[^0-9a-fA-F]/g, "");
    var out = new Uint8Array(s.length >> 1);
    for (var i = 0; i < out.length; i++) out[i] = parseInt(s.substr(2 * i, 2), 16);
    return out;
  }
  function xor(a, b) {
    var n = Math.min(a.length, b.length), out = new Uint8Array(n);
    for (var i = 0; i < n; i++) out[i] = a[i] ^ b[i];
    return out;
  }
  function concat(a, b) { var out = new Uint8Array(a.length + b.length); out.set(a); out.set(b, a.length); return out; }
  function randomBytes(n) {
    var b = new Uint8Array(n);
    (typeof crypto !== "undefined" ? crypto : require("crypto").webcrypto).getRandomValues(b);
    return b;
  }
  function popcount8(x) { x = x - ((x >> 1) & 0x55); x = (x & 0x33) + ((x >> 2) & 0x33); return (x + (x >> 4)) & 0x0f; }
  function bitDiff(a, b) { var d = 0; for (var i = 0; i < a.length; i++) d += popcount8(a[i] ^ b[i]); return d; }

  var B64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
  function base64(b) {
    var out = "";
    for (var i = 0; i < b.length; i += 3) {
      var n = (b[i] << 16) | ((b[i + 1] || 0) << 8) | (b[i + 2] || 0);
      out += B64[(n >> 18) & 63] + B64[(n >> 12) & 63] + (i + 1 < b.length ? B64[(n >> 6) & 63] : "=") +
        (i + 2 < b.length ? B64[n & 63] : "=");
    }
    return out;
  }

  // A small deterministic generator (mulberry32), so figures can be reproducible.
  function rng(seed) {
    var a = seed >>> 0;
    return function () {
      a = (a + 0x6d2b79f5) >>> 0;
      var t = a;
      t = Math.imul(t ^ (t >>> 15), t | 1);
      t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }

  // ------------------------------------------------------------------ classical ciphers

  var ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  function isLetter(c) { return /[A-Za-z]/.test(c); }
  function shiftChar(c, k) {
    var up = c.toUpperCase(), i = ALPHA.indexOf(up);
    if (i < 0) return c;
    var out = ALPHA[((i + k) % 26 + 26) % 26];
    return c === up ? out : out.toLowerCase();
  }
  function caesar(text, k) { return text.replace(/[A-Za-z]/g, function (c) { return shiftChar(c, k); }); }
  function vigenere(text, key, sign) {
    key = key.toUpperCase().replace(/[^A-Z]/g, "");
    if (!key) return text;
    var j = 0;
    return text.replace(/[A-Za-z]/g, function (c) { return shiftChar(c, (sign || 1) * (key.charCodeAt(j++ % key.length) - 65)); });
  }
  function lettersOnly(text) {
    return text.normalize("NFD").replace(/[̀-ͯ]/g, "").toUpperCase().replace(/[^A-Z]/g, "");
  }
  function counts(text) {
    var c = new Array(26).fill(0), s = lettersOnly(text);
    for (var i = 0; i < s.length; i++) c[s.charCodeAt(i) - 65]++;
    return c;
  }
  // Letter frequencies (%) of typical English and Spanish text (Spanish folds Ñ into N and accents away).
  var FREQ = {
    en: [8.17, 1.49, 2.78, 4.25, 12.70, 2.23, 2.02, 6.09, 6.97, 0.15, 0.77, 4.03, 2.41, 6.75, 7.51, 1.93, 0.10, 5.99,
      6.33, 9.06, 2.76, 0.98, 2.36, 0.15, 1.97, 0.07],
    es: [12.53, 1.42, 4.68, 5.86, 13.68, 0.69, 1.01, 0.70, 6.25, 0.44, 0.02, 4.97, 3.15, 6.71, 8.68, 2.51, 0.88, 6.87,
      7.98, 4.63, 3.93, 0.90, 0.01, 0.22, 0.90, 0.52],
  };
  // χ² distance between observed counts and a language's frequencies: small means "looks like that language".
  function chi2(c, lang) {
    var n = c.reduce(function (a, b) { return a + b; }, 0), f = FREQ[lang] || FREQ.en, s = 0;
    if (!n) return Infinity;
    for (var i = 0; i < 26; i++) { var e = n * f[i] / 100; s += (c[i] - e) * (c[i] - e) / e; }
    return s;
  }
  // Index of coincidence: the probability that two letters drawn from the text are equal (≈0.066 English, ≈0.077
  // Spanish, 1/26 ≈ 0.038 for uniformly random letters).
  function ic(text) {
    var c = counts(text), n = c.reduce(function (a, b) { return a + b; }, 0), s = 0;
    if (n < 2) return 0;
    for (var i = 0; i < 26; i++) s += c[i] * (c[i] - 1);
    return s / (n * (n - 1));
  }

  // ------------------------------------------------------------------ Enigma I (same wiring as the desktop app)

  var ROTORS = ["EKMFLGDQVZNTOWYHXUSPAIBRCJ", "AJDKSIRUXBLHWTMCQGZNPYFVOE", "BDFHJLCPRTXVZNYEIWGAKMUSQO",
    "ESOVPZJAYQUIRHXLNFTGKDCMWB", "VZBRGITYUPSDNHLXAWMJQOFECK"];
  var NOTCH = "QEVJZ";
  var ROTOR_NAMES = ["I", "II", "III", "IV", "V"];
  var REFLECTORS = { B: "YRUHQSLDPXNGOKMIEBFZCWVJAT", C: "FVPJIAOYEDRZXWGCTKUQSBNMHL" };

  function Enigma(o) {
    o = o || {};
    this.rotor = (o.rotors || [0, 1, 2]).slice();
    this.ring = (o.rings || [0, 0, 0]).slice();
    this.pos = (o.positions || [0, 0, 0]).slice();
    this.reflector = REFLECTORS[o.reflector || "B"];
    this.plug = [];
    for (var i = 0; i < 26; i++) this.plug[i] = i;
    var self = this;
    (o.plugboard || "").toUpperCase().split(/[\s,]+/).forEach(function (p) {
      if (p.length !== 2) return;
      var a = p.charCodeAt(0) - 65, b = p.charCodeAt(1) - 65;
      if (a < 0 || a > 25 || b < 0 || b > 25 || a === b || self.plug[a] !== a || self.plug[b] !== b) return;
      self.plug[a] = b; self.plug[b] = a;
    });
  }
  Enigma.prototype.atNotch = function (i) { return this.pos[i] === NOTCH.charCodeAt(this.rotor[i]) - 65; };
  Enigma.prototype.step = function () {
    var middle = this.atNotch(1), right = this.atNotch(2);
    if (middle) this.pos[0] = (this.pos[0] + 1) % 26;
    if (middle || right) this.pos[1] = (this.pos[1] + 1) % 26;
    this.pos[2] = (this.pos[2] + 1) % 26;
  };
  Enigma.prototype.through = function (c, i, back) {
    var w = ROTORS[this.rotor[i]], shift = this.pos[i] - this.ring[i];
    var inp = ((c + shift) % 26 + 26) % 26;
    var out = back ? w.indexOf(ALPHA[inp]) : w.charCodeAt(inp) - 65;
    return ((out - shift) % 26 + 26) % 26;
  };
  // One key press: steps the rotors, returns the lamp and the path of the current (letters at each stage).
  Enigma.prototype.press = function (c) {
    this.step();
    var path = [c];
    c = this.plug[c]; path.push(c);
    for (var i = 2; i >= 0; i--) { c = this.through(c, i, false); path.push(c); }
    c = this.reflector.charCodeAt(c) - 65; path.push(c);
    for (i = 0; i < 3; i++) { c = this.through(c, i, true); path.push(c); }
    c = this.plug[c]; path.push(c);
    return { lamp: c, path: path };
  };
  Enigma.prototype.encrypt = function (text) {
    var self = this;
    return text.toUpperCase().replace(/[A-Z]/g, function (ch) { return ALPHA[self.press(ch.charCodeAt(0) - 65).lamp]; });
  };
  Enigma.prototype.window = function () { return this.pos.map(function (p) { return ALPHA[p]; }).join(""); };

  // ------------------------------------------------------------------ GF(2^8) and AES-128

  // Multiplication in GF(2^8) modulo the AES polynomial x^8 + x^4 + x^3 + x + 1 (0x11b).
  function gmul(a, b) {
    var p = 0;
    for (var i = 0; i < 8; i++) {
      if (b & 1) p ^= a;
      var hi = a & 0x80;
      a = (a << 1) & 0xff;
      if (hi) a ^= 0x1b;
      b >>= 1;
    }
    return p;
  }
  function ginv(a) {                     // a^254 = a^-1 (Fermat in a field of 256 elements); 0 maps to 0
    if (!a) return 0;
    var r = 1, x = a, e = 254;
    while (e) { if (e & 1) r = gmul(r, x); x = gmul(x, x); e >>= 1; }
    return r;
  }
  // The S-box is not a table of magic numbers: inversion in GF(2^8) followed by a fixed affine map.
  var SBOX = new Uint8Array(256), INV_SBOX = new Uint8Array(256);
  (function () {
    for (var i = 0; i < 256; i++) {
      var x = ginv(i), s = x;
      for (var k = 1; k <= 4; k++) s ^= ((x << k) | (x >> (8 - k))) & 0xff;
      s ^= 0x63;
      SBOX[i] = s;
      INV_SBOX[s] = i;
    }
  })();

  function expandKey(key) {               // 16 bytes -> 11 round keys of 16 bytes
    var w = new Uint8Array(176), rcon = 1;
    w.set(key);
    for (var i = 16; i < 176; i += 4) {
      var t = [w[i - 4], w[i - 3], w[i - 2], w[i - 1]];
      if (i % 16 === 0) {
        t = [SBOX[t[1]] ^ rcon, SBOX[t[2]], SBOX[t[3]], SBOX[t[0]]];
        rcon = gmul(rcon, 2);
      }
      for (var j = 0; j < 4; j++) w[i + j] = w[i - 16 + j] ^ t[j];
    }
    var keys = [];
    for (var r = 0; r < 11; r++) keys.push(w.slice(16 * r, 16 * r + 16));
    return keys;
  }
  // The state is 16 bytes in column order: byte (row r, column c) is s[4c + r].
  function subBytes(s) { for (var i = 0; i < 16; i++) s[i] = SBOX[s[i]]; }
  function shiftRows(s) {
    var t = s.slice();
    for (var r = 1; r < 4; r++) for (var c = 0; c < 4; c++) s[4 * c + r] = t[4 * ((c + r) % 4) + r];
  }
  function mixColumns(s) {
    for (var c = 0; c < 4; c++) {
      var a = s.slice(4 * c, 4 * c + 4);
      s[4 * c] = gmul(a[0], 2) ^ gmul(a[1], 3) ^ a[2] ^ a[3];
      s[4 * c + 1] = a[0] ^ gmul(a[1], 2) ^ gmul(a[2], 3) ^ a[3];
      s[4 * c + 2] = a[0] ^ a[1] ^ gmul(a[2], 2) ^ gmul(a[3], 3);
      s[4 * c + 3] = gmul(a[0], 3) ^ a[1] ^ a[2] ^ gmul(a[3], 2);
    }
  }
  function addRoundKey(s, k) { for (var i = 0; i < 16; i++) s[i] ^= k[i]; }

  // Encrypts one block. With trace = true it also returns the state after every step of every round.
  function aesEncryptBlock(keys, block, trace) {
    var s = Uint8Array.from(block), steps = [];
    function rec(round, op) { if (trace) steps.push({ round: round, op: op, state: s.slice() }); }
    rec(0, "input");
    addRoundKey(s, keys[0]); rec(0, "addRoundKey");
    for (var r = 1; r <= 10; r++) {
      subBytes(s); rec(r, "subBytes");
      shiftRows(s); rec(r, "shiftRows");
      if (r < 10) { mixColumns(s); rec(r, "mixColumns"); }
      addRoundKey(s, keys[r]); rec(r, "addRoundKey");
    }
    return trace ? { out: s, steps: steps } : s;
  }
  function aes128(key) {
    var keys = expandKey(key);
    return {
      keys: keys,
      encryptBlock: function (b, trace) { return aesEncryptBlock(keys, b, trace); },
    };
  }
  // Modes on whole blocks (the figures use data that is already a multiple of 16 bytes).
  function ecb(cipher, data) {
    var out = new Uint8Array(data.length);
    for (var i = 0; i < data.length; i += 16) out.set(cipher.encryptBlock(data.subarray(i, i + 16)), i);
    return out;
  }
  function cbc(cipher, data, iv) {
    var out = new Uint8Array(data.length), prev = iv;
    for (var i = 0; i < data.length; i += 16) {
      prev = cipher.encryptBlock(xor(data.subarray(i, i + 16), prev));
      out.set(prev, i);
    }
    return out;
  }
  function ctr(cipher, data, nonce) {
    var out = new Uint8Array(data.length), counter = Uint8Array.from(nonce);
    for (var i = 0; i < data.length; i += 16) {
      var ks = cipher.encryptBlock(counter);
      for (var j = 0; j < 16 && i + j < data.length; j++) out[i + j] = data[i + j] ^ ks[j];
      for (var k = 15; k >= 0; k--) { counter[k] = (counter[k] + 1) & 0xff; if (counter[k]) break; }
    }
    return out;
  }

  // ------------------------------------------------------------------ SHA-256 (synchronous, for tight loops)

  var K256 = new Uint32Array([
    0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5, 0xd807aa98,
    0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174, 0xe49b69c1, 0xefbe4786,
    0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, 0x983e5152, 0xa831c66d, 0xb00327c8,
    0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
    0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85, 0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819,
    0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a,
    0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7,
    0xc67178f2]);
  function sha256(msg) {
    if (typeof msg === "string") msg = utf8(msg);
    var len = msg.length, n = ((len + 9 + 63) >> 6) << 6, m = new Uint8Array(n);
    m.set(msg);
    m[len] = 0x80;
    var bits = len * 8;
    for (var i = 0; i < 4; i++) m[n - 1 - i] = (bits >>> (8 * i)) & 0xff;
    m[n - 5] = Math.floor(len / 0x20000000) & 0xff;
    var h = new Uint32Array([0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19]);
    var w = new Uint32Array(64);
    for (var off = 0; off < n; off += 64) {
      for (var t = 0; t < 16; t++) {
        w[t] = (m[off + 4 * t] << 24) | (m[off + 4 * t + 1] << 16) | (m[off + 4 * t + 2] << 8) | m[off + 4 * t + 3];
      }
      for (t = 16; t < 64; t++) {
        var x = w[t - 15], y = w[t - 2];
        var s0 = ((x >>> 7) | (x << 25)) ^ ((x >>> 18) | (x << 14)) ^ (x >>> 3);
        var s1 = ((y >>> 17) | (y << 15)) ^ ((y >>> 19) | (y << 13)) ^ (y >>> 10);
        w[t] = (w[t - 16] + s0 + w[t - 7] + s1) | 0;
      }
      var a = h[0], b = h[1], c = h[2], d = h[3], e = h[4], f = h[5], g = h[6], hh = h[7];
      for (t = 0; t < 64; t++) {
        var S1 = ((e >>> 6) | (e << 26)) ^ ((e >>> 11) | (e << 21)) ^ ((e >>> 25) | (e << 7));
        var ch = (e & f) ^ (~e & g);
        var t1 = (hh + S1 + ch + K256[t] + w[t]) | 0;
        var S0 = ((a >>> 2) | (a << 30)) ^ ((a >>> 13) | (a << 19)) ^ ((a >>> 22) | (a << 10));
        var maj = (a & b) ^ (a & c) ^ (b & c);
        var t2 = (S0 + maj) | 0;
        hh = g; g = f; f = e; e = (d + t1) | 0; d = c; c = b; b = a; a = (t1 + t2) | 0;
      }
      h[0] += a; h[1] += b; h[2] += c; h[3] += d; h[4] += e; h[5] += f; h[6] += g; h[7] += hh;
    }
    var out = new Uint8Array(32);
    for (i = 0; i < 8; i++) { out[4 * i] = h[i] >>> 24; out[4 * i + 1] = h[i] >>> 16; out[4 * i + 2] = h[i] >>> 8; out[4 * i + 3] = h[i]; }
    return out;
  }

  // ------------------------------------------------------------------ number theory (BigInt)

  function modpow(b, e, m) {
    b = BigInt(b) % BigInt(m); e = BigInt(e); m = BigInt(m);
    var r = 1n;
    if (b < 0n) b += m;
    while (e > 0n) { if (e & 1n) r = r * b % m; b = b * b % m; e >>= 1n; }
    return r;
  }
  // Square-and-multiply with its trace, for the figure that shows it.
  function modpowTrace(b, e, m) {
    b = BigInt(b); e = BigInt(e); m = BigInt(m);
    var bitsStr = e.toString(2), r = 1n, steps = [];
    for (var i = 0; i < bitsStr.length; i++) {
      r = r * r % m; var sq = r;
      if (bitsStr[i] === "1") r = r * b % m;
      steps.push({ bit: bitsStr[i], square: sq, result: r });
    }
    return { result: r, steps: steps, bits: bitsStr };
  }
  function egcd(a, b) {
    a = BigInt(a); b = BigInt(b);
    var x0 = 1n, y0 = 0n, x1 = 0n, y1 = 1n;
    while (b !== 0n) {
      var q = a / b, t = a - q * b; a = b; b = t;
      t = x0 - q * x1; x0 = x1; x1 = t;
      t = y0 - q * y1; y0 = y1; y1 = t;
    }
    return { g: a, x: x0, y: y0 };
  }
  function gcd(a, b) { return egcd(a, b).g; }
  function modinv(a, m) {
    var r = egcd(a, m);
    if (r.g !== 1n && r.g !== -1n) return null;
    m = BigInt(m);
    return ((r.x % m) + m) % m;
  }
  function isPrime(n) {
    n = BigInt(n);
    if (n < 2n) return false;
    var small = [2n, 3n, 5n, 7n, 11n, 13n, 17n, 19n, 23n, 29n, 31n, 37n];
    for (var i = 0; i < small.length; i++) { if (n === small[i]) return true; if (n % small[i] === 0n) return false; }
    var d = n - 1n, s = 0;
    while ((d & 1n) === 0n) { d >>= 1n; s++; }
    outer: for (i = 0; i < small.length; i++) {          // deterministic for n < 3.3e24
      var x = modpow(small[i], d, n);
      if (x === 1n || x === n - 1n) continue;
      for (var r = 1; r < s; r++) { x = x * x % n; if (x === n - 1n) continue outer; }
      return false;
    }
    return true;
  }
  function primesBetween(lo, hi) {
    var out = [];
    for (var n = lo; n <= hi; n++) if (isPrime(n)) out.push(n);
    return out;
  }

  // ------------------------------------------------------------------ elliptic curves over F_p (small p)

  // y^2 = x^3 + a x + b over F_p. Points are {x, y} numbers or null for the point at infinity O.
  function Curve(a, b, p) { this.a = a; this.b = b; this.p = p; }
  function mod(n, p) { return ((n % p) + p) % p; }
  function inv(n, p) { var r = egcd(BigInt(mod(n, p)), BigInt(p)); return Number(mod(Number(r.x), p)); }
  Curve.prototype.on = function (P) {
    return P === null || mod(P.y * P.y - (P.x * P.x * P.x + this.a * P.x + this.b), this.p) === 0;
  };
  Curve.prototype.add = function (P, Q) {
    var p = this.p, l;
    if (P === null) return Q;
    if (Q === null) return P;
    if (P.x === Q.x && mod(P.y + Q.y, p) === 0) return null;
    if (P.x === Q.x) l = mod((3 * P.x * P.x + this.a) * inv(2 * P.y, p), p);
    else l = mod((Q.y - P.y) * inv(Q.x - P.x, p), p);
    var x = mod(l * l - P.x - Q.x, p);
    return { x: x, y: mod(l * (P.x - x) - P.y, p) };
  };
  Curve.prototype.mul = function (k, P) {
    var R = null;
    for (var i = 0; i < k; i++) R = this.add(R, P);
    return R;
  };
  Curve.prototype.points = function () {
    var pts = [];
    for (var x = 0; x < this.p; x++) for (var y = 0; y < this.p; y++) if (this.on({ x: x, y: y })) pts.push({ x: x, y: y });
    return pts;
  };
  Curve.prototype.order = function (P) {
    var R = P, n = 1;
    while (R !== null) { R = this.add(R, P); n++; if (n > 4 * this.p) return -1; }
    return n;
  };

  // ------------------------------------------------------------------ learning with errors (toy Regev scheme)

  // Keys: secret s in Z_q^n; public (A, b = A s + e) with m rows and small errors e in [-noise, noise].
  function lweKeygen(n, m, q, noise, rand) {
    rand = rand || Math.random;
    var s = [], A = [], b = [];
    for (var i = 0; i < n; i++) s.push(Math.floor(rand() * q));
    for (var r = 0; r < m; r++) {
      var row = [], dot = 0;
      for (i = 0; i < n; i++) { var x = Math.floor(rand() * q); row.push(x); dot += x * s[i]; }
      var e = Math.round((rand() * 2 - 1) * noise);
      A.push(row);
      b.push(mod(dot + e, q));
    }
    return { n: n, m: m, q: q, s: s, A: A, b: b };
  }
  // Encrypts one bit: sum a random subset of rows; add q/2 if the bit is 1.
  function lweEncrypt(k, bit, rand) {
    rand = rand || Math.random;
    var u = new Array(k.n).fill(0), v = 0, rows = [];
    for (var r = 0; r < k.m; r++) {
      if (rand() < 0.5) continue;
      rows.push(r);
      for (var i = 0; i < k.n; i++) u[i] += k.A[r][i];
      v += k.b[r];
    }
    u = u.map(function (x) { return mod(x, k.q); });
    return { u: u, v: mod(v + (bit ? Math.floor(k.q / 2) : 0), k.q), rows: rows };
  }
  // Decrypts: v - <u, s> is near 0 for a 0 and near q/2 for a 1, off by the sum of the errors used.
  function lweDecrypt(k, c) {
    var dot = 0;
    for (var i = 0; i < k.n; i++) dot += c.u[i] * k.s[i];
    var d = mod(c.v - dot, k.q);
    var dist = Math.min(d, k.q - d);
    return { bit: dist > k.q / 4 ? 1 : 0, d: d };
  }

  return {
    utf8: utf8, fromUtf8: fromUtf8, hex: hex, unhex: unhex, xor: xor, concat: concat, randomBytes: randomBytes,
    base64: base64, bitDiff: bitDiff, popcount8: popcount8, rng: rng,
    ALPHA: ALPHA, isLetter: isLetter, caesar: caesar, vigenere: vigenere, lettersOnly: lettersOnly, counts: counts,
    FREQ: FREQ, chi2: chi2, ic: ic,
    Enigma: Enigma, ROTOR_NAMES: ROTOR_NAMES, ROTORS: ROTORS, NOTCH: NOTCH, REFLECTORS: REFLECTORS,
    gmul: gmul, ginv: ginv, SBOX: SBOX, INV_SBOX: INV_SBOX, aes128: aes128, ecb: ecb, cbc: cbc, ctr: ctr,
    subBytes: subBytes, shiftRows: shiftRows, mixColumns: mixColumns,
    sha256: sha256,
    modpow: modpow, modpowTrace: modpowTrace, egcd: egcd, gcd: gcd, modinv: modinv, isPrime: isPrime,
    primesBetween: primesBetween,
    Curve: Curve, mod: mod,
    lweKeygen: lweKeygen, lweEncrypt: lweEncrypt, lweDecrypt: lweDecrypt,
  };
});
