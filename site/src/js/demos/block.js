// Chapter 04 — block ciphers: a toy Feistel network (invertible whatever F is), AES-128 step by step on the
// FIPS-197 example, and the avalanche effect round by round.
(function () {
  "use strict";
  var M = window.CK, K = window.CKL, h = M.h, s = M.s, t = M.t;

  function bin8(x) { return ("0000000" + x.toString(2)).slice(-8); }
  function hex2(b) { return (b < 16 ? "0" : "") + b.toString(16); }

  // ---------------------------------------------------------------- Feistel

  // A deliberately lossy round function: only the top four bits of an S-box output survive.
  function F(x, k) { return (K.SBOX[x ^ k] & 0xf0) | ((x * 5 + k) >> 5 & 0x0f); }

  M.register("block/feistel", function (stage) {
    var text = M.textInput({ label: t("bk.twoLetters"), value: "Hi", maxlength: 2, wide: false, onInput: render });
    var key = M.slider({ label: t("bk.key"), min: 0, max: 255, step: 1, value: 0x5a, onInput: render,
      format: function (v) { return "0x" + hex2(v); } });
    var table = h("table", { class: "mini-table feistel" });
    var note = h("p", { class: "demo-hint" });
    stage.appendChild(M.controls(text.el, key.el));
    stage.appendChild(table);
    stage.appendChild(note);

    function keys(k) { var ks = []; for (var i = 0; i < 4; i++) { ks.push(k); k = K.SBOX[k ^ (i + 1)]; } return ks; }

    function render() {
      var b = K.utf8((text.get() + "  ").slice(0, 2));
      var L = b[0], R = b[1], ks = keys(key.get()), rows = [];
      rows.push([t("bk.input"), L, R, ""]);
      for (var i = 0; i < 4; i++) {
        var nl = R, nr = L ^ F(R, ks[i]);
        L = nl; R = nr;
        rows.push([t("bk.round", { r: i + 1 }), L, R, "0x" + hex2(ks[i])]);
      }
      var cL = L, cR = R;
      // Decrypt: the same network with the round keys in reverse order (and the halves swapped).
      for (i = 3; i >= 0; i--) { var pr = L, pl = R ^ F(L, ks[i]); L = pl; R = pr; }
      M.clear(table);
      table.appendChild(h("thead", null, h("tr", null, h("th", null, ""), h("th", null, "L"), h("th", null, "R"),
        h("th", { class: "num" }, t("bk.roundKey")))));
      var tb = h("tbody");
      rows.forEach(function (r) {
        tb.appendChild(h("tr", null, h("td", null, r[0]), h("td", { class: "mono" }, bin8(r[1])),
          h("td", { class: "mono" }, bin8(r[2])), h("td", { class: "num" }, r[3])));
      });
      tb.appendChild(h("tr", { class: "back" }, h("td", null, t("bk.decrypted")), h("td", { class: "mono" }, bin8(L)),
        h("td", { class: "mono" }, bin8(R)), h("td", { class: "num" }, String.fromCharCode(L) + String.fromCharCode(R))));
      table.appendChild(tb);
      // Show that F itself loses information: find two inputs with the same output.
      var seen = {}, a = -1, c = -1;
      for (var x = 0; x < 256 && a < 0; x++) {
        var y = F(x, ks[0]);
        if (seen[y] !== undefined) { a = seen[y]; c = x; } else seen[y] = x;
      }
      note.textContent = t("bk.feistelNote", { a: "0x" + hex2(a), b: "0x" + hex2(c), y: "0x" + hex2(F(a, ks[0])),
        c: hex2(cL) + hex2(cR) });
    }
    render();
  });

  // ---------------------------------------------------------------- AES-128 step by step

  M.register("block/aes", function (stage) {
    var keyHex = "2b7e151628aed2a6abf7158809cf4f3c", ptHex = "3243f6a8885a308d313198a2e0370734";
    var pt = M.textInput({ label: t("bk.ptHex"), value: ptHex, maxlength: 47, mono: true, onInput: recompute });
    var key = M.textInput({ label: t("bk.keyHex"), value: keyHex, maxlength: 47, mono: true, onInput: recompute });
    var step = M.slider({ label: t("bk.step"), min: 0, max: 40, step: 1, value: 0, onInput: render,
      format: function (v) { return v + "/40"; } });
    var prev = M.button("◂", function () { step.set(Math.max(0, step.get() - 1)); });
    var next = M.button("▸", function () { step.set(Math.min(40, step.get() + 1)); });
    var grid = h("div", { class: "aes-grid" });
    var info = h("div", { class: "aes-info" });
    var out = h("p", { class: "cipher-out mono" });
    stage.appendChild(M.controls(pt.el, key.el));
    stage.appendChild(M.controls(prev, step.el, next));
    stage.appendChild(h("div", { class: "aes-layout" }, grid, info));
    stage.appendChild(out);
    var trace, keys;

    function recompute() {
      var p = K.unhex(pt.get()), k = K.unhex(key.get());
      if (p.length !== 16 || k.length !== 16) { out.textContent = t("bk.need16"); return; }
      var aes = K.aes128(k);
      keys = aes.keys;
      trace = aes.encryptBlock(p, true);
      render();
    }
    function render() {
      if (!trace) return;
      var i = step.get(), st = trace.steps[i], before = i > 0 ? trace.steps[i - 1].state : st.state;
      M.clear(grid);
      for (var r = 0; r < 4; r++) {
        for (var c = 0; c < 4; c++) {
          var b = st.state[4 * c + r], changed = b !== before[4 * c + r];
          grid.appendChild(h("span", { class: "aes-cell" + (changed ? " changed" : ""),
            dataset: { hue: String(Math.round(b * 1.41) % 360) } }, hex2(b)));
        }
      }
      grid.querySelectorAll(".aes-cell").forEach(function (el) { el.style.setProperty("--h", el.dataset.hue); });
      M.clear(info);
      info.appendChild(h("p", { class: "aes-op" }, (st.round ? t("bk.roundN", { r: st.round }) + " · " : "") + st.op));
      info.appendChild(h("p", null, t("bk.op." + st.op)));
      if (st.op === "addRoundKey") info.appendChild(h("p", { class: "mono small" }, t("bk.roundKey") + " " + st.round + ": " + K.hex(keys[st.round])));
      out.textContent = i === 40 ? t("bk.cipherIs") + " " + K.hex(trace.out) : t("bk.stateIs") + " " + K.hex(st.state);
    }
    recompute();
  });

  // ---------------------------------------------------------------- avalanche

  M.register("block/avalanche", function (stage) {
    var aes = K.aes128(K.unhex("2b7e151628aed2a6abf7158809cf4f3c"));
    var p = K.unhex("3243f6a8885a308d313198a2e0370734");
    var bit = M.slider({ label: t("bk.flipBit"), min: 0, max: 127, step: 1, value: 0, onInput: draw,
      format: function (v) { return String(v); } });
    var svg = M.svgBox(stage, 640, 220, t("bk.avLabel"));
    var bits = h("div", { class: "bitgrid" });
    stage.appendChild(M.controls(bit.el));
    stage.appendChild(h("p", { class: "panel-label" }, t("bk.avFinal")));
    stage.appendChild(bits);

    function draw() {
      var q = p.slice();
      q[bit.get() >> 3] ^= 0x80 >> (bit.get() & 7);
      var a = aes.encryptBlock(p, true).steps.filter(function (x) { return x.op === "addRoundKey"; });
      var b = aes.encryptBlock(q, true).steps.filter(function (x) { return x.op === "addRoundKey"; });
      var diffs = [K.bitDiff(p, q)].concat(a.map(function (x, i) { return K.bitDiff(x.state, b[i].state); }));
      M.clear(svg);
      var x0 = 40, y0 = 190, bw = 48, top = 128;
      [0, 32, 64, 96, 128].forEach(function (v) {
        var y = y0 - v / top * 170;
        svg.appendChild(s("line", { class: "gridline", x1: x0, x2: x0 + 12 * bw, y1: y, y2: y }));
        svg.appendChild(s("text", { x: x0 - 6, y: y + 4, "text-anchor": "end" }, String(v)));
      });
      var half = y0 - 64 / top * 170;
      svg.appendChild(s("line", { class: "curve-muted", x1: x0, x2: x0 + 12 * bw, y1: half, y2: half }));
      svg.appendChild(s("text", { x: x0 + 12 * bw, y: half - 6, "text-anchor": "end" }, t("bk.half")));
      diffs.forEach(function (d, i) {
        var x = x0 + i * bw;
        svg.appendChild(s("rect", { class: "bar", x: x + 8, y: y0 - d / top * 170, width: bw - 16, height: d / top * 170 }));
        svg.appendChild(s("text", { x: x + bw / 2, y: y0 - d / top * 170 - 5, "text-anchor": "middle", class: "label-strong" }, String(d)));
        svg.appendChild(s("text", { x: x + bw / 2, y: y0 + 16, "text-anchor": "middle" }, i === 0 ? t("bk.in") : "R" + (i - 1)));
      });
      M.clear(bits);
      var fa = a[10].state, fb = b[10].state;
      for (var i = 0; i < 128; i++) {
        var m = 0x80 >> (i & 7), on = ((fa[i >> 3] ^ fb[i >> 3]) & m) !== 0;
        bits.appendChild(h("span", { class: "bit" + (on ? " on" : "") }));
      }
    }
    draw();
  });
})();
