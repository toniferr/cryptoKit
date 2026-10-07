// Introduction — from text to bytes: UTF-8, hexadecimal and Base64, with Base64's 6-bit groups made visible.
(function () {
  "use strict";
  var M = window.CK, K = window.CKL, h = M.h, t = M.t;
  var B64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

  M.register("intro/encodings", function (stage) {
    var input = M.textInput({ label: t("in.text"), value: t("in.sample"), maxlength: 24, onInput: draw });
    var rows = h("div", { class: "enc-rows" });
    stage.appendChild(M.controls(input.el));
    stage.appendChild(rows);

    function bits(b) { return ("0000000" + b.toString(2)).slice(-8); }

    function draw() {
      var bytes = K.utf8(input.get());
      M.clear(rows);
      // Characters and their bytes (one character can take several bytes: é is c3 a9).
      var chars = h("div", { class: "enc-row" }, h("span", { class: "byte-label" }, t("in.chars")));
      Array.from(input.get()).forEach(function (ch) {
        var n = K.utf8(ch).length;
        chars.appendChild(h("span", { class: "enc-char", dataset: { n: String(n) } }, ch === " " ? "␣" : ch));
      });
      rows.appendChild(chars);
      var hexRow = h("div", { class: "enc-row" }, h("span", { class: "byte-label" }, "UTF-8 (hex)"));
      var binRow = h("div", { class: "enc-row bits" }, h("span", { class: "byte-label" }, t("in.bits")));
      for (var i = 0; i < bytes.length; i++) {
        hexRow.appendChild(h("span", { class: "byte" }, (bytes[i] < 16 ? "0" : "") + bytes[i].toString(16)));
        binRow.appendChild(h("span", { class: "bits8" }, bits(bytes[i])));
      }
      rows.appendChild(hexRow);
      rows.appendChild(binRow);
      // Base64: the same bits regrouped in sixes, each six a character of a 64-letter alphabet.
      var all = Array.prototype.map.call(bytes, bits).join("");
      var sixRow = h("div", { class: "enc-row bits" }, h("span", { class: "byte-label" }, t("in.sixes")));
      var b64Row = h("div", { class: "enc-row" }, h("span", { class: "byte-label" }, "Base64"));
      for (var j = 0; j < all.length; j += 6) {
        var six = (all.slice(j, j + 6) + "00000").slice(0, 6);
        sixRow.appendChild(h("span", { class: "bits6" }, six));
        b64Row.appendChild(h("span", { class: "b64" }, B64[parseInt(six, 2)]));
      }
      for (var p = 0; p < (3 - bytes.length % 3) % 3; p++) b64Row.appendChild(h("span", { class: "b64 pad" }, "="));
      rows.appendChild(sixRow);
      rows.appendChild(b64Row);
      rows.appendChild(h("p", { class: "demo-hint" }, t("in.sizes", { chars: Array.from(input.get()).length, bytes: bytes.length,
        hex: bytes.length * 2, b64: K.base64(bytes).length })));
    }
    draw();
  });
})();
