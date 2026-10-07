package es.toni.crypto.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.net.URI;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;

import es.toni.crypto.CryptoKit;
import es.toni.crypto.core.Messages;
import es.toni.crypto.core.Security;

/** Look and feel, colours, fonts, icons and links shared by the windows of the app. */
final class Ui {

    /** Accent of the app and the portal: a deep teal. */
    static final String ACCENT = "#14857a";

    private Ui() {
    }

    static void installLaf(boolean dark) {
        FlatInterFont.install();
        FlatJetBrainsMonoFont.install();
        FlatLaf.setPreferredFontFamily(FlatInterFont.FAMILY);
        FlatLaf.setPreferredMonospacedFontFamily(FlatJetBrainsMonoFont.FAMILY);
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", ACCENT));
        if (dark) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("Tree.showDefaultIcons", false);
        UIManager.put("TitlePane.unifiedBackground", true);
    }

    static boolean isDark() {
        return FlatLaf.isLafDark();
    }

    static Font mono(float size) {
        return new Font(FlatJetBrainsMonoFont.FAMILY, Font.PLAIN, Math.round(size));
    }

    static FlatSVGIcon icon(String name, int size) {
        return new FlatSVGIcon("es/toni/crypto/ui/icons/" + name + ".svg", size, size);
    }

    static Color color(Security s) {
        boolean dark = isDark();
        return switch (s) {
            case RECOMMENDED -> dark ? new Color(0x4cc38a) : new Color(0x1f8a52);
            case LEGACY -> dark ? new Color(0xe0a63a) : new Color(0xb3740c);
            case BROKEN -> dark ? new Color(0xf0705f) : new Color(0xc23a2b);
            case EDUCATIONAL -> dark ? new Color(0x6ea8ff) : new Color(0x2f6fd0);
            case ENCODING -> dark ? new Color(0x9aa1ab) : new Color(0x6b7280);
        };
    }

    static Color good() {
        return color(Security.RECOMMENDED);
    }

    static Color bad() {
        return color(Security.BROKEN);
    }

    static Color muted() {
        return UIManager.getColor("Label.disabledForeground");
    }

    /** A small filled circle in the colour of a security level. */
    static Icon dot(Security s) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color(s));
                g2.fillOval(x + 2, y + 3, 8, 8);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 14;
            }
        };
    }

    /** The portal page for a chapter reference like "modes#gcm", in the current language. */
    static String portalUrl(String chapter) {
        String[] parts = chapter.split("#", 2);
        String slug = Messages.getOr("url." + parts[0], parts[0]);
        String base = CryptoKit.PORTAL + (Messages.spanish() ? "es/" : "");
        return base + slug + "/" + (parts.length > 1 ? "#" + parts[1] : "");
    }

    static void browse(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception e) {
            // No browser available: nothing sensible to do.
        }
    }

    /** "encrypt" -> "Encrypt". */
    static String capital(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
