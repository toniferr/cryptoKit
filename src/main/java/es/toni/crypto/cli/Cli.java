package es.toni.crypto.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.toni.crypto.CryptoKit;
import es.toni.crypto.core.Algorithm;
import es.toni.crypto.core.Algorithms;
import es.toni.crypto.core.CryptoException;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Family;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Kit;
import es.toni.crypto.core.Messages;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Result;
import es.toni.crypto.core.Values;

/**
 * The command line. Examples:
 * <pre>
 *   cryptokit list
 *   cryptokit sha256 hash "hello"
 *   cryptokit aes-gcm encrypt --key=00112233445566778899aabbccddeeff --key-format=hex "secret"
 *   echo -n "secret" | cryptokit argon2id hash
 *   cryptokit keygen ed25519
 *   cryptokit ed25519 sign --private=@key.pem "message"
 * </pre>
 * Exit status: 0 success, 1 a verification failed, 2 a usage or crypto error.
 */
public final class Cli {

    private Cli() {
    }

    public static int run(String[] argv, InputStream stdin, PrintStream out, PrintStream err) {
        List<String> args = new ArrayList<>(List.of(argv));
        // --lang is global and can go anywhere.
        for (int i = 0; i < args.size(); i++) {
            String a = args.get(i);
            if (a.startsWith("--lang=") || (a.equals("--lang") && i + 1 < args.size())) {
                String v = a.contains("=") ? a.substring(7) : args.remove(i + 1);
                args.remove(i);
                Messages.setLocale(Locale.forLanguageTag(v));
                break;
            }
        }
        try {
            if (args.isEmpty() || args.get(0).equals("--help") || args.get(0).equals("-h") || args.get(0).equals("help")) {
                out.println(Messages.get("cli.help", CryptoKit.version()));
                return 0;
            }
            switch (args.get(0)) {
                case "--version", "-v", "version" -> {
                    out.println("cryptoKit " + CryptoKit.version());
                    return 0;
                }
                case "list" -> {
                    list(out);
                    return 0;
                }
                case "info" -> {
                    info(algorithm(args, 1), out);
                    return 0;
                }
                case "keygen" -> {
                    Algorithm a = algorithm(args, 1);
                    if (!a.hasKeyPair()) {
                        throw new CryptoException("error.nokeypair");
                    }
                    Algorithm.KeyPairPem kp = a.generateKeyPair();
                    out.print(kp.publicPem());
                    out.print(kp.privatePem());
                    return 0;
                }
                default -> {
                    return operate(args, stdin, out, err);
                }
            }
        } catch (CryptoException e) {
            err.println(Messages.get("cli.error", e.getLocalizedMessage()));
            return 2;
        }
    }

    private static Algorithm algorithm(List<String> args, int i) throws CryptoException {
        if (args.size() <= i) {
            throw new CryptoException("cli.missingAlgorithm");
        }
        return Algorithms.byId(args.get(i)).orElseThrow(() -> new CryptoException("cli.unknownAlgorithm", args.get(i)));
    }

    private static void list(PrintStream out) {
        for (Family f : Family.values()) {
            out.println(Messages.get(f.key()));
            for (Algorithm a : Algorithms.of(f)) {
                out.printf("  %-18s %-24s %s%n", a.id(), a.name(), Messages.get(a.security().key()));
            }
        }
    }

    private static void info(Algorithm a, PrintStream out) {
        out.println(a.name() + "  (" + a.id() + ")  ·  " + Messages.get(a.family().key()) + "  ·  "
            + Messages.get(a.security().key()));
        out.println(a.description());
        out.println(Messages.get("cli.verbs", verbWord(a, Direction.FORWARD), verbWord(a, Direction.BACKWARD)));
        for (Param p : a.params()) {
            String when = p.when().size() == 2 ? "" : " [" + verbWord(a, p.when().iterator().next()) + "]";
            String def = p.defaultValue().isEmpty() ? "" : " = " + p.defaultValue();
            out.printf("  --%-12s %s%s%s%n", p.id(), Messages.get(p.labelKey()), def, when);
        }
        if (a.hasKeyPair()) {
            out.println(Messages.get("cli.keygenHint", a.id()));
        }
    }

    /** The English command word of a direction, e.g. "encrypt" (the CLI always accepts the English verbs). */
    private static String verbWord(Algorithm a, Direction d) {
        return a.verb(d).substring("verb.".length());
    }

    private static int operate(List<String> args, InputStream stdin, PrintStream out, PrintStream err)
        throws CryptoException {
        Algorithm a = algorithm(args, 0);
        if (args.size() < 2) {
            throw new CryptoException("cli.missingVerb", verbWord(a, Direction.FORWARD), verbWord(a, Direction.BACKWARD));
        }
        String verb = args.get(1);
        Direction d;
        if (verb.equals(verbWord(a, Direction.FORWARD)) || verb.equals("forward")) {
            d = Direction.FORWARD;
        } else if (verb.equals(verbWord(a, Direction.BACKWARD)) || verb.equals("backward")) {
            d = Direction.BACKWARD;
        } else {
            throw new CryptoException("cli.missingVerb", verbWord(a, Direction.FORWARD), verbWord(a, Direction.BACKWARD));
        }

        Values values = new Values();
        Format in = a.inputFormat(d);
        Format outFormat = a.outputFormat(d);
        String message = null;
        for (int i = 2; i < args.size(); i++) {
            String arg = args.get(i);
            if (arg.startsWith("--")) {
                String name;
                String value;
                int eq = arg.indexOf('=');
                if (eq > 0) {
                    name = arg.substring(2, eq);
                    value = arg.substring(eq + 1);
                } else {
                    name = arg.substring(2);
                    if (i + 1 >= args.size()) {
                        throw new CryptoException("cli.missingValue", arg);
                    }
                    value = args.get(++i);
                }
                if (value.startsWith("@")) {
                    value = readFile(value.substring(1));
                }
                switch (name) {
                    case "in" -> in = Format.parse(value);
                    case "out" -> outFormat = Format.parse(value);
                    default -> {
                        if (name.endsWith("-format")) {
                            values.format(name.substring(0, name.length() - 7), Format.parse(value));
                        } else if (a.params().stream().anyMatch(p -> p.id().equals(name))) {
                            values.set(name, value);
                        } else {
                            throw new CryptoException("cli.unknownOption", arg, a.id());
                        }
                    }
                }
            } else if (message == null) {
                message = arg;
            } else {
                throw new CryptoException("cli.tooMany");
            }
        }
        if (message == null || message.equals("-")) {
            try {
                message = new String(stdin.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new CryptoException("cli.stdin");
            }
        }

        Kit.Outcome o = Kit.run(a, d, message, in, outFormat, values);
        for (Result.Note n : o.result().notes()) {
            err.println("· " + n.text());
        }
        if (o.verified() != null) {
            out.println(Messages.get(o.verified() ? "verdict.valid" : "verdict.invalid"));
            return o.verified() ? 0 : 1;
        }
        out.println(o.text());
        return 0;
    }

    private static String readFile(String path) throws CryptoException {
        try {
            return Files.readString(Path.of(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CryptoException("cli.file", path);
        }
    }
}
