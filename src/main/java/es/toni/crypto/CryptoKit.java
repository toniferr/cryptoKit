package es.toni.crypto;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import es.toni.crypto.cli.Cli;
import es.toni.crypto.ui.App;

/**
 * cryptoKit: with no arguments it opens the desktop app; with arguments it is a command-line tool
 * ({@code java -jar cryptoKit.jar --help}).
 *
 * @author Toni Ferreiro
 */
public final class CryptoKit {

    public static final String PORTAL = "https://toniferr.github.io/cryptoKit/";
    public static final String REPOSITORY = "https://github.com/toniferr/cryptoKit";

    private CryptoKit() {
    }

    public static String version() {
        try (InputStream in = CryptoKit.class.getResourceAsStream("version.properties")) {
            Properties p = new Properties();
            if (in != null) {
                p.load(in);
            }
            String v = p.getProperty("version", "dev");
            return v.startsWith("${") ? "dev" : v;
        } catch (IOException e) {
            return "dev";
        }
    }

    public static void main(String[] args) {
        if (args.length > 0 || GraphicsEnvironment.isHeadless()) {
            System.exit(Cli.run(args.length > 0 ? args : new String[] {"--help"}, System.in, System.out, System.err));
        }
        App.start();
    }
}
