package es.toni.crypto.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGUtils;

import es.toni.crypto.CryptoKit;
import es.toni.crypto.core.Messages;

/** The main window: menu bar plus a {@link MainPanel}. */
final class MainFrame extends JFrame {

    private MainPanel panel;

    MainFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIconImages(FlatSVGUtils.createWindowIconImages("/es/toni/crypto/ui/icons/app.svg"));
        build();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1200, screen.width - 80), Math.min(800, screen.height - 80));
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(null);
    }

    private void build() {
        setTitle("cryptoKit " + CryptoKit.version());
        panel = new MainPanel();
        setContentPane(panel);
        setJMenuBar(menu());
    }

    private JMenuBar menu() {
        int mod = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu(Messages.get("menu.file"));
        JMenuItem quit = new JMenuItem(Messages.get("menu.quit"));
        quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, mod));
        quit.addActionListener(e -> dispose());
        file.add(quit);
        bar.add(file);

        JMenu view = new JMenu(Messages.get("menu.view"));
        ButtonGroup themes = new ButtonGroup();
        JRadioButtonMenuItem light = new JRadioButtonMenuItem(Messages.get("menu.light"), !Ui.isDark());
        JRadioButtonMenuItem dark = new JRadioButtonMenuItem(Messages.get("menu.dark"), Ui.isDark());
        light.addActionListener(e -> theme(false));
        dark.addActionListener(e -> theme(true));
        themes.add(light);
        themes.add(dark);
        view.add(light);
        view.add(dark);
        view.addSeparator();
        ButtonGroup langs = new ButtonGroup();
        JRadioButtonMenuItem en = new JRadioButtonMenuItem("English", !Messages.spanish());
        JRadioButtonMenuItem es = new JRadioButtonMenuItem("Español", Messages.spanish());
        en.addActionListener(e -> language(Messages.ENGLISH));
        es.addActionListener(e -> language(Messages.SPANISH));
        langs.add(en);
        langs.add(es);
        view.add(en);
        view.add(es);
        bar.add(view);

        JMenu help = new JMenu(Messages.get("menu.help"));
        JMenuItem portal = new JMenuItem(Messages.get("menu.portal"), Ui.icon("book", 16));
        portal.addActionListener(e -> Ui.browse(CryptoKit.PORTAL + (Messages.spanish() ? "es/" : "")));
        JMenuItem algorithm = new JMenuItem(Messages.get("menu.algorithm"));
        algorithm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        algorithm.addActionListener(e -> Ui.browse(Ui.portalUrl(panel.current().chapter())));
        JMenuItem changes = new JMenuItem(Messages.get("menu.changelog"));
        changes.addActionListener(e -> Ui.browse(CryptoKit.REPOSITORY + "/blob/main/CHANGELOG.md"));
        JMenuItem about = new JMenuItem(Messages.get("menu.about"));
        about.addActionListener(e -> about());
        help.add(portal);
        help.add(algorithm);
        help.add(changes);
        help.addSeparator();
        help.add(about);
        bar.add(help);
        return bar;
    }

    private void theme(boolean dark) {
        if (dark == Ui.isDark()) {
            return;
        }
        Prefs.dark(dark);
        FlatAnimatedLafChange.showSnapshot();
        Ui.installLaf(dark);
        FlatLaf.updateUI();
        rebuild();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    private void language(Locale l) {
        Messages.setLocale(l);
        Prefs.locale(l);
        rebuild();
    }

    /** Rebuilds the window content (after a change of language or theme), keeping the algorithm selected. */
    private void rebuild() {
        Prefs.algorithm(panel.current().id());
        build();
        revalidate();
        repaint();
    }

    private void about() {
        String html = "<html><body style='width:340px'>"
            + "<h2 style='margin:0'>cryptoKit " + CryptoKit.version() + "</h2>"
            + "<p>" + Messages.get("about.tagline") + "</p><br>"
            + "<p>" + Messages.get("about.license") + "</p><br>"
            + "<p>Java " + System.getProperty("java.version") + " · Bouncy Castle " + MainPanel.bcVersion()
            + " · FlatLaf " + String.valueOf(FlatLaf.class.getPackage().getImplementationVersion()).replace("null", "3")
            + "</p><br>"
            + "<p>" + CryptoKit.PORTAL + "<br>" + CryptoKit.REPOSITORY + "</p></body></html>";
        JOptionPane.showMessageDialog(this, html, Messages.get("menu.about"), JOptionPane.INFORMATION_MESSAGE,
            Ui.icon("app", 64));
    }

    static void open() {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
