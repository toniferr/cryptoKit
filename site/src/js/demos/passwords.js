// Chapter 07 — passwords: how many guesses a password is worth, how long each server-side hash makes an attacker
// wait, and real PBKDF2 (WebCrypto) timed in this browser.
(function () {
  "use strict";
  var M = window.CK, h = M.h, t = M.t;

  // Approximate guesses per second for one high-end consumer GPU (order of magnitude, public hashcat benchmarks).
  var SPEEDS = [
    { id: "md5", rate: 1.6e11 },
    { id: "sha256", rate: 2.2e10 },
    { id: "pbkdf2", rate: 1.5e4 },
    { id: "bcrypt", rate: 1.4e3 },
    { id: "argon2", rate: 1e3 },
  ];
  var COMMON = ["123456", "password", "123456789", "12345678", "12345", "qwerty", "abc123", "111111", "1234567",
    "iloveyou", "admin", "welcome", "monkey", "dragon", "letmein", "football", "password1", "princess", "sunshine",
    "qwerty123", "contraseña", "123123", "000000", "superman", "starwars", "teamo", "barcelona", "realmadrid"];

  function entropy(pw) {
    var pool = 0;
    if (/[a-z]/.test(pw)) pool += 26;
    if (/[A-Z]/.test(pw)) pool += 26;
    if (/[0-9]/.test(pw)) pool += 10;
    if (/[^A-Za-z0-9]/.test(pw)) pool += /[^\x00-\x7f]/.test(pw) ? 100 : 33;
    return { pool: pool, bits: pw.length ? pw.length * Math.log2(Math.max(pool, 1)) : 0 };
  }

  function duration(seconds) {
    var units = [["year", 31557600], ["day", 86400], ["hour", 3600], ["minute", 60], ["second", 1]];
    if (seconds < 1) return t("pw.instant");
    if (seconds > 31557600 * 1e9) return t("pw.forever");
    for (var i = 0; i < units.length; i++) {
      if (seconds >= units[i][1]) {
        var v = seconds / units[i][1];
        return (v >= 1e6 ? M.fmt(v / 1e6, 1) + " " + t("pw.million") : M.fmt(v, v < 10 ? 1 : 0)) + " " + t("pw.unit." + units[i][0]);
      }
    }
    return t("pw.instant");
  }

  M.register("passwords/entropy", function (stage) {
    var input = M.textInput({ label: t("pw.password"), value: "Tr0ub4dor&3", wide: false, onInput: update });
    var bits = M.stat(t("pw.bits"), "accent"), pool = M.stat(t("pw.pool"));
    var warn = h("p", { class: "demo-hint" });
    var table = h("table", { class: "mini-table" });
    stage.appendChild(M.controls(input.el));
    stage.appendChild(h("div", { class: "stats" }, bits.el, pool.el));
    stage.appendChild(warn);
    stage.appendChild(table);

    function update() {
      var pw = input.get(), e = entropy(pw), common = COMMON.indexOf(pw.toLowerCase()) >= 0;
      bits.set(M.fmt(e.bits, 0));
      pool.set(String(e.pool));
      warn.textContent = common ? t("pw.common") : t("pw.assumption");
      warn.classList.toggle("bad", common);
      M.clear(table);
      table.appendChild(h("thead", null, h("tr", null, h("th", null, t("pw.storedAs")), h("th", { class: "num" }, t("pw.rate")),
        h("th", { class: "num" }, t("pw.time")))));
      var tb = h("tbody");
      SPEEDS.forEach(function (sp) {
        // On average the attacker finds it halfway through the search space.
        var secs = common ? 0 : Math.pow(2, e.bits - 1) / sp.rate;
        tb.appendChild(h("tr", null, h("td", null, t("pw.hash." + sp.id)), h("td", { class: "num" }, M.fmt(sp.rate, 0) + "/s"),
          h("td", { class: "num" }, duration(secs))));
      });
      table.appendChild(tb);
    }
    update();
  });

  M.register("passwords/pbkdf2", function (stage) {
    var subtle = window.crypto && window.crypto.subtle;
    var iters = M.slider({ label: t("pw.iterations"), min: 1000, max: 2000000, log: true, value: 600000, onInput: function () {},
      format: function (v) { return M.fmt(Math.round(v), 0); } });
    var go = M.button(t("pw.derive"), run, "primary");
    var time = M.stat(t("pw.took"), "accent"), rate = M.stat(t("pw.yourRate"));
    var out = h("p", { class: "cipher-out mono small" });
    stage.appendChild(M.controls(iters.el, go));
    stage.appendChild(h("div", { class: "stats" }, time.el, rate.el));
    stage.appendChild(out);
    time.set("—"); rate.set("—");

    function run() {
      if (!subtle) { out.textContent = t("pw.noWebCrypto"); return; }
      var n = Math.round(iters.get()), enc = new TextEncoder();
      go.disabled = true;
      out.textContent = t("pw.working");
      var salt = crypto.getRandomValues(new Uint8Array(16)), t0;
      subtle.importKey("raw", enc.encode("correct horse battery staple"), "PBKDF2", false, ["deriveBits"]).then(function (k) {
        t0 = performance.now();
        return subtle.deriveBits({ name: "PBKDF2", hash: "SHA-256", salt: salt, iterations: n }, k, 256);
      }).then(function (bitsBuf) {
        var ms = performance.now() - t0;
        go.disabled = false;
        time.set(M.fmt(ms, 0) + " ms");
        rate.set(M.fmt(1000 / ms, 1) + "/s");
        var hex = Array.prototype.map.call(new Uint8Array(bitsBuf), function (b) { return (b < 16 ? "0" : "") + b.toString(16); }).join("");
        out.textContent = t("pw.result", { n: M.fmt(n, 0) }) + " " + hex;
      }, function (e) { go.disabled = false; out.textContent = String(e); });
    }
  });
})();
