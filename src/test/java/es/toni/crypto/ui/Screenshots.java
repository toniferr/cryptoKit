package es.toni.crypto.ui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import es.toni.crypto.core.Algorithms;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Messages;

/**
 * Paints the main window off screen in a few scenes, for the README and the portal. Needs a display (it is not a
 * test): {@code java -cp target/classes:target/test-classes:<deps> es.toni.crypto.ui.Screenshots <out dir>}.
 */
public final class Screenshots {

    private interface Scene {
        void setUp(MainPanel p) throws Exception;
    }

    public static void main(String[] args) throws Exception {
        File out = new File(args.length > 0 ? args[0] : "target/screenshots");
        out.mkdirs();
        shoot(out, "app-aes-gcm-en", false, Messages.ENGLISH, p -> {
            p.select(Algorithms.byId("aes-gcm").orElseThrow());
            p.setParam("key", "6b0f3d8a1c2e4f5a7b9c0d1e2f3a4b5c6d7e8f90a1b2c3d4e5f60718293a4b5c", Format.HEX);
            p.setInput("Attack at dawn. Bring the Enigma settings for tomorrow.", Format.TEXT);
            p.runNow();
        });
        shoot(out, "app-enigma-en", true, Messages.ENGLISH, p -> {
            p.select(Algorithms.byId("enigma").orElseThrow());
            p.setParam("positions", "B L A", Format.TEXT);
            p.setParam("plugboard", "AV BS CG DL FU HZ IN KM OW RX", Format.TEXT);
            p.setInput("WETTERVORHERSAGE BISKAYA", Format.TEXT);
            p.runNow();
        });
        shoot(out, "app-ml-kem-es", false, Messages.SPANISH, p -> {
            p.select(Algorithms.byId("ml-kem-768").orElseThrow());
            p.generateKeyPairNow();
            p.setInput("Hola desde el futuro poscuántico.", Format.TEXT);
            p.runNow();
        });
        shoot(out, "app-ed25519-es", true, Messages.SPANISH, p -> {
            p.select(Algorithms.byId("ed25519").orElseThrow());
            p.generateKeyPairNow();
            p.setInput("Te debo 10 euros. — Toni", Format.TEXT);
            p.runNow();
            p.useResult();
            p.runNow();
        });
        System.exit(0);
    }

    private static void shoot(File dir, String name, boolean dark, Locale locale, Scene scene) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                Messages.setLocale(locale);
                Ui.installLaf(dark);
                MainFrame f = new MainFrame();
                f.setSize(1180, 780);
                f.addNotify();
                f.validate();
                MainPanel p = (MainPanel) f.getContentPane();
                scene.setUp(p);
                f.validate();
                int scale = 2;
                BufferedImage img = new BufferedImage(f.getRootPane().getWidth() * scale,
                    f.getRootPane().getHeight() * scale, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.scale(scale, scale);
                f.getRootPane().paint(g);
                g.dispose();
                ImageIO.write(img, "png", new File(dir, name + ".png"));
                f.dispose();
                System.out.println("wrote " + name + ".png");
            } catch (Exception e) {
                throw new RuntimeException(name, e);
            }
        });
    }

    private Screenshots() {
    }
}
