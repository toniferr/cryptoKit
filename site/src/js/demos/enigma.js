// Chapter 02 — Enigma: a working Enigma I (same wiring and stepping as the desktop app) with the path of the
// current drawn through plugboard, rotors and reflector; and the crib attack that exploits "no letter encrypts to
// itself".
(function () {
  "use strict";
  var M = window.CK, K = window.CKL, h = M.h, s = M.s, t = M.t;
  var A = K.ALPHA;

  M.register("enigma/machine", function (stage) {
    var settings = { rotors: [0, 1, 2], reflector: "B", rings: [0, 0, 0], positions: [0, 0, 0], plugboard: "" };
    var machine, last = null, tape = "", input = "";

    function rotorSelect(i) {
      return M.select({ label: t("en.rotor", { n: ["L", "M", "R"][i] }), value: String(settings.rotors[i]),
        options: K.ROTOR_NAMES.map(function (n, k) { return { value: String(k), label: n }; }),
        onChange: function (v) { settings.rotors[i] = +v; reset(); } });
    }
    var sel = [0, 1, 2].map(rotorSelect);
    var refl = M.segmented({ label: t("en.reflector"), value: "B",
      options: [{ value: "B", label: "B" }, { value: "C", label: "C" }],
      onChange: function (v) { settings.reflector = v; reset(); } });
    var rings = M.textInput({ label: t("en.rings"), value: "AAA", maxlength: 3, wide: false, onInput: reset });
    var plugs = M.textInput({ label: t("en.plugboard"), value: "", wide: false, onInput: reset });
    plugs.input.placeholder = "AV BS CG";

    var windows = h("div", { class: "en-windows" });
    var lamps = h("div", { class: "en-board lamps" });
    var keys = h("div", { class: "en-board keys" });
    var tapeIn = h("p", { class: "en-tape in" }), tapeOut = h("p", { class: "en-tape out" });
    var pathBox = h("div", { class: "en-path" });
    var svg = M.svgBox(pathBox, 640, 300, t("en.pathLabel"));
    var resetBtn = M.button(t("en.reset"), function () { reset(); });

    stage.appendChild(M.controls(sel[0].el, sel[1].el, sel[2].el, refl.el));
    stage.appendChild(M.controls(rings.el, plugs.el, resetBtn));
    stage.appendChild(h("div", { class: "en-machine" }, windows, lamps, keys));
    stage.appendChild(h("div", { class: "en-tapes" }, h("span", { class: "panel-label" }, t("en.typed")), tapeIn,
      h("span", { class: "panel-label" }, t("en.lit")), tapeOut));
    stage.appendChild(h("p", { class: "panel-label" }, t("en.path")));
    stage.appendChild(pathBox);

    var ROWS = ["QWERTZUIO", "ASDFGHJK", "PYXCVBNML"];
    var lampEls = {};
    ROWS.forEach(function (row) {
      var lr = h("div", { class: "row" }), kr = h("div", { class: "row" });
      row.split("").forEach(function (ch) {
        lampEls[ch] = h("span", { class: "lamp" }, ch);
        lr.appendChild(lampEls[ch]);
        kr.appendChild(h("button", { type: "button", class: "key", "aria-label": ch, onclick: function () { press(ch); } }, ch));
      });
      lamps.appendChild(lr);
      keys.appendChild(kr);
    });
    // Rotor windows: click a window to turn the rotor by hand, like the operator setting the day's key.
    var winEls = [0, 1, 2].map(function (i) {
      var up = h("button", { type: "button", class: "en-turn", "aria-label": t("en.turn"), onclick: function () { turn(i, 1); } }, "▲");
      var letter = h("span", { class: "en-letter" });
      var down = h("button", { type: "button", class: "en-turn", "aria-label": t("en.turn"), onclick: function () { turn(i, -1); } }, "▼");
      windows.appendChild(h("div", { class: "en-window" }, up, letter, down));
      return letter;
    });

    function parseRings(v) {
      var r = v.toUpperCase().replace(/[^A-Z]/g, "").padEnd(3, "A").slice(0, 3);
      return r.split("").map(function (c) { return c.charCodeAt(0) - 65; });
    }
    function reset() {
      settings.rings = parseRings(rings.get());
      settings.plugboard = plugs.get();
      machine = new K.Enigma({ rotors: settings.rotors, reflector: settings.reflector, rings: settings.rings,
        positions: settings.positions, plugboard: settings.plugboard });
      last = null; tape = ""; input = "";
      render();
    }
    function turn(i, d) {
      settings.positions[i] = (machine.pos[i] + d + 26) % 26;
      machine.pos[i] = settings.positions[i];
      render();
    }
    function press(ch) {
      var r = machine.press(ch.charCodeAt(0) - 65);
      last = { key: ch, lamp: A[r.lamp], path: r.path };
      input += ch; tape += A[r.lamp];
      if (input.length % 5 === 0) { input += " "; tape += " "; }
      render();
    }
    function render() {
      winEls.forEach(function (el, i) { el.textContent = A[machine.pos[i]]; });
      Object.keys(lampEls).forEach(function (ch) { lampEls[ch].classList.toggle("on", !!last && last.lamp === ch); });
      tapeIn.textContent = input || "—";
      tapeOut.textContent = tape || "—";
      drawPath();
    }

    // The path: columns for plugboard, rotors R, M, L and the reflector; the current goes left (in) and comes back.
    function drawPath() {
      M.clear(svg);
      var cols = ["plug", "R", "M", "L", "UKW"], x0 = 40, dx = 135, top = 24, dy = 10.2;
      cols.forEach(function (c, i) {
        var x = x0 + i * dx;
        svg.appendChild(s("text", { x: x, y: 14, "text-anchor": "middle", class: "label-strong" },
          c === "plug" ? t("en.plug") : c === "UKW" ? t("en.refl") : c));
        for (var k = 0; k < 26; k++) {
          svg.appendChild(s("text", { x: x, y: top + k * dy + 3.5, "text-anchor": "middle", class: "mono en-pin" }, A[k]));
        }
      });
      if (!last) {
        svg.appendChild(s("text", { x: 320, y: 160, "text-anchor": "middle" }, t("en.pressHint")));
        return;
      }
      var p = last.path;   // [key, after plug, after R, after M, after L, after reflector, after L, M, R, after plug]
      function pt(col, letter) { return [x0 + col * dx, top + letter * dy]; }
      // Inside each rotor the letter changes; between rotors it travels straight across.
      var f = [pt(0, p[1]), pt(1, p[1]), pt(1, p[2]), pt(2, p[2]), pt(2, p[3]), pt(3, p[3]), pt(3, p[4]), pt(4, p[4])];
      var b = [pt(4, p[5]), pt(3, p[5]), pt(3, p[6]), pt(2, p[6]), pt(2, p[7]), pt(1, p[7]), pt(1, p[8]), pt(0, p[8])];
      svg.appendChild(s("path", { class: "en-wire fwd", d: M.path(f) }));
      svg.appendChild(s("path", { class: "en-wire back", d: M.path(b) }));
      svg.appendChild(s("path", { class: "en-wire refl", d: M.path([pt(4, p[4]), [x0 + 4 * dx + 18, top + p[4] * dy],
        [x0 + 4 * dx + 18, top + p[5] * dy], pt(4, p[5])]) }));
      [[0, p[0], "fwd"], [0, p[9], "back"]].forEach(function (q) {
        var c = pt(q[0], q[1]);
        svg.appendChild(s("circle", { cx: c[0] - 18, cy: c[1], r: 7, class: "en-end " + q[2] }));
      });
      svg.appendChild(s("text", { x: x0 - 30, y: top + p[0] * dy + 4, "text-anchor": "end", class: "label-strong" }, last.key));
      svg.appendChild(s("text", { x: x0 - 30, y: top + p[9] * dy + 4, "text-anchor": "end", class: "label-strong en-litlabel" }, last.lamp));
    }

    // Physical keyboard, while the figure has focus or the pointer is over it.
    var active = false;
    stage.addEventListener("pointerenter", function () { active = true; });
    stage.addEventListener("pointerleave", function () { active = false; });
    document.addEventListener("keydown", function (e) {
      if (!active || e.ctrlKey || e.metaKey || e.altKey || /input|textarea|select/i.test(e.target.tagName)) return;
      var ch = e.key.toUpperCase();
      if (ch.length === 1 && ch >= "A" && ch <= "Z") { e.preventDefault(); press(ch); }
    });
    reset();
  });

  // ---------------------------------------------------------------- crib dragging: where can the crib NOT be?

  M.register("enigma/crib", function (stage) {
    var secret = new K.Enigma({ rotors: [1, 3, 0], positions: [16, 4, 22], plugboard: "AQ EP KR LS" });
    var cipher = secret.encrypt(t("en.cribMessage").toUpperCase().replace(/[^A-Z]/g, ""));
    var crib = M.textInput({ label: t("en.crib"), value: "WETTERBERICHT", maxlength: 24, wide: false, onInput: draw });
    var list = h("div", { class: "crib-rows" });
    var summary = h("p", { class: "recovered" });
    stage.appendChild(M.controls(crib.el));
    stage.appendChild(list);
    stage.appendChild(summary);

    function draw() {
      var c = crib.get().toUpperCase().replace(/[^A-Z]/g, "");
      M.clear(list);
      if (!c) return;
      var possible = 0, total = 0;
      for (var off = 0; off + c.length <= Math.min(cipher.length, 40); off++) {
        total++;
        var clash = false, cells = [];
        for (var i = 0; i < cipher.length && i < 40; i++) {
          var inCrib = i >= off && i < off + c.length;
          var hit = inCrib && c[i - off] === cipher[i];
          if (hit) clash = true;
          cells.push(h("span", { class: "cc" + (inCrib ? " in" : "") + (hit ? " hit" : "") }, inCrib ? c[i - off] : "·"));
        }
        if (!clash) possible++;
        list.appendChild(h("div", { class: "crib-row" + (clash ? " no" : " ok") },
          h("span", { class: "off" }, String(off)), h("span", { class: "cells" }, cells)));
      }
      list.insertBefore(h("div", { class: "crib-row cipher" }, h("span", { class: "off" }, ""),
        h("span", { class: "cells" }, cipher.slice(0, 40).split("").map(function (x) { return h("span", { class: "cc" }, x); }))),
        list.firstChild);
      summary.textContent = t("en.cribSummary", { ok: possible, total: total });
    }
    draw();
  });
})();
