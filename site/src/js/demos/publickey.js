// Chapter 08 — public-key cryptography: Diffie–Hellman with real numbers next to the paint-mixing picture, and toy
// RSA end to end (key generation, encryption, square-and-multiply, decryption, and breaking it by factoring n).
(function () {
  "use strict";
  var M = window.CK, K = window.CKL, h = M.h, s = M.s, t = M.t;

  // ---------------------------------------------------------------- Diffie–Hellman

  function mix(a, b) { return [0, 1, 2].map(function (i) { return Math.round((a[i] + b[i]) / 2); }); }
  function css(c) { return "rgb(" + c.join(",") + ")"; }

  M.register("publickey/dh", function (stage) {
    var PRIMES = [23, 467, 7919, 2147483647];
    var p = M.select({ label: "p", value: "467", options: PRIMES.map(function (x) { return { value: String(x), label: String(x) }; }),
      onChange: update });
    var a = M.slider({ label: t("pk.aliceSecret"), min: 2, max: 400, step: 1, value: 153, onInput: update,
      format: function (v) { return String(v); } });
    var b = M.slider({ label: t("pk.bobSecret"), min: 2, max: 400, step: 1, value: 197, onInput: update,
      format: function (v) { return String(v); } });
    var svg = M.svgBox(stage, 640, 250, t("pk.dhLabel"));
    stage.insertBefore(M.controls(p.el, a.el, b.el), svg);
    var eve = h("p", { class: "demo-hint" });
    stage.appendChild(eve);

    function blob(x, y, col, label, sub) {
      svg.appendChild(s("circle", { cx: x, cy: y, r: 24, fill: css(col), stroke: "currentColor", "stroke-opacity": 0.2 }));
      svg.appendChild(s("text", { x: x, y: y + 42, "text-anchor": "middle", class: "label-strong" }, label));
      if (sub) svg.appendChild(s("text", { x: x, y: y + 56, "text-anchor": "middle", class: "mono" }, sub));
    }
    function update() {
      var P = +p.get(), g = 5, A = K.modpow(g, a.get(), P), B = K.modpow(g, b.get(), P);
      var sA = K.modpow(B, a.get(), P), sB = K.modpow(A, b.get(), P);
      var common = [240, 200, 60], ca = [200, 40, 60], cb = [40, 90, 200];
      var mA = mix(common, ca), mB = mix(common, cb), shared = mix(mix(common, ca), cb);
      M.clear(svg);
      svg.appendChild(s("text", { x: 100, y: 20, "text-anchor": "middle", class: "label-strong" }, t("pk.alice")));
      svg.appendChild(s("text", { x: 540, y: 20, "text-anchor": "middle", class: "label-strong" }, t("pk.bob")));
      svg.appendChild(s("text", { x: 320, y: 20, "text-anchor": "middle" }, t("pk.public")));
      svg.appendChild(s("rect", { x: 230, y: 30, width: 180, height: 210, rx: 10, class: "zone-public" }));
      blob(320, 62, common, "g = 5, p = " + P, null);
      blob(60, 70, ca, "a = " + a.get(), null);
      blob(580, 70, cb, "b = " + b.get(), null);
      blob(270, 150, mA, "A = g^a", String(A));
      blob(370, 150, mB, "B = g^b", String(B));
      blob(100, 170, shared, t("pk.shared"), String(sA));
      blob(540, 170, shared, t("pk.shared"), String(sB));
      eve.textContent = t("pk.eve", { p: P, A: String(A), B: String(B) });
    }
    update();
  });

  // ---------------------------------------------------------------- toy RSA

  M.register("publickey/rsa", function (stage) {
    var primes = K.primesBetween(11, 199).map(Number);
    var opts = primes.map(function (x) { return { value: String(x), label: String(x) }; });
    var p = M.select({ label: "p", value: "61", options: opts, onChange: keygen });
    var q = M.select({ label: "q", value: "53", options: opts, onChange: keygen });
    var e = M.select({ label: "e", value: "17", options: [3, 5, 7, 17, 257, 65537].map(function (x) { return { value: String(x), label: String(x) }; }),
      onChange: keygen });
    var msg = M.textInput({ label: t("pk.message"), value: "HOLA", maxlength: 12, wide: false, onInput: run });
    var keyBox = h("div", { class: "rsa-keys" });
    var table = h("table", { class: "mini-table" });
    var sm = h("p", { class: "demo-hint mono" });
    var crack = M.button(t("pk.factor"), factor);
    var cracked = h("p", { class: "recovered" });
    stage.appendChild(M.controls(p.el, q.el, e.el));
    stage.appendChild(keyBox);
    stage.appendChild(M.controls(msg.el));
    stage.appendChild(table);
    stage.appendChild(sm);
    stage.appendChild(M.controls(crack));
    stage.appendChild(cracked);
    var n, phi, d;

    function keygen() {
      var P = BigInt(p.get()), Q = BigInt(q.get()), E = BigInt(e.get());
      n = P * Q; phi = (P - 1n) * (Q - 1n);
      d = P === Q ? null : K.modinv(E, phi);
      M.clear(keyBox);
      cracked.textContent = "";
      if (P === Q) { keyBox.appendChild(h("p", { class: "bad" }, t("pk.samePrime"))); return; }
      if (d === null) { keyBox.appendChild(h("p", { class: "bad" }, t("pk.badE", { phi: String(phi) }))); M.clear(table); return; }
      keyBox.appendChild(h("p", null, h("span", { class: "chip-k pub" }, t("pk.publicKey")), " n = p·q = " + n + ",  e = " + E));
      keyBox.appendChild(h("p", null, h("span", { class: "chip-k priv" }, t("pk.privateKey")), " φ(n) = (p−1)(q−1) = " + phi +
        ",  d = e⁻¹ mod φ(n) = " + d));
      run();
    }
    function run() {
      if (!d) return;
      var codes = Array.from(msg.get()).map(function (c) { return c.codePointAt(0); });
      M.clear(table);
      table.appendChild(h("thead", null, h("tr", null, h("th", null, t("pk.char")), h("th", { class: "num" }, "m"),
        h("th", { class: "num" }, "c = mᵉ mod n"), h("th", { class: "num" }, "cᵈ mod n"), h("th", null, ""))));
      var tb = h("tbody");
      codes.forEach(function (m) {
        var mm = BigInt(m), c = K.modpow(mm, BigInt(e.get()), n), back = K.modpow(c, d, n);
        var ok = mm < n;
        tb.appendChild(h("tr", { class: ok ? null : "bad" }, h("td", null, String.fromCodePoint(m)), h("td", { class: "num" }, String(m)),
          h("td", { class: "num" }, String(c)), h("td", { class: "num" }, String(back)),
          h("td", null, ok ? String.fromCodePoint(Number(back)) : t("pk.tooBig"))));
      });
      table.appendChild(tb);
      if (codes.length) {
        var tr = K.modpowTrace(BigInt(codes[0]), d, n);
        sm.textContent = t("pk.squareMultiply", { m: codes[0], d: String(d), bits: tr.bits, steps: tr.steps.length,
          mults: tr.bits.replace(/0/g, "").length });
      }
    }
    function factor() {
      var t0 = performance.now(), N = n, f = 2n;
      while (f * f <= N && N % f !== 0n) f++;
      var ms = performance.now() - t0;
      var P2 = f, Q2 = N / f, phi2 = (P2 - 1n) * (Q2 - 1n), d2 = K.modinv(BigInt(e.get()), phi2);
      cracked.textContent = t("pk.cracked", { n: String(N), p: String(P2), q: String(Q2), d: String(d2), ms: M.fmt(ms, 2) });
    }
    keygen();
  });
})();
