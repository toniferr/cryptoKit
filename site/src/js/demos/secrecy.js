// Chapter 03 — perfect secrecy: the one-time pad hides everything (any plaintext of the same length is possible),
// and reusing the pad gives it all away (c1 ⊕ c2 = m1 ⊕ m2, then crib dragging).
(function () {
  "use strict";
  var M = window.CK, K = window.CKL, h = M.h, t = M.t;

  function bytesRow(label, bytes, cls) {
    var cells = [];
    for (var i = 0; i < bytes.length; i++) cells.push(h("span", { class: "byte" }, (bytes[i] < 16 ? "0" : "") + bytes[i].toString(16)));
    return h("div", { class: "byte-row " + (cls || "") }, h("span", { class: "byte-label" }, label), h("span", { class: "bytes" }, cells));
  }
  function printable(bytes) {
    var s = "";
    for (var i = 0; i < bytes.length; i++) s += bytes[i] >= 32 && bytes[i] < 127 ? String.fromCharCode(bytes[i]) : "·";
    return s;
  }

  // ---------------------------------------------------------------- the one-time pad and its decoys

  M.register("secrecy/otp", function (stage) {
    var msg = M.textInput({ label: t("sc.message"), value: t("sc.msg1"), maxlength: 32, onInput: newPad });
    var decoy = M.textInput({ label: t("sc.decoy"), value: t("sc.decoy1"), maxlength: 32, onInput: render });
    var rows = h("div", { class: "byte-rows" });
    var note = h("p", { class: "demo-hint" });
    var again = M.button(t("sc.newPad"), function () { newPad(); });
    stage.appendChild(M.controls(msg.el, again));
    stage.appendChild(rows);
    stage.appendChild(M.controls(decoy.el));
    var decoyRows = h("div", { class: "byte-rows" });
    stage.appendChild(decoyRows);
    stage.appendChild(note);
    var pad, m, c;

    function newPad() {
      m = K.utf8(msg.get().slice(0, 32));
      pad = K.randomBytes(m.length);
      c = K.xor(m, pad);
      render();
    }
    function render() {
      M.clear(rows);
      rows.appendChild(bytesRow(t("sc.m"), m));
      rows.appendChild(bytesRow("⊕ " + t("sc.k"), pad, "key"));
      rows.appendChild(bytesRow("= " + t("sc.c"), c, "cipher"));
      // Any other message of the same length: the key that turns c into it.
      // Same number of bytes as the real message: pad with spaces or cut.
      var d = new Uint8Array(m.length).fill(32);
      d.set(K.utf8(decoy.get()).subarray(0, m.length));
      var fake = K.xor(c, d);
      M.clear(decoyRows);
      decoyRows.appendChild(bytesRow(t("sc.c"), c, "cipher"));
      decoyRows.appendChild(bytesRow("⊕ " + t("sc.k2"), fake, "key alt"));
      decoyRows.appendChild(bytesRow("= " + t("sc.m2"), d));
      note.textContent = t("sc.decoyNote", { m: K.fromUtf8(d) });
    }
    newPad();
  });

  // ---------------------------------------------------------------- the two-time pad: crib dragging

  M.register("secrecy/twotime", function (stage) {
    var m1 = K.utf8(t("sc.tt1")), m2 = K.utf8(t("sc.tt2"));
    var n = Math.min(m1.length, m2.length);
    m1 = m1.subarray(0, n); m2 = m2.subarray(0, n);
    var pad = K.randomBytes(n), c1 = K.xor(m1, pad), c2 = K.xor(m2, pad), x = K.xor(c1, c2);
    var crib = M.textInput({ label: t("sc.crib"), value: t("sc.cribDefault"), maxlength: 20, wide: false, onInput: render });
    var rows = h("div", { class: "byte-rows" });
    var out = h("ol", { class: "drag" });
    stage.appendChild(rows);
    rows.appendChild(bytesRow("c₁", c1, "cipher"));
    rows.appendChild(bytesRow("c₂", c2, "cipher"));
    rows.appendChild(bytesRow("c₁ ⊕ c₂", x, "key"));
    stage.appendChild(M.controls(crib.el));
    stage.appendChild(h("p", { class: "panel-label" }, t("sc.dragTitle")));
    stage.appendChild(out);

    function render() {
      var cb = K.utf8(crib.get());
      M.clear(out);
      if (!cb.length) return;
      for (var off = 0; off + cb.length <= n; off++) {
        var guess = K.xor(x.subarray(off, off + cb.length), cb);
        var text = printable(guess);
        var readable = /^[A-Za-z ,.'!?¿¡áéíóúñÁÉÍÓÚÑ-]+$/.test(K.fromUtf8(guess)) && !/·/.test(text);
        out.appendChild(h("li", { class: readable ? "best" : null }, h("span", { class: "k" }, String(off)),
          h("span", { class: "p" }, text)));
      }
    }
    render();
  });
})();
