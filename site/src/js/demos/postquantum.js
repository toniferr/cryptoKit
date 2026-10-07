// Chapter 10 — post-quantum: a 2-D lattice with a good and a bad basis (Babai's rounding finds the closest point
// with one and misses with the other), and a toy learning-with-errors cipher whose noise you can turn up.
(function () {
  "use strict";
  var M = window.CK, K = window.CKL, h = M.h, s = M.s, t = M.t;

  M.register("postquantum/lattice", function (stage) {
    var good = [[5, 1], [1, 4]];
    // The same lattice: [b1; b2] = U · [g1; g2] with U = [[5, 3], [3, 2]], determinant 1.
    var bad = [[5 * 5 + 3 * 1, 5 * 1 + 3 * 4], [3 * 5 + 2 * 1, 3 * 1 + 2 * 4]];
    var which = M.segmented({ label: t("pq.basis"), value: "good",
      options: [{ value: "good", label: t("pq.good") }, { value: "bad", label: t("pq.bad") }], onChange: draw });
    var svg = M.svgBox(stage, 600, 420, t("pq.latticeLabel"));
    var readout = h("p", { class: "demo-hint" });
    stage.insertBefore(M.controls(which.el), svg);
    stage.appendChild(readout);
    var target = [13.3, 9.6], S = 12, OX = 300, OY = 210;
    function sx(x) { return OX + x * S; }
    function sy(y) { return OY - y * S; }

    function babai(B, p) {
      var det = B[0][0] * B[1][1] - B[1][0] * B[0][1];
      // p = c1 b1 + c2 b2  =>  solve the 2×2 system, then round the coordinates.
      var c1 = (p[0] * B[1][1] - p[1] * B[1][0]) / det, c2 = (B[0][0] * p[1] - B[0][1] * p[0]) / det;
      var r1 = Math.round(c1), r2 = Math.round(c2);
      return [r1 * B[0][0] + r2 * B[1][0], r1 * B[0][1] + r2 * B[1][1]];
    }
    function closest(p) {
      var best = null, bd = Infinity;
      for (var i = -12; i <= 12; i++) for (var j = -12; j <= 12; j++) {
        var q = [i * good[0][0] + j * good[1][0], i * good[0][1] + j * good[1][1]], d = Math.hypot(q[0] - p[0], q[1] - p[1]);
        if (d < bd) { bd = d; best = q; }
      }
      return best;
    }
    function draw() {
      var B = which.get() === "good" ? good : bad;
      M.clear(svg);
      for (var i = -14; i <= 14; i++) for (var j = -14; j <= 14; j++) {
        var x = i * good[0][0] + j * good[1][0], y = i * good[0][1] + j * good[1][1];
        if (sx(x) < 0 || sx(x) > 600 || sy(y) < 0 || sy(y) > 420) continue;
        svg.appendChild(s("circle", { cx: sx(x), cy: sy(y), r: 2.4, class: "pt-faint" }));
      }
      // The fundamental parallelogram of the chosen basis.
      var c = [[0, 0], B[0], [B[0][0] + B[1][0], B[0][1] + B[1][1]], B[1]];
      svg.appendChild(s("path", { class: "cell", d: M.path(c.map(function (p) { return [sx(p[0]), sy(p[1])]; })) + "Z" }));
      B.forEach(function (v, k) {
        svg.appendChild(s("line", { class: "basis", x1: sx(0), y1: sy(0), x2: sx(v[0]), y2: sy(v[1]) }));
        svg.appendChild(s("text", { x: sx(v[0]) + 6, y: sy(v[1]) - 6, class: "label-strong" }, "b" + (k + 1)));
      });
      var guess = babai(B, target), best = closest(target);
      svg.appendChild(s("circle", { cx: sx(best[0]), cy: sy(best[1]), r: 8, class: "pt-ring" }));
      svg.appendChild(s("line", { class: "curve-muted", x1: sx(target[0]), y1: sy(target[1]), x2: sx(guess[0]), y2: sy(guess[1]) }));
      svg.appendChild(s("circle", { cx: sx(guess[0]), cy: sy(guess[1]), r: 6, class: guess[0] === best[0] && guess[1] === best[1] ? "pt-star" : "pt-dot-2" }));
      svg.appendChild(s("circle", { cx: sx(target[0]), cy: sy(target[1]), r: 6, class: "pt-dot" }));
      svg.appendChild(s("text", { x: sx(target[0]) + 9, y: sy(target[1]) + 16, class: "label-strong" }, t("pq.target")));
      var ok = guess[0] === best[0] && guess[1] === best[1];
      readout.textContent = t(ok ? "pq.found" : "pq.missed", {
        d: M.fmt(Math.hypot(guess[0] - target[0], guess[1] - target[1]), 2),
        best: M.fmt(Math.hypot(best[0] - target[0], best[1] - target[1]), 2) });
    }
    svg.addEventListener("pointerdown", function (e) {
      var r = svg.getBoundingClientRect(), k = 600 / r.width;
      target = [((e.clientX - r.left) * k - OX) / S, (OY - (e.clientY - r.top) * k) / S];
      draw();
    });
    draw();
  });

  M.register("postquantum/lwe", function (stage) {
    var msg = M.textInput({ label: t("pq.message"), value: "PQ!", maxlength: 8, wide: false, onInput: run });
    var noise = M.slider({ label: t("pq.noise"), min: 0, max: 60, step: 1, value: 3, onInput: run, format: function (v) { return "±" + v; } });
    var q = 401, n = 8, m = 24;
    var svg = M.svgBox(stage, 600, 300, t("pq.lweLabel"));
    var out = h("p", { class: "recovered" });
    stage.insertBefore(M.controls(msg.el, noise.el), svg);
    stage.appendChild(out);
    var rand = K.rng(2024);

    function run() {
      var key = K.lweKeygen(n, m, q, noise.get(), rand);
      var bytes = K.utf8(msg.get()), bitsIn = [], dec = [], errs = 0;
      for (var i = 0; i < bytes.length; i++) for (var b = 7; b >= 0; b--) bitsIn.push((bytes[i] >> b) & 1);
      M.clear(svg);
      var cx = 150, cy = 150, R = 115;
      svg.appendChild(s("circle", { cx: cx, cy: cy, r: R, class: "ring" }));
      // Values near 0 decode to 0, values near q/2 decode to 1.
      svg.appendChild(s("path", { class: "zone0", d: arc(cx, cy, R, -Math.PI / 2, Math.PI / 2) }));
      svg.appendChild(s("path", { class: "zone1", d: arc(cx, cy, R, Math.PI / 2, 3 * Math.PI / 2) }));
      svg.appendChild(s("text", { x: cx + R + 10, y: cy + 4 }, "0"));
      svg.appendChild(s("text", { x: cx - R - 10, y: cy + 4, "text-anchor": "end" }, "q/2"));
      bitsIn.forEach(function (bit, j) {
        var c = K.lweEncrypt(key, bit, rand), r = K.lweDecrypt(key, c);
        dec.push(r.bit);
        if (r.bit !== bit) errs++;
        var ang = r.d / q * 2 * Math.PI, rr = R - 10 - (j % 4) * 6;
        svg.appendChild(s("circle", { cx: cx + rr * Math.cos(ang), cy: cy - rr * Math.sin(ang), r: 4,
          class: (bit ? "pt-b" : "pt-a") + (r.bit !== bit ? " wrong" : "") }));
      });
      var outBytes = new Uint8Array(bytes.length);
      for (i = 0; i < dec.length; i++) outBytes[i >> 3] |= dec[i] << (7 - (i & 7));
      var lines = [t("pq.params", { n: n, m: m, q: q }), t("pq.decrypted", { text: Array.from(outBytes).map(function (x) {
        return x >= 32 && x < 127 ? String.fromCharCode(x) : "·"; }).join("") }), t("pq.errors", { e: errs, n: bitsIn.length })];
      lines.forEach(function (l, k) { svg.appendChild(s("text", { x: 310, y: 90 + k * 26, class: k ? "label-strong" : "" }, l)); });
      svg.appendChild(s("circle", { cx: 318, cy: 190, r: 5, class: "pt-a" }));
      svg.appendChild(s("text", { x: 330, y: 194 }, t("pq.bit0")));
      svg.appendChild(s("circle", { cx: 318, cy: 214, r: 5, class: "pt-b" }));
      svg.appendChild(s("text", { x: 330, y: 218 }, t("pq.bit1")));
      out.textContent = errs ? t("pq.tooNoisy") : t("pq.allGood");
      out.classList.toggle("solved", !errs);
    }
    function arc(cx, cy, r, a0, a1) {
      return "M" + (cx + r * Math.cos(a0)) + "," + (cy - r * Math.sin(a0)) + " A" + r + "," + r + " 0 0 0 " +
        (cx + r * Math.cos(a1)) + "," + (cy - r * Math.sin(a1));
    }
    run();
  });
})();
