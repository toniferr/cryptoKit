// Home — the title screen. One message travels through the history of cryptography in six acts, each one a real
// cipher applied to the same row of letter tiles: Caesar's wheel turns three places; Vigenère's keyword rides above
// the tiles; Enigma's three rotors step and a lamp lights for every letter; the letters become bytes and AES-128
// churns them round by round in its 4×4 state; public-key cryptography sends a padlock across; and a lattice hides
// the secret behind noise. Everything is drawn on one canvas with the real algorithms from cklib.js.
(function () {
  "use strict";
  var M = window.CK, K = window.CKL, h = M.h, t = M.t;

  var ACTS = ["caesar", "vigenere", "enigma", "aes", "publickey", "lattice"];
  var DUR = { caesar: 7.5, vigenere: 8, enigma: 10, aes: 11, publickey: 9, lattice: 9 };
  var AES_KEY = K.unhex("2b7e151628aed2a6abf7158809cf4f3c");

  function clamp(x, lo, hi) { return Math.max(lo, Math.min(hi, x)); }
  function ease(x) { x = clamp(x, 0, 1); return x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2; }
  function lerp(a, b, k) { return a + (b - a) * k; }
  function hex2(b) { return (b < 16 ? "0" : "") + b.toString(16).toUpperCase(); }

  M.register("splash/ages", function (root) {
    var canvas = root.querySelector(".splash-canvas");
    var ctx = canvas.getContext("2d");
    var reduce = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    // HUD: act chips, caption, pause and next.
    var chips = ACTS.map(function (a, i) {
      return h("li", { dataset: { act: a } }, h("span", { class: "n" }, String(i + 1)), t("splash.act." + a));
    });
    var caption = h("p", { class: "splash-caption", "aria-live": "polite" });
    var pauseBtn = h("button", { type: "button", class: "splash-btn" });
    var nextBtn = h("button", { type: "button", class: "splash-btn" }, t("splash.next") + " ▸");
    root.appendChild(h("div", { class: "splash-hud" },
      h("ol", { class: "splash-acts", "aria-label": t("splash.actsLabel") }, chips),
      caption, h("div", { class: "splash-ctrls" }, pauseBtn, nextBtn)));
    canvas.setAttribute("role", "img");
    canvas.setAttribute("aria-label", t("splash.label"));

    var message = t("splash.message");          // e.g. "ATTACK AT DAWN"
    var plain = message.toUpperCase();
    var act = 0, at = 0, paused = false, running = false, visible = true, W = 0, H = 0, dpr = 1;
    var colors = {};
    var data = {};                               // per-act precomputed results

    function readColors() {
      var cs = getComputedStyle(root);
      ["--bg", "--fg", "--text", "--muted", "--faint", "--rule", "--accent", "--accent-strong", "--accent-soft",
        "--k1", "--k2", "--k3", "--k4", "--bg-soft"].forEach(function (v) { colors[v] = cs.getPropertyValue(v).trim(); });
    }

    // ---------------------------------------------------------------- the real ciphers behind every act

    function prepare() {
      data.caesar = K.caesar(plain, 3);
      data.vigenere = K.vigenere(plain, "LEMON");
      var m = new K.Enigma({ positions: [0, 3, 20], plugboard: "AV BS CG DL" });
      data.enigma = { steps: [] };
      for (var i = 0; i < plain.length; i++) {
        var ch = plain.charCodeAt(i) - 65;
        if (ch < 0 || ch > 25) { data.enigma.steps.push(null); continue; }
        var r = m.press(ch);
        data.enigma.steps.push({ lamp: r.lamp, pos: m.pos.slice() });
      }
      data.enigma.text = plain.split("").map(function (c, i) {
        var s = data.enigma.steps[i];
        return s ? K.ALPHA[s.lamp] : c;
      }).join("");
      var block = new Uint8Array(16);
      block.set(K.utf8(plain).subarray(0, 16));
      for (var j = plain.length; j < 16; j++) block[j] = 16 - plain.length;    // PKCS#7 padding
      data.aes = K.aes128(AES_KEY).encryptBlock(block, true);
      data.aesBlock = block;
    }

    // ---------------------------------------------------------------- layout

    function layout() {
      var r = canvas.getBoundingClientRect();
      dpr = Math.min(window.devicePixelRatio || 1, 2);
      W = r.width; H = r.height;
      canvas.width = Math.round(W * dpr);
      canvas.height = Math.round(H * dpr);
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }

    // The stage: right part of the screen on wide layouts (the copy is on the left), the whole canvas on phones.
    function stage() {
      var wide = W >= 900;
      var x0 = wide ? Math.max(W * 0.46, 520) : 16;
      var x1 = W - (wide ? 40 : 16);
      var y0 = wide ? 70 : 20;
      var y1 = H - (wide ? 110 : 20);
      return { x: x0, y: y0, w: x1 - x0, h: y1 - y0, cx: (x0 + x1) / 2 };
    }

    // ---------------------------------------------------------------- drawing helpers

    function font(size, weight, mono) {
      return (weight || 600) + " " + size + "px " + (mono ? "ui-monospace, 'Cascadia Mono', Menlo, Consolas, monospace"
        : "system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif");
    }
    function roundRect(x, y, w, hh, r) {
      ctx.beginPath();
      ctx.moveTo(x + r, y);
      ctx.arcTo(x + w, y, x + w, y + hh, r);
      ctx.arcTo(x + w, y + hh, x, y + hh, r);
      ctx.arcTo(x, y + hh, x, y, r);
      ctx.arcTo(x, y, x + w, y, r);
      ctx.closePath();
    }
    function text(s, x, y, size, color, weight, mono, align) {
      ctx.font = font(size, weight, mono);
      ctx.fillStyle = color;
      ctx.textAlign = align || "center";
      ctx.textBaseline = "middle";
      ctx.fillText(s, x, y);
    }

    // The row of tiles: `from` and `to` are strings; each tile flips from one to the other at its own time.
    function tiles(st, y, from, to, progress, opts) {
      opts = opts || {};
      var n = from.length, gap = Math.max(3, st.w * 0.008);
      var size = Math.min(48, (st.w - gap * (n - 1)) / n);
      var x = st.cx - (n * size + (n - 1) * gap) / 2;
      var out = [];
      for (var i = 0; i < n; i++) {
        var local = clamp(progress * (n + 3) - i, 0, 1);
        var flip = Math.abs(Math.cos(local * Math.PI));
        var shown = local < 0.5 ? from[i] : to[i];
        var tx = x + i * (size + gap);
        out.push({ x: tx + size / 2, y: y, size: size });
        if (from[i] === " ") continue;
        var hh = size * 1.18 * Math.max(0.08, flip);
        ctx.save();
        roundRect(tx, y - hh / 2, size, hh, 6);
        var lit = opts.lit === i;
        ctx.fillStyle = lit ? colors["--accent"] : local >= 0.5 ? colors["--fg"] : colors["--bg"];
        ctx.fill();
        ctx.lineWidth = 1.5;
        ctx.strokeStyle = local >= 0.5 ? colors["--fg"] : colors["--rule"];
        ctx.stroke();
        if (flip > 0.25) {
          text(shown, tx + size / 2, y + 1, size * 0.56, local >= 0.5 || lit ? colors["--bg"] : colors["--fg"], 700, true);
        }
        ctx.restore();
      }
      return out;
    }

    // ---------------------------------------------------------------- the acts

    function drawCaesar(st, p) {
      var cy = st.y + st.h * 0.36, R = Math.min(st.w * 0.3, st.h * 0.3, 170);
      var turn = ease((p - 0.08) / 0.25) * 3;
      for (var ring = 0; ring < 2; ring++) {
        var r = ring ? R * 0.74 : R;
        ctx.beginPath();
        ctx.arc(st.cx, cy, r + 14, 0, 2 * Math.PI);
        ctx.fillStyle = ring ? colors["--bg"] : colors["--bg-soft"];
        ctx.fill();
        ctx.strokeStyle = colors["--rule"];
        ctx.lineWidth = 1.5;
        ctx.stroke();
        for (var i = 0; i < 26; i++) {
          var k = ring ? i - turn : i;
          var a = (k / 26) * 2 * Math.PI - Math.PI / 2;
          var hot = ring ? Math.round(i) === 3 : i === 0;
          text(K.ALPHA[i], st.cx + r * Math.cos(a), cy + r * Math.sin(a), R * 0.12,
            hot ? colors["--accent"] : ring ? colors["--fg"] : colors["--muted"], hot ? 800 : 600, true);
        }
      }
      text("+3", st.cx, cy, R * 0.28, colors["--accent-strong"], 800);
      tiles(st, st.y + st.h * 0.86, plain, data.caesar, ease((p - 0.35) / 0.45));
    }

    function drawVigenere(st, p) {
      var y = st.y + st.h * 0.62;
      var key = "LEMON", j = 0, keyRow = "";
      for (var i = 0; i < plain.length; i++) keyRow += /[A-Z]/.test(plain[i]) ? key[j++ % key.length] : " ";
      var show = ease(p / 0.25);
      ctx.globalAlpha = show;
      var pos = tiles(st, y - 74, keyRow, keyRow, 1);
      ctx.globalAlpha = 1;
      pos.forEach(function (q, i) {
        if (keyRow[i] === " ") return;
        var shift = keyRow.charCodeAt(i) - 65;
        text("+" + shift, q.x, y - 34, q.size * 0.32, colors["--accent-strong"], 700, true);
      });
      text(t("splash.keyword") + " LEMON", st.cx, st.y + st.h * 0.2, Math.min(28, st.w * 0.05), colors["--muted"], 700);
      tiles(st, y + 26, plain, data.vigenere, ease((p - 0.3) / 0.5));
    }

    function drawEnigma(st, p) {
      var steps = data.enigma.steps, n = steps.length;
      var k = clamp(Math.floor((p - 0.12) / 0.7 * n), -1, n - 1);
      var cur = k >= 0 ? steps[k] : null;
      var last = cur;
      for (var b = k; !last && b > 0; b--) last = steps[b - 1];
      var pos = last ? last.pos : [0, 3, 20];
      // Three rotors seen from above: drums with the letter in the window.
      var rw = Math.min(70, st.w * 0.12), rh = rw * 2.1, top = st.y + st.h * 0.08;
      for (var r = 0; r < 3; r++) {
        var x = st.cx + (r - 1) * rw * 1.5 - rw / 2;
        var g = ctx.createLinearGradient(x, 0, x + rw, 0);
        g.addColorStop(0, colors["--rule"]); g.addColorStop(0.5, colors["--bg"]); g.addColorStop(1, colors["--rule"]);
        roundRect(x, top, rw, rh, 10);
        ctx.fillStyle = g; ctx.fill();
        ctx.strokeStyle = colors["--faint"]; ctx.lineWidth = 1.5; ctx.stroke();
        for (var d = -1; d <= 1; d++) {
          var letter = K.ALPHA[(pos[r] + d + 26) % 26];
          text(letter, x + rw / 2, top + rh / 2 + d * rw * 0.62, d ? rw * 0.3 : rw * 0.44, d ? colors["--faint"] : colors["--fg"],
            d ? 600 : 800, true);
        }
        text(["I", "II", "III"][r], x + rw / 2, top + rh + 16, 12, colors["--muted"], 700);
      }
      // The lampboard: three rows as on the machine, the lit lamp in accent colour.
      var rows = ["QWERTZUIO", "ASDFGHJK", "PYXCVBNML"];
      var lw = Math.min(30, st.w / 11), ly = top + rh + 44;
      rows.forEach(function (row, ri) {
        for (var i = 0; i < row.length; i++) {
          var lx = st.cx + (i - (row.length - 1) / 2) * lw * 1.12;
          var on = cur && K.ALPHA[cur.lamp] === row[i];
          ctx.beginPath();
          ctx.arc(lx, ly + ri * lw * 1.1, lw * 0.44, 0, 2 * Math.PI);
          ctx.fillStyle = on ? "#f6c443" : colors["--bg-soft"];
          ctx.fill();
          ctx.strokeStyle = on ? "#c99210" : colors["--rule"];
          ctx.stroke();
          text(row[i], lx, ly + ri * lw * 1.1 + 1, lw * 0.42, on ? "#2b1d00" : colors["--muted"], 700, true);
        }
      });
      var done = data.enigma.text.split("").map(function (c, i) { return i <= k ? c : plain[i]; }).join("");
      tiles(st, st.y + st.h * 0.9, plain, done, 1, { lit: k });
    }

    function drawAes(st, p) {
      var steps = data.aes.steps, s;
      // First the letters turn into bytes, then the state goes through the rounds, then out comes the ciphertext.
      var into = ease(p / 0.15);
      var idx = clamp(Math.floor((p - 0.15) / 0.7 * steps.length), 0, steps.length - 1);
      s = steps[idx];
      var cell = Math.min(54, st.h * 0.12, st.w * 0.11), gx = st.cx - cell * 2, gy = st.y + st.h * 0.1;
      var prev = idx > 0 ? steps[idx - 1].state : s.state;
      for (var c = 0; c < 4; c++) {
        for (var r = 0; r < 4; r++) {
          var b = into < 1 ? data.aesBlock[4 * c + r] : s.state[4 * c + r];
          var changed = into >= 1 && b !== prev[4 * c + r];
          roundRect(gx + c * cell + 2, gy + r * cell + 2, cell - 4, cell - 4, 6);
          var hue = (b * 1.41) % 360;
          ctx.fillStyle = into < 1 ? colors["--bg-soft"] : "hsla(" + hue + ", 55%, " + (changed ? 52 : 46) + "%, " + (changed ? 0.95 : 0.78) + ")";
          ctx.fill();
          text(hex2(b), gx + c * cell + cell / 2, gy + r * cell + cell / 2, cell * 0.34, into < 1 ? colors["--fg"] : "#fff", 700, true);
        }
      }
      var label = s.round === 0 && s.op === "input" ? t("splash.aes.input")
        : t("splash.aes.round", { r: s.round }) + " · " + s.op;
      text(label, st.cx, gy + 4 * cell + 26, 15, colors["--accent-strong"], 700, true);
      var outHex = K.hex(data.aes.out).toUpperCase();
      var revealed = clamp((p - 0.86) / 0.12, 0, 1);
      var ciph = outHex.slice(0, Math.round(revealed * 32)).replace(/(..)/g, "$1 ").trim();
      text(revealed > 0 ? ciph : plain + "  →  " + Array.prototype.map.call(data.aesBlock.subarray(0, 6), hex2).join(" ") + "…",
        st.cx, st.y + st.h * 0.9, Math.min(17, st.w / 34), colors["--fg"], 700, true);
    }

    function drawPublicKey(st, p) {
      var y = st.y + st.h * 0.5, left = st.x + st.w * 0.14, right = st.x + st.w * 0.86;
      text(t("splash.alice"), left, y + 70, 16, colors["--muted"], 700);
      text(t("splash.bob"), right, y + 70, 16, colors["--muted"], 700);
      [left, right].forEach(function (x) {
        ctx.beginPath(); ctx.arc(x, y - 14, 18, 0, 2 * Math.PI); ctx.fillStyle = colors["--rule"]; ctx.fill();
        roundRect(x - 26, y + 8, 52, 40, 18); ctx.fill();
      });
      // Bob's open padlock flies to Alice; she locks the message; the box flies back; only Bob's key opens it.
      var a = ease(p / 0.3), b = ease((p - 0.32) / 0.12), c = ease((p - 0.46) / 0.3), d = ease((p - 0.8) / 0.12);
      var lx = c > 0 ? lerp(left + 70, right - 70, c) : lerp(right - 70, left + 70, a);
      var closed = b > 0.5 && d < 0.5;
      ctx.lineWidth = 6;
      ctx.strokeStyle = colors["--accent"];
      ctx.beginPath();
      ctx.arc(lx, y - 44 - (closed ? 0 : 10), 16, Math.PI, 2 * Math.PI);
      ctx.stroke();
      roundRect(lx - 24, y - 44, 48, 40, 7);
      ctx.fillStyle = colors["--accent"]; ctx.fill();
      text(closed ? "🔒" : "🔓", lx, y - 24, 16, "#fff", 700);
      if (b > 0) {
        text(c > 0 && d < 0.5 ? K.hex(data.aes.out).toUpperCase().slice(0, 12) + "…" : plain, lx, y + 20, 13,
          colors["--fg"], 700, true);
      }
      text("🗝", right + 4, y - 64, 26, d > 0 ? colors["--accent"] : colors["--muted"], 700);
      text(t("splash.pk.public"), right, st.y + st.h * 0.12, 14, colors["--muted"], 600);
      text("e · d ≡ 1 (mod φ(n))", st.cx, st.y + st.h * 0.88, Math.min(20, st.w / 26), colors["--accent-strong"], 700, true);
    }

    function drawLattice(st, p) {
      var cx = st.cx, cy = st.y + st.h * 0.48, sc = Math.min(st.w, st.h) / 9;
      var b1 = [1, 0.25], b2 = [0.35, 1];
      ctx.fillStyle = colors["--faint"];
      for (var i = -7; i <= 7; i++) {
        for (var j = -7; j <= 7; j++) {
          var x = cx + (i * b1[0] + j * b2[0]) * sc, y = cy - (i * b1[1] + j * b2[1]) * sc;
          if (x < st.x || x > st.x + st.w || y < st.y || y > st.y + st.h * 0.82) continue;
          ctx.beginPath(); ctx.arc(x, y, 2.6, 0, 2 * Math.PI); ctx.fill();
        }
      }
      // A secret lattice point plus noise: easy to undo with the good basis, hopeless without it in 768 dimensions.
      var target = [2 * b1[0] + 1 * b2[0], 2 * b1[1] + 1 * b2[1]];
      var tx = cx + target[0] * sc, ty = cy - target[1] * sc;
      var noise = ease(p / 0.3) * 0.38 * sc, ang = 2.1;
      var nx = tx + noise * Math.cos(ang), ny = ty + noise * Math.sin(ang);
      ctx.strokeStyle = colors["--accent"];
      ctx.lineWidth = 3;
      ctx.beginPath(); ctx.moveTo(cx, cy); ctx.lineTo(cx + b1[0] * sc, cy - b1[1] * sc); ctx.stroke();
      ctx.beginPath(); ctx.moveTo(cx, cy); ctx.lineTo(cx + b2[0] * sc, cy - b2[1] * sc); ctx.stroke();
      ctx.beginPath(); ctx.arc(tx, ty, 7, 0, 2 * Math.PI); ctx.fillStyle = colors["--k3"]; ctx.fill();
      ctx.beginPath(); ctx.arc(nx, ny, 7, 0, 2 * Math.PI); ctx.fillStyle = colors["--k2"]; ctx.fill();
      var back = ease((p - 0.55) / 0.25);
      if (back > 0) {
        ctx.setLineDash([5, 5]);
        ctx.strokeStyle = colors["--k3"];
        ctx.lineWidth = 2;
        ctx.beginPath(); ctx.moveTo(nx, ny); ctx.lineTo(lerp(nx, tx, back), lerp(ny, ty, back)); ctx.stroke();
        ctx.setLineDash([]);
      }
      text(t("splash.lattice.noise"), nx - 12, ny - 22, 13, colors["--k2"], 700, false, "right");
      text("b = A·s + e", st.cx, st.y + st.h * 0.9, Math.min(22, st.w / 22), colors["--accent-strong"], 700, true);
    }

    var DRAW = { caesar: drawCaesar, vigenere: drawVigenere, enigma: drawEnigma, aes: drawAes,
      publickey: drawPublicKey, lattice: drawLattice };

    function draw() {
      ctx.clearRect(0, 0, W, H);
      var st = stage(), name = ACTS[act];
      var p = clamp(at / DUR[name], 0, 1);
      // Fade between acts.
      var alpha = Math.min(1, at / 0.5, (DUR[name] - at) / 0.5);
      ctx.globalAlpha = reduce ? 1 : clamp(alpha, 0, 1);
      // Year in large faint type behind the scene.
      text(t("splash.year." + name), st.cx, st.y + st.h * 0.5, Math.min(st.w * 0.3, 190), colors["--rule"], 800);
      ctx.globalAlpha = reduce ? 1 : clamp(alpha, 0, 1);
      DRAW[name](st, reduce ? 1 : p);
      ctx.globalAlpha = 1;
    }

    function syncHud() {
      chips.forEach(function (c, i) {
        if (i === act) c.setAttribute("aria-current", "step"); else c.removeAttribute("aria-current");
        c.classList.toggle("done", i < act);
      });
      caption.textContent = t("splash.cap." + ACTS[act]);
    }

    function goTo(i) {
      act = (i + ACTS.length) % ACTS.length;
      at = 0;
      syncHud();
      draw();
    }

    // ---------------------------------------------------------------- loop, visibility and controls

    var last = 0;
    function frame(now) {
      if (!running) return;
      var dt = Math.min(0.05, (now - (last || now)) / 1000);
      last = now;
      at += dt;
      if (at >= DUR[ACTS[act]]) goTo(act + 1);
      draw();
      requestAnimationFrame(frame);
    }
    function setRunning(on) {
      on = on && !paused && visible && !reduce;
      if (on === running) return;
      running = on;
      last = 0;
      if (on) requestAnimationFrame(frame);
    }
    function syncPause() {
      pauseBtn.textContent = paused ? "▶ " + t("splash.play") : "❚❚ " + t("splash.pause");
      pauseBtn.setAttribute("aria-pressed", paused ? "true" : "false");
    }
    pauseBtn.addEventListener("click", function () { paused = !paused; syncPause(); setRunning(true); });
    nextBtn.addEventListener("click", function () { goTo(act + 1); });
    chips.forEach(function (c, i) {
      c.addEventListener("click", function () { goTo(i); });
      c.setAttribute("role", "button");
      c.tabIndex = 0;
      c.addEventListener("keydown", function (e) { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); goTo(i); } });
    });
    if (reduce) pauseBtn.hidden = true;
    syncPause();

    if ("IntersectionObserver" in window) {
      new IntersectionObserver(function (entries) { visible = entries[0].isIntersecting; setRunning(true); }).observe(root);
    }
    document.addEventListener("visibilitychange", function () { visible = !document.hidden; setRunning(true); });
    var resizeTimer = 0;
    window.addEventListener("resize", function () { clearTimeout(resizeTimer); resizeTimer = setTimeout(function () { layout(); draw(); }, 120); });
    M.onTheme(function () { readColors(); draw(); });

    readColors();
    prepare();
    layout();
    goTo(0);
    setRunning(true);
  });
})();
