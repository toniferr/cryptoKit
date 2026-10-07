// Chapter 06 — hash functions: SHA-256's avalanche (edit one character, half the bits flip) and the birthday
// paradox, finding real collisions in SHA-256 cut down to k bits after about 2^(k/2) tries.
(function () {
  "use strict";
  var M = window.CK, K = window.CKL, h = M.h, s = M.s, t = M.t;

  M.register("hashing/avalanche", function (stage) {
    var input = M.textInput({ label: t("hs.message"), value: t("hs.sample"), onInput: update });
    var hexOut = h("p", { class: "cipher-out mono small" });
    var grid = h("div", { class: "bitgrid wide" });
    var flipped = M.stat(t("hs.flipped"), "accent");
    stage.appendChild(M.controls(input.el));
    stage.appendChild(hexOut);
    stage.appendChild(grid);
    stage.appendChild(h("div", { class: "stats" }, flipped.el));
    var prev = K.sha256(input.get());
    var cells = [];
    for (var i = 0; i < 256; i++) { cells.push(h("span", { class: "bit" })); grid.appendChild(cells[i]); }

    function update() {
      var d = K.sha256(input.get());
      hexOut.textContent = "SHA-256 = " + K.hex(d);
      var changed = 0;
      for (var i = 0; i < 256; i++) {
        var m = 0x80 >> (i & 7), on = (d[i >> 3] & m) !== 0, diff = ((d[i >> 3] ^ prev[i >> 3]) & m) !== 0;
        if (diff) changed++;
        cells[i].className = "bit" + (on ? " on" : "") + (diff ? " flip" : "");
      }
      flipped.set(changed + " / 256");
      prev = d;
    }
    update();
    flipped.set("—");
  });

  M.register("hashing/birthday", function (stage) {
    var bits = M.slider({ label: t("hs.bits"), min: 16, max: 36, step: 4, value: 24, onInput: drawCurve,
      format: function (v) { return v + " bits"; } });
    var go = M.button(t("hs.search"), search, "primary");
    var tries = M.stat(t("hs.tries")), expected = M.stat(t("hs.expected")), result = h("div", { class: "collision" });
    var svg = M.svgBox(stage, 640, 200, t("hs.curveLabel"));
    stage.insertBefore(M.controls(bits.el, go), svg);
    stage.appendChild(h("div", { class: "stats" }, tries.el, expected.el));
    stage.appendChild(result);
    var running = false;

    function drawCurve(found) {
      var k = bits.get(), N = Math.pow(2, k), x0 = 40, y0 = 170, w = 580, maxN = 4 * Math.sqrt(N);
      expected.set("≈ " + M.fmt(Math.sqrt(Math.PI * N / 2), 0));
      M.clear(svg);
      [0, 0.5, 1].forEach(function (v) {
        var y = y0 - v * 150;
        svg.appendChild(s("line", { class: "gridline", x1: x0, x2: x0 + w, y1: y, y2: y }));
        svg.appendChild(s("text", { x: x0 - 6, y: y + 4, "text-anchor": "end" }, (v * 100) + "%"));
      });
      var pts = [];
      for (var i = 0; i <= 100; i++) {
        var n = maxN * i / 100;
        pts.push([x0 + w * i / 100, y0 - 150 * (1 - Math.exp(-n * n / (2 * N)))]);
      }
      svg.appendChild(s("path", { class: "curve", d: M.path(pts) }));
      [1, 2, 3, 4].forEach(function (q) {
        var x = x0 + w * q / 4;
        svg.appendChild(s("text", { x: x, y: y0 + 16, "text-anchor": "middle" }, q + "·2^" + k / 2));
      });
      svg.appendChild(s("text", { x: x0 + w, y: 14, "text-anchor": "end" }, t("hs.curveAxis")));
      if (typeof found === "number") {
        var fx = x0 + w * Math.min(1, found / maxN);
        svg.appendChild(s("line", { class: "marker-theory", x1: fx, x2: fx, y1: y0, y2: 20 }));
        svg.appendChild(s("text", { x: fx + 4, y: 30, class: "label-strong" }, t("hs.here")));
      }
    }

    // Hash "cryptoKit #0", "cryptoKit #1"... and keep the first k bits of each digest until two coincide.
    function search() {
      if (running) return;
      running = true;
      go.disabled = true;
      var k = bits.get(), seen = new Map(), n = 0, prefix = t("hs.prefix") + " #";
      M.clear(result);
      function chunk() {
        var stop = performance.now() + 30;
        while (performance.now() < stop) {
          var d = K.sha256(prefix + n);
          // The first 48 bits as a number (exact below 2^53), then its top k bits.
          var v = ((d[0] * 256 + d[1]) * 256 + d[2]) * 16777216 + (d[3] << 16 | d[4] << 8 | d[5]);
          var key = Math.floor(v / Math.pow(2, 48 - k));
          if (seen.has(key)) {
            var other = seen.get(key);
            running = false;
            go.disabled = false;
            tries.set(M.fmt(n + 1, 0));
            var a = K.hex(K.sha256(prefix + other)), b = K.hex(d), hexDigits = Math.ceil(k / 4);
            result.appendChild(h("p", null, h("code", null, prefix + other), " → ", h("code", { class: "hl" }, a.slice(0, hexDigits)), h("code", null, a.slice(hexDigits, 24) + "…")));
            result.appendChild(h("p", null, h("code", null, prefix + n), " → ", h("code", { class: "hl" }, b.slice(0, hexDigits)), h("code", null, b.slice(hexDigits, 24) + "…")));
            drawCurve(n + 1);
            return;
          }
          seen.set(key, n);
          n++;
        }
        tries.set(M.fmt(n, 0) + "…");
        setTimeout(chunk, 0);
      }
      chunk();
    }
    drawCurve();
    tries.set("—");
  });
})();
