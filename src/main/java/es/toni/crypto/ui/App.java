package es.toni.crypto.ui;

import javax.swing.SwingUtilities;

import es.toni.crypto.core.Messages;

/** Starts the desktop app. */
public final class App {

    private App() {
    }

    public static void start() {
        System.setProperty("flatlaf.uiScale.allowScaleDown", "true");
        Messages.setLocale(Prefs.locale());
        SwingUtilities.invokeLater(() -> {
            Ui.installLaf(Prefs.dark());
            new MainFrame().setVisible(true);
        });
    }
}
