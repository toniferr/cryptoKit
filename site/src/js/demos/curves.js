// Chapter 09 — elliptic curves: the chord-and-tangent law over the real numbers, and the same curve over F_97, where
// the multiples kP jump around with no visible pattern (the discrete logarithm problem).
(function () {
  "use strict";
  var M = window.CK, K = window.CKL, h = M.h, s = M.s, t = M.t;

  M.register("curves/real", function (stage) {
    var a = M.slider({ label: "a", min: -3, max: 2, step: 0.1, value: -2, onInput: draw, format: function (v) { return M.fmt(v, 1); } });
    var b = M.slider({ label: "b", min: 0.5, max: 4, step: 0.1, value: 2, onInput: draw, format: function (v) { return M.fmt(v, 1); } });
    var tp = M.slider({ label: "P", min: 0, max: 1, step: 0.001, value: 0.18, onInput: draw, format: function () { return ""; } });
    var tq = M.slider({ label: "Q", min: 0, max: 1, step: 0.001, value: 0.42, onInput: draw, format: function () { return ""; } });
    var svg = M.svgBox(stage, 640, 400, t("cv.realLabel"));
    var readout = h("p", { class: "demo-hint mono" });
    stage.insertBefore(M.controls(a.el, b.el), svg);
    stage.insertBefore(M.controls(tp.el, tq.el), svg);
    stage.appendChild(readout);
    var X0 = -3.2, X1 = 4.2, Y0 = -5, Y1 = 5, W = 640, H = 400;
    function sx(x) { return (x - X0) / (X1 - X0) * W; }
    function sy(y) { return H - (y - Y0) / (Y1 - Y0) * H; }

    // Points of the curve in order: the upper branch left to right, then the lower one right to left.
    function trace(A, B) {
      var up = [], lo = [];
      for (var i = 0; i <= 1400; i++) {
        var x = X0 + (X1 - X0) * i / 1400, r = x * x * x + A * x + B;
        if (r >= 0) { var y = Math.sqrt(r); if (y < Y1) { up.push([x, y]); lo.push([x, -y]); } }
      }
      return up.concat(lo.reverse());
    }
    function add(P, Q, A) {
      var l;
      if (Math.abs(P[0] - Q[0]) < 1e-9) {
        if (Math.abs(P[1] + Q[1]) < 1e-9) return null;
        l = (3 * P[0] * P[0] + A) / (2 * P[1]);
      } else l = (Q[1] - P[1]) / (Q[0] - P[0]);
      var x = l * l - P[0] - Q[0];
      return { sum: [x, -(P[1] + l * (x - P[0]))], third: [x, P[1] + l * (x - P[0])], slope: l };
    }
    function draw() {
      var A = a.get(), B = b.get(), pts = trace(A, B);
      M.clear(svg);
      svg.appendChild(s("line", { class: "axis-line", x1: 0, x2: W, y1: sy(0), y2: sy(0) }));
      svg.appendChild(s("line", { class: "axis-line", x1: sx(0), x2: sx(0), y1: 0, y2: H }));
      // Draw each branch as its own path (the curve can have two components).
      var seg = [], segs = [];
      for (var i = 0; i < pts.length; i++) {
        if (i && Math.hypot(pts[i][0] - pts[i - 1][0], pts[i][1] - pts[i - 1][1]) > 0.6) { segs.push(seg); seg = []; }
        seg.push([sx(pts[i][0]), sy(pts[i][1])]);
      }
      segs.push(seg);
      segs.forEach(function (sg) { if (sg.length > 1) svg.appendChild(s("path", { class: "curve-fn", d: M.path(sg) })); });
      if (!pts.length) return;
      var P = pts[Math.round(tp.get() * (pts.length - 1))], Q = pts[Math.round(tq.get() * (pts.length - 1))];
      var r = add(P, Q, A);
      if (!r) { readout.textContent = t("cv.infinity"); return; }
      var x0 = Math.min(P[0], Q[0], r.third[0]) - 0.6, x1 = Math.max(P[0], Q[0], r.third[0]) + 0.6;
      svg.appendChild(s("line", { class: "curve", x1: sx(x0), y1: sy(P[1] + r.slope * (x0 - P[0])), x2: sx(x1), y2: sy(P[1] + r.slope * (x1 - P[0])) }));
      svg.appendChild(s("line", { class: "curve-muted", x1: sx(r.third[0]), y1: sy(r.third[1]), x2: sx(r.sum[0]), y2: sy(r.sum[1]) }));
      [[P, "P", "pt-dot"], [Q, "Q", "pt-dot"], [r.third, "−(P+Q)", "pt-dot-2"], [r.sum, "P+Q", "pt-star"]].forEach(function (d) {
        svg.appendChild(s("circle", { cx: sx(d[0][0]), cy: sy(d[0][1]), r: 6, class: d[2] }));
        svg.appendChild(s("text", { x: sx(d[0][0]) + 9, y: sy(d[0][1]) - 8, class: "label-strong" }, d[1]));
      });
      readout.textContent = "y² = x³ " + (A < 0 ? "− " : "+ ") + M.fmt(Math.abs(A), 1) + "x + " + M.fmt(B, 1) + "   ·   P+Q = (" +
        M.fmt(r.sum[0], 2) + ", " + M.fmt(r.sum[1], 2) + ")";
    }
    draw();
  });

  M.register("curves/finite", function (stage) {
    var E = new K.Curve(2, 3, 97), all = E.points(), G = { x: 3, y: 6 }, n = E.order(G);
    var k = M.slider({ label: "k", min: 1, max: n - 1, step: 1, value: 1, onInput: draw, format: function (v) { return String(v); } });
    var play = M.button("▶ " + t("cv.play"), toggle);
    var svg = M.svgBox(stage, 520, 520, t("cv.finiteLabel"));
    var readout = h("p", { class: "demo-hint mono" });
    stage.insertBefore(M.controls(k.el, play), svg);
    stage.appendChild(readout);
    var multiples = [null];
    for (var i = 1; i < n; i++) multiples.push(E.mul(i, G));
    var timer = 0;
    function sx(x) { return 20 + x * 5; }
    function sy(y) { return 500 - y * 5; }
    function toggle() {
      if (timer) { clearInterval(timer); timer = 0; play.textContent = "▶ " + t("cv.play"); return; }
      play.textContent = "❚❚ " + t("cv.pause");
      timer = setInterval(function () { k.set(k.get() % (n - 1) + 1); }, 350);
    }
    function draw() {
      M.clear(svg);
      svg.appendChild(s("rect", { x: 18, y: 13, width: 489, height: 489, class: "grid-frame" }));
      all.forEach(function (p) { svg.appendChild(s("circle", { cx: sx(p.x), cy: sy(p.y), r: 2.6, class: "pt-faint" })); });
      var kk = k.get(), path = [];
      for (var j = 1; j <= kk; j++) path.push([sx(multiples[j].x), sy(multiples[j].y)]);
      if (path.length > 1) svg.appendChild(s("path", { class: "trail", d: M.path(path.slice(-12)) }));
      svg.appendChild(s("circle", { cx: sx(G.x), cy: sy(G.y), r: 6, class: "pt-dot" }));
      svg.appendChild(s("text", { x: sx(G.x) + 8, y: sy(G.y) - 8, class: "label-strong" }, "G"));
      var Pk = multiples[kk];
      svg.appendChild(s("circle", { cx: sx(Pk.x), cy: sy(Pk.y), r: 7, class: "pt-star" }));
      svg.appendChild(s("text", { x: sx(Pk.x) + 9, y: sy(Pk.y) - 9, class: "label-strong" }, kk + "G"));
      readout.textContent = t("cv.readout", { k: kk, x: Pk.x, y: Pk.y, n: n, count: all.length + 1 });
    }
    draw();
  });
})();
