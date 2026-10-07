// Chapter 01 — classical ciphers: Caesar and its brute force, frequency analysis, and breaking Vigenère with the
// index of coincidence (key length) plus a χ² fit per column (key letters).
(function () {
  "use strict";
  var M = window.CK, K = window.CKL, h = M.h, s = M.s, t = M.t;

  function lang() { return M.lang() === "es" ? "es" : "en"; }

  // ---------------------------------------------------------------- Caesar: encrypt, then try all 25 keys

  M.register("classical/caesar", function (stage) {
    var input = M.textInput({ label: t("cl.plain"), value: t("cl.sample"), rows: 2, onInput: update });
    var shift = M.slider({ label: t("cl.shift"), min: 0, max: 25, step: 1, value: 3, onInput: update,
      format: function (v) { return String(v); } });
    var out = h("p", { class: "cipher-out" });
    var table = h("ol", { class: "brute" });
    stage.appendChild(M.controls(input.el, shift.el));
    stage.appendChild(h("p", { class: "panel-label" }, t("cl.cipher")));
    stage.appendChild(out);
    stage.appendChild(h("p", { class: "panel-label" }, t("cl.brute")));
    stage.appendChild(table);

    function update() {
      var c = K.caesar(input.get(), shift.get());
      out.textContent = c;
      var cands = [];
      for (var k = 1; k <= 25; k++) {
        var p = K.caesar(c, -k);
        cands.push({ k: k, p: p, score: K.chi2(K.counts(p), lang()) });
      }
      var best = cands.reduce(function (a, b) { return b.score < a.score ? b : a; });
      M.clear(table);
      cands.forEach(function (x) {
        table.appendChild(h("li", { class: x === best ? "best" : null },
          h("span", { class: "k" }, "−" + x.k), h("span", { class: "p" }, x.p.slice(0, 60)),
          x === best ? h("span", { class: "tag" }, t("cl.best")) : null));
      });
    }
    update();
  });

  // ---------------------------------------------------------------- frequency analysis: slide the histogram

  M.register("classical/frequency", function (stage) {
    var secret = 7 + Math.floor(Math.random() * 15);
    var cipher = K.caesar(t("cl.long"), secret);
    var c = K.counts(cipher), n = c.reduce(function (a, b) { return a + b; }, 0);
    var shift = M.slider({ label: t("cl.try"), min: 0, max: 25, step: 1, value: 0, onInput: draw,
      format: function (v) { return String(v); } });
    var chi = M.stat("χ²"), verdict = h("p", { class: "freq-plain" });
    var svg = M.svgBox(stage, 640, 230, t("cl.freqLabel"));
    stage.appendChild(M.controls(shift.el, chi.el));
    stage.appendChild(verdict);

    function draw() {
      var k = shift.get();
      M.clear(svg);
      var ref = K.FREQ[lang()], x0 = 30, y0 = 200, bw = 22.5, max = 16;
      [0, 5, 10, 15].forEach(function (v) {
        var y = y0 - v / max * 180;
        svg.appendChild(s("line", { class: "gridline", x1: x0, x2: x0 + 26 * bw, y1: y, y2: y }));
        svg.appendChild(s("text", { x: x0 - 6, y: y + 4, "text-anchor": "end" }, v + "%"));
      });
      for (var i = 0; i < 26; i++) {
        var obs = 100 * c[(i + k) % 26] / n;
        var x = x0 + i * bw;
        svg.appendChild(s("rect", { class: "bar-soft", x: x + 2, y: y0 - ref[i] / max * 180, width: bw - 4, height: ref[i] / max * 180 }));
        svg.appendChild(s("rect", { class: "bar", x: x + 6, y: y0 - obs / max * 180, width: bw - 12, height: obs / max * 180 }));
        svg.appendChild(s("text", { x: x + bw / 2, y: y0 + 16, "text-anchor": "middle", class: "mono" }, K.ALPHA[i]));
      }
      var plain = K.caesar(cipher, -k);
      chi.set(M.fmt(K.chi2(K.counts(plain), lang()), 0));
      verdict.textContent = plain.slice(0, 120) + "…";
      verdict.classList.toggle("solved", k === secret);
    }
    draw();
  });

  // ---------------------------------------------------------------- Vigenère: find the key length, then each letter

  M.register("classical/vigenere", function (stage) {
    var input = M.textInput({ label: t("cl.plain"), value: t("cl.long"), rows: 3, onInput: update });
    var key = M.textInput({ label: t("cl.keyword"), value: "CRYPTOKIT", maxlength: 16, onInput: update });
    var out = h("p", { class: "cipher-out small" });
    var found = h("p", { class: "recovered" });
    stage.appendChild(M.controls(input.el, key.el));
    stage.appendChild(h("p", { class: "panel-label" }, t("cl.cipher")));
    stage.appendChild(out);
    stage.appendChild(h("p", { class: "panel-label" }, t("cl.icTitle")));
    var svg = M.svgBox(stage, 640, 170, t("cl.icLabel"));
    stage.appendChild(found);

    function columns(letters, L) {
      var cols = [];
      for (var j = 0; j < L; j++) {
        var col = "";
        for (var i = j; i < letters.length; i += L) col += letters[i];
        cols.push(col);
      }
      return cols;
    }

    function update() {
      var kw = key.get().toUpperCase().replace(/[^A-Z]/g, "") || "A";
      var c = K.vigenere(input.get(), kw);
      out.textContent = c;
      var letters = K.lettersOnly(c);
      // Average index of coincidence of the columns, for each candidate key length.
      var ics = [];
      for (var L = 1; L <= 16; L++) {
        ics.push(columns(letters, L).reduce(function (a, col) { return a + K.ic(col); }, 0) / L);
      }
      var target = lang() === "es" ? 0.072 : 0.066, threshold = (target + 1 / 26) / 2 + 0.006, guess = 1;
      for (L = 1; L <= 16; L++) { if (ics[L - 1] > threshold) { guess = L; break; } }
      M.clear(svg);
      var x0 = 34, y0 = 140, bw = 36, top = 0.085;
      [0.04, 0.06, 0.08].forEach(function (v) {
        var y = y0 - v / top * 125;
        svg.appendChild(s("line", { class: "gridline", x1: x0, x2: x0 + 16 * bw, y1: y, y2: y }));
        svg.appendChild(s("text", { x: x0 - 6, y: y + 4, "text-anchor": "end" }, v.toFixed(2)));
      });
      var ty = y0 - target / top * 125;
      svg.appendChild(s("line", { class: "curve-muted", x1: x0, x2: x0 + 16 * bw, y1: ty, y2: ty }));
      ics.forEach(function (v, i) {
        var x = x0 + i * bw;
        svg.appendChild(s("rect", { class: i + 1 === guess ? "bar" : "bar-soft", x: x + 5, y: y0 - v / top * 125,
          width: bw - 10, height: v / top * 125 }));
        svg.appendChild(s("text", { x: x + bw / 2, y: y0 + 16, "text-anchor": "middle" }, String(i + 1)));
      });
      svg.appendChild(s("text", { x: x0 + 16 * bw, y: ty - 6, "text-anchor": "end" }, t("cl.icAxis")));
      // Each column is a Caesar cipher: the shift with the best χ² is that key letter.
      var rec = columns(letters, guess).map(function (col) {
        var best = 0, bestScore = Infinity;
        for (var k = 0; k < 26; k++) {
          var sc = K.chi2(K.counts(K.caesar(col, -k)), lang());
          if (sc < bestScore) { bestScore = sc; best = k; }
        }
        return K.ALPHA[best];
      }).join("");
      found.textContent = t("cl.recovered", { n: guess, key: rec });
      found.classList.toggle("solved", rec === kw);
    }
    update();
  });
})();
