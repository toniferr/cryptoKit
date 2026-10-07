// Chapter 05 — modes of operation: the ECB "penguin" (an image encrypted with real AES in ECB, CBC and CTR),
// PKCS#7 padding, and malleability: flipping ciphertext bits in CTR changes the message at will; GCM refuses it.
(function () {
  "use strict";
  var M = window.CK, K = window.CKL, h = M.h, t = M.t;
  var SIZE = 128;

  // A flat, original picture (a padlock and a key) with four colours, one byte per pixel: large runs of equal bytes,
  // which is exactly what ECB cannot hide.
  function picture() {
    var c = document.createElement("canvas");
    c.width = c.height = SIZE;
    var g = c.getContext("2d");
    var pal = ["#f3efe4", "#1f6f68", "#e0a43a", "#2b2b2b"];
    g.fillStyle = pal[0]; g.fillRect(0, 0, SIZE, SIZE);
    g.strokeStyle = pal[3]; g.lineWidth = 14;
    g.beginPath(); g.arc(64, 50, 26, Math.PI, 0); g.stroke();
    g.fillStyle = pal[1]; g.fillRect(26, 48, 76, 62);
    g.fillStyle = pal[2]; g.beginPath(); g.arc(64, 72, 10, 0, 2 * Math.PI); g.fill();
    g.fillRect(59, 76, 10, 22);
    g.fillStyle = pal[3]; g.fillRect(0, 118, SIZE, 10);
    var img = g.getImageData(0, 0, SIZE, SIZE).data, out = new Uint8Array(SIZE * SIZE);
    var rgb = pal.map(function (p) { return [parseInt(p.substr(1, 2), 16), parseInt(p.substr(3, 2), 16), parseInt(p.substr(5, 2), 16)]; });
    for (var i = 0; i < out.length; i++) {
      var best = 0, bd = Infinity;
      for (var k = 0; k < 4; k++) {
        var d = Math.abs(img[4 * i] - rgb[k][0]) + Math.abs(img[4 * i + 1] - rgb[k][1]) + Math.abs(img[4 * i + 2] - rgb[k][2]);
        if (d < bd) { bd = d; best = k; }
      }
      out[i] = best * 60 + 7;     // four distinct byte values
    }
    return { bytes: out, pal: rgb };
  }

  function paint(canvas, bytes, pal) {
    var g = canvas.getContext("2d"), img = g.createImageData(SIZE, SIZE);
    for (var i = 0; i < bytes.length; i++) {
      var b = bytes[i], col;
      if (pal && (b - 7) % 60 === 0 && (b - 7) / 60 < 4) col = pal[(b - 7) / 60];
      else {
        // Ciphertext: one colour per 16-byte block, taken from the block's first bytes, so equal blocks look equal.
        var j = i & ~15;
        col = [bytes[j], bytes[j + 1], bytes[j + 2]];
      }
      img.data[4 * i] = col[0]; img.data[4 * i + 1] = col[1]; img.data[4 * i + 2] = col[2]; img.data[4 * i + 3] = 255;
    }
    g.putImageData(img, 0, 0);
  }

  M.register("modes/penguin", function (stage) {
    var pic = picture();
    var aes = K.aes128(K.randomBytes(16)), iv = K.randomBytes(16);
    var left = h("canvas", { class: "pixels", width: SIZE, height: SIZE, role: "img", "aria-label": t("md.original") });
    var right = h("canvas", { class: "pixels", width: SIZE, height: SIZE, role: "img", "aria-label": t("md.encrypted") });
    var cap = h("p", { class: "panel-label" });
    var mode = M.segmented({ label: t("md.mode"), value: "ECB",
      options: [{ value: "ECB", label: "ECB" }, { value: "CBC", label: "CBC" }, { value: "CTR", label: "CTR" }],
      onChange: draw });
    stage.appendChild(M.controls(mode.el));
    stage.appendChild(h("div", { class: "panel-grid pixels-grid" },
      h("div", null, h("p", { class: "panel-label" }, t("md.original")), left),
      h("div", null, cap, right)));
    paint(left, pic.bytes, pic.pal);

    function draw() {
      var m = mode.get();
      var c = m === "ECB" ? K.ecb(aes, pic.bytes) : m === "CBC" ? K.cbc(aes, pic.bytes, iv) : K.ctr(aes, pic.bytes, iv);
      paint(right, c, null);
      cap.textContent = t("md.encryptedWith", { m: m });
    }
    draw();
  });

  // ---------------------------------------------------------------- PKCS#7 padding

  M.register("modes/padding", function (stage) {
    var input = M.textInput({ label: t("md.message"), value: t("md.padSample"), maxlength: 60, onInput: draw });
    var blocks = h("div", { class: "blocks" });
    stage.appendChild(M.controls(input.el));
    stage.appendChild(blocks);

    function draw() {
      var b = K.utf8(input.get()), pad = 16 - b.length % 16, all = new Uint8Array(b.length + pad);
      all.set(b);
      all.fill(pad, b.length);
      M.clear(blocks);
      for (var i = 0; i < all.length; i += 16) {
        var row = h("div", { class: "block-row" }, h("span", { class: "byte-label" }, t("md.block", { n: i / 16 + 1 })));
        for (var j = i; j < i + 16; j++) {
          var isPad = j >= b.length, v = all[j];
          row.appendChild(h("span", { class: "byte" + (isPad ? " pad" : ""), title: isPad ? t("md.padByte") : "" },
            isPad ? (v < 16 ? "0" : "") + v.toString(16) : v >= 32 && v < 127 ? String.fromCharCode(v) : "·"));
        }
        blocks.appendChild(row);
      }
      blocks.appendChild(h("p", { class: "demo-hint" }, t("md.padNote", { n: pad })));
    }
    draw();
  });

  // ---------------------------------------------------------------- malleability: CTR versus GCM

  M.register("modes/malleable", function (stage) {
    var original = t("md.order");
    var target = M.textInput({ label: t("md.attacker"), value: t("md.forged"), maxlength: original.length, onInput: run });
    var rows = h("div", { class: "mall" });
    stage.appendChild(h("p", { class: "mall-orig" }, h("span", { class: "panel-label" }, t("md.sent")), " ", h("code", null, original)));
    stage.appendChild(M.controls(target.el));
    stage.appendChild(rows);
    var key = K.randomBytes(16), aes = K.aes128(key), nonce = K.randomBytes(16), gcmKey = null;
    var p = K.utf8(original), ctrC = K.ctr(aes, p, nonce), gcmIv = K.randomBytes(12), gcmC = null;
    var subtle = window.crypto && window.crypto.subtle;
    var ready = subtle ? subtle.importKey("raw", key, "AES-GCM", false, ["encrypt", "decrypt"]).then(function (k) {
      gcmKey = k;
      return subtle.encrypt({ name: "AES-GCM", iv: gcmIv }, k, p);
    }).then(function (c) { gcmC = new Uint8Array(c); }) : Promise.resolve();

    function forge(c) {
      // c' = c ⊕ (known plaintext ⊕ wanted plaintext): no key needed.
      var want = new Uint8Array(p.length).fill(32);
      want.set(K.utf8(target.get()).subarray(0, p.length));
      var out = c.slice(), d = K.xor(p, want);
      for (var i = 0; i < d.length; i++) out[i] ^= d[i];
      return out;
    }
    function run() {
      M.clear(rows);
      var forgedCtr = forge(ctrC);
      var dec = K.fromUtf8(K.ctr(aes, forgedCtr, nonce));
      rows.appendChild(h("div", { class: "mall-row bad" }, h("strong", null, "AES-CTR"), h("span", null, t("md.decryptsTo")),
        h("code", null, dec), h("span", { class: "verdict" }, t("md.fooled"))));
      var gcmRow = h("div", { class: "mall-row" }, h("strong", null, "AES-GCM"), h("span", null, t("md.checking")));
      rows.appendChild(gcmRow);
      ready.then(function () {
        if (!gcmC) { gcmRow.lastChild.textContent = t("md.noWebCrypto"); return null; }
        var forged = forge(gcmC);        // the tag at the end is left as it was
        return subtle.decrypt({ name: "AES-GCM", iv: gcmIv }, gcmKey, forged).then(function () {
          gcmRow.className = "mall-row bad";
          M.clear(gcmRow).appendChild(h("strong", null, "AES-GCM"));
          gcmRow.appendChild(h("span", null, t("md.same")));
        }, function () {
          gcmRow.className = "mall-row good";
          M.clear(gcmRow);
          gcmRow.appendChild(h("strong", null, "AES-GCM"));
          gcmRow.appendChild(h("span", null, t("md.rejected")));
          gcmRow.appendChild(h("span", { class: "verdict" }, t("md.safe")));
        });
      });
    }
    run();
  });
})();
