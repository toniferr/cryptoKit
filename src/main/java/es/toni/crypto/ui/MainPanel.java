package es.toni.crypto.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import com.formdev.flatlaf.FlatClientProperties;

import es.toni.crypto.CryptoKit;
import es.toni.crypto.core.Algorithm;
import es.toni.crypto.core.Algorithms;
import es.toni.crypto.core.Codec;
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
 * The whole window content: algorithm list on the left; on the right the algorithm's description, its parameters,
 * the input, the operation buttons and the output. It can also be painted off screen (for the screenshots).
 */
public final class MainPanel extends JPanel {

    private final Sidebar sidebar;
    private final JLabel title = new JLabel();
    private final JLabel family = new JLabel();
    private final Badge badge = new Badge();
    private final JTextArea description = new JTextArea();
    private final JButton learn = new JButton();
    private final JPanel params = new JPanel(new GridBagLayout());
    private final JButton keyPair = new JButton();
    private final Map<String, ParamField> fields = new LinkedHashMap<>();
    /** Values typed for each parameter id, kept when switching algorithms (a key typed for AES-GCM stays for AES-CTR). */
    private final Map<String, String> remembered = new HashMap<>();
    private final Map<String, Format> rememberedFormat = new HashMap<>();

    private final JToggleButton forward = new JToggleButton();
    private final JToggleButton backward = new JToggleButton();
    private final JButton run = new JButton();
    private final JProgressBar busy = new JProgressBar();
    private final JComboBox<Format> inFormat = ParamField.formatCombo();
    private final JComboBox<Format> outFormat = ParamField.formatCombo();
    private final JTextArea input = new JTextArea();
    private final JTextArea output = new JTextArea();
    private final JLabel banner = new JLabel();
    private final JLabel notes = new JLabel();
    private final JLabel status = new JLabel();
    private final JButton swap = new JButton();

    private Algorithm current;
    private Direction direction = Direction.FORWARD;
    private SwingWorker<?, ?> worker;

    public MainPanel() {
        super(new BorderLayout());
        sidebar = new Sidebar(this::select);
        add(sidebar, BorderLayout.WEST);
        add(workspace(), BorderLayout.CENTER);
        add(statusBar(), BorderLayout.SOUTH);
        bindKeys();
        select(Algorithms.byId(Prefs.algorithm()).orElse(Algorithms.all().get(0)));
    }

    // ------------------------------------------------------------------ layout

    private JComponent workspace() {
        JPanel w = new JPanel(new GridBagLayout());
        w.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        // Title, badge and family
        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.X_AXIS));
        title.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
        family.setForeground(Ui.muted());
        head.add(title);
        head.add(Box.createHorizontalStrut(12));
        head.add(badge);
        head.add(Box.createHorizontalStrut(12));
        head.add(family);
        head.add(Box.createHorizontalGlue());
        learn.setText(Messages.get("ui.learn"));
        learn.setIcon(Ui.icon("book", 16));
        learn.setHorizontalTextPosition(JButton.LEADING);
        learn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        learn.addActionListener(e -> Ui.browse(Ui.portalUrl(current.chapter())));
        head.add(learn);
        c.gridy = 0;
        w.add(head, c);

        description.setEditable(false);
        description.setFocusable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setOpaque(false);
        description.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
        description.setFont(new JLabel().getFont());
        c.gridy = 1;
        w.add(description, c);

        // Parameters
        params.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        keyPair.setText(Messages.get("ui.keypair"));
        keyPair.setIcon(Ui.icon("key", 16));
        keyPair.addActionListener(e -> generateKeyPair());
        c.gridy = 2;
        w.add(params, c);

        // Operation bar
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        ButtonGroup g = new ButtonGroup();
        g.add(forward);
        g.add(backward);
        for (JToggleButton b : List.of(forward, backward)) {
            b.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TAB);
            b.putClientProperty(FlatClientProperties.STYLE, "tab.selectedBackground: $Component.accentColor;"
                + "tab.selectedForeground: #fff; arc: 8; margin: 4,12,4,12");
        }
        forward.addActionListener(e -> direction(Direction.FORWARD));
        backward.addActionListener(e -> direction(Direction.BACKWARD));
        run.putClientProperty(FlatClientProperties.BUTTON_TYPE, "default");
        run.putClientProperty(FlatClientProperties.STYLE, "font: bold; margin: 6,18,6,18;"
            + "background: $Component.accentColor; foreground: #fff; borderWidth: 0; focusWidth: 0");
        run.setToolTipText("Ctrl+Enter");
        run.addActionListener(e -> runOperation());
        busy.setIndeterminate(true);
        busy.setVisible(false);
        busy.setPreferredSize(new Dimension(90, 6));
        bar.add(forward);
        bar.add(backward);
        bar.add(Box.createHorizontalStrut(14));
        bar.add(run);
        bar.add(busy);
        c.gridy = 3;
        c.insets = new Insets(4, -6, 8, 0);
        w.add(bar, c);
        c.insets = new Insets(0, 0, 0, 0);

        // Input
        c.gridy = 4;
        w.add(ioHeader("ui.input", inFormat, toolButton("open", "ui.open", e -> openFile()),
            toolButton("paste", "ui.paste", e -> paste()), toolButton("clear", "ui.clear", e -> {
                input.setText("");
                clearOutput();
            })), c);
        input.setFont(Ui.mono(13));
        input.setLineWrap(true);
        input.setRows(5);
        input.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Messages.get("ui.input.hint"));
        c.gridy = 5;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        w.add(new JScrollPane(input), c);
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Output
        swap.setIcon(Ui.icon("swap", 16));
        swap.setToolTipText(Messages.get("ui.swap.tip"));
        swap.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        swap.addActionListener(e -> swap());
        c.gridy = 6;
        c.insets = new Insets(10, 0, 0, 0);
        w.add(ioHeader("ui.output", outFormat, toolButton("copy", "ui.copy", e -> copy()),
            toolButton("save", "ui.save", e -> saveFile()), swap), c);
        c.insets = new Insets(0, 0, 0, 0);
        banner.setOpaque(true);
        banner.setVisible(false);
        banner.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        banner.setFont(banner.getFont().deriveFont(Font.BOLD));
        c.gridy = 7;
        w.add(banner, c);
        output.setFont(Ui.mono(13));
        output.setLineWrap(true);
        output.setEditable(false);
        output.setRows(5);
        c.gridy = 8;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        w.add(new JScrollPane(output), c);
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        notes.setForeground(Ui.muted());
        notes.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 0));
        c.gridy = 9;
        w.add(notes, c);

        outFormat.addActionListener(e -> reformatOutput());
        return w;
    }

    private JPanel ioHeader(String key, JComboBox<Format> format, JComponent... tools) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        JLabel l = new JLabel(Messages.get(key));
        l.putClientProperty(FlatClientProperties.STYLE_CLASS, "h4");
        p.add(l);
        p.add(Box.createHorizontalStrut(10));
        format.setMaximumSize(format.getPreferredSize());
        p.add(format);
        p.add(Box.createHorizontalGlue());
        for (JComponent t : tools) {
            p.add(t);
        }
        return p;
    }

    private static JButton toolButton(String icon, String tipKey, java.awt.event.ActionListener action) {
        JButton b = new JButton(Ui.icon(icon, 16));
        b.setToolTipText(Messages.get(tipKey));
        b.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        b.addActionListener(action);
        return b;
    }

    private JComponent statusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, javax.swing.UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        status.setText(Messages.get("ui.ready"));
        p.add(status, BorderLayout.CENTER);
        JLabel about = new JLabel("cryptoKit " + CryptoKit.version() + " · Java " + Runtime.version().feature()
            + " · Bouncy Castle " + bcVersion());
        about.setForeground(Ui.muted());
        p.add(about, BorderLayout.EAST);
        return p;
    }

    static String bcVersion() {
        Package p = org.bouncycastle.crypto.Digest.class.getPackage();
        String v = p == null ? null : p.getImplementationVersion();
        return v == null ? "1.86" : v;
    }

    private void bindKeys() {
        int menu = java.awt.GraphicsEnvironment.isHeadless() ? java.awt.event.InputEvent.CTRL_DOWN_MASK
            : Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, menu), "run");
        getActionMap().put("run", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                runOperation();
            }
        });
    }

    // ------------------------------------------------------------------ algorithm and direction

    public void select(Algorithm a) {
        remember();
        current = a;
        Prefs.algorithm(a.id());
        sidebar.select(a, false);
        title.setText(a.name());
        badge.set(a.security());
        family.setText(Messages.get(a.family().key()));
        title.setMinimumSize(title.getPreferredSize());
        family.setMinimumSize(family.getPreferredSize());
        description.setText(a.description());
        learn.setToolTipText(Ui.portalUrl(a.chapter()));
        forward.setText(Ui.capital(Messages.get(a.verb(Direction.FORWARD))));
        backward.setText(Ui.capital(Messages.get(a.verb(Direction.BACKWARD))));
        forward.setIcon(verbIcon(a.verb(Direction.FORWARD)));
        backward.setIcon(verbIcon(a.verb(Direction.BACKWARD)));

        fields.clear();
        for (Param p : a.params()) {
            ParamField f = new ParamField(p);
            if (remembered.containsKey(p.id()) && !p.kind().equals(Param.Kind.CHOICE)) {
                f.value(remembered.get(p.id()));
                f.format(rememberedFormat.getOrDefault(p.id(), p.format()));
            }
            fields.put(p.id(), f);
        }
        direction(Direction.FORWARD);
        clearOutput();
        setStatus(Messages.get("ui.ready"), null);
    }

    private static Icon verbIcon(String verb) {
        return Ui.icon(switch (verb) {
            case "verb.encrypt" -> "lock";
            case "verb.decrypt" -> "unlock";
            case "verb.encode", "verb.decode" -> "code";
            case "verb.hash", "verb.mac" -> "hash";
            case "verb.sign" -> "pen";
            default -> "check";
        }, 16);
    }

    public void direction(Direction d) {
        direction = d;
        (d == Direction.FORWARD ? forward : backward).setSelected(true);
        run.setText(Ui.capital(Messages.get(current.verb(d))) + "  ▸");
        inFormat.setSelectedItem(current.inputFormat(d));
        outFormat.setSelectedItem(current.outputFormat(d));
        swap.setEnabled(d == Direction.FORWARD || !isVerification());
        layoutParams();
    }

    private boolean isVerification() {
        Family f = current.family();
        return f == Family.HASH || f == Family.MAC || f == Family.PASSWORD || f == Family.SIGNATURE;
    }

    private void layoutParams() {
        params.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 0, 3, 10);
        int y = 0;
        for (ParamField f : fields.values()) {
            // Key pairs stay visible in both directions: they are generated and read together.
            if (!f.param.usedIn(direction) && !f.multiline()) {
                continue;
            }
            c.gridy = y++;
            c.gridx = 0;
            c.weightx = 0;
            c.anchor = f.multiline() ? GridBagConstraints.NORTHEAST : GridBagConstraints.EAST;
            c.fill = GridBagConstraints.NONE;
            f.label.setEnabled(f.param.usedIn(direction));
            params.add(f.label, c);
            c.gridx = 1;
            c.weightx = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            params.add(f.row, c);
        }
        if (current.hasKeyPair()) {
            c.gridy = y;
            c.gridx = 1;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.WEST;
            params.add(keyPair, c);
        }
        params.setVisible(y > 0 || current.hasKeyPair());
        params.revalidate();
        params.repaint();
    }

    private void remember() {
        for (ParamField f : fields.values()) {
            remembered.put(f.param.id(), f.value());
            rememberedFormat.put(f.param.id(), f.format());
        }
    }

    // ------------------------------------------------------------------ running

    private Values values() {
        Values v = new Values();
        for (ParamField f : fields.values()) {
            v.set(f.param.id(), f.value(), f.format());
        }
        return v;
    }

    private byte[] lastOutput;

    public void runOperation() {
        if (worker != null && !worker.isDone()) {
            return;
        }
        Algorithm a = current;
        Direction d = direction;
        String text = input.getText();
        Format in = (Format) inFormat.getSelectedItem();
        Format out = (Format) outFormat.getSelectedItem();
        Values v = values();
        setBusy(true);
        worker = new SwingWorker<Kit.Outcome, Void>() {
            @Override
            protected Kit.Outcome doInBackground() throws CryptoException {
                return Kit.run(a, d, text, in, out, v);
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    show(get());
                } catch (ExecutionException e) {
                    showError(e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        worker.execute();
    }

    /** Runs synchronously on the calling thread (used by the screenshot tool and tests). */
    public void runNow() {
        try {
            show(Kit.run(current, direction, input.getText(), (Format) inFormat.getSelectedItem(),
                (Format) outFormat.getSelectedItem(), values()));
        } catch (CryptoException e) {
            showError(e);
        }
    }

    private void show(Kit.Outcome o) {
        lastOutput = o.result().data();
        silentFormat(o.format());
        if (o.verified() != null) {
            banner(o.verified() ? Ui.good() : Ui.bad(), Messages.get(o.verified() ? "verdict.valid" : "verdict.invalid"),
                o.verified() ? "check" : "cross");
            // For digests and MACs the recomputed value is worth seeing; for the rest the verdict says it all.
            boolean showValue = current.family() == Family.HASH || current.family() == Family.MAC;
            output.setText(showValue ? o.text() : "");
            if (!showValue) {
                lastOutput = null;
            }
        } else {
            banner.setVisible(false);
            output.setText(o.text());
        }
        output.setCaretPosition(0);
        List<String> ns = new ArrayList<>();
        for (Result.Note n : o.result().notes()) {
            ns.add(escape(n.text()));
        }
        notes.setText(ns.isEmpty() ? "" : "<html>• " + String.join("<br>• ", ns) + "</html>");
        String time = o.nanos() < 1_000_000 ? String.format("%.2f ms", o.nanos() / 1e6)
            : String.format("%,d ms", o.nanos() / 1_000_000);
        setStatus(Messages.get("ui.done", Ui.capital(Messages.get(current.verb(direction))), o.inBytes(),
            o.result().data().length, time), Ui.good());
    }

    private void showError(Throwable t) {
        String msg = t instanceof CryptoException ce ? ce.getLocalizedMessage() : String.valueOf(t);
        banner(Ui.bad(), msg, "cross");
        output.setText("");
        notes.setText("");
        lastOutput = null;
        setStatus(msg, Ui.bad());
    }

    private void banner(Color color, String text, String icon) {
        banner.setText(text);
        banner.setIcon(Ui.icon(icon, 16));
        banner.setForeground(color);
        banner.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        banner.setVisible(true);
    }

    private boolean formatting;

    private void silentFormat(Format f) {
        formatting = true;
        outFormat.setSelectedItem(f);
        formatting = false;
    }

    /** Changing the output format re-encodes the last result instead of running again. */
    private void reformatOutput() {
        if (formatting || lastOutput == null || banner.isVisible() && output.getText().isEmpty()) {
            return;
        }
        Format f = (Format) outFormat.getSelectedItem();
        if (f == Format.TEXT && !Codec.isUtf8(lastOutput)) {
            silentFormat(Format.HEX);
            f = Format.HEX;
            setStatus(Messages.get("note.notText"), Ui.color(es.toni.crypto.core.Security.LEGACY));
        }
        output.setText(f.encode(lastOutput));
        output.setCaretPosition(0);
    }

    private void clearOutput() {
        output.setText("");
        notes.setText("");
        banner.setVisible(false);
        lastOutput = null;
    }

    private void setBusy(boolean on) {
        busy.setVisible(on);
        run.setEnabled(!on);
        keyPair.setEnabled(!on);
        if (on) {
            setStatus(Messages.get("ui.working"), null);
        }
    }

    private void setStatus(String text, Color color) {
        status.setText(text);
        status.setForeground(color == null ? javax.swing.UIManager.getColor("Label.foreground") : color);
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ------------------------------------------------------------------ tools

    private void generateKeyPair() {
        Algorithm a = current;
        setBusy(true);
        new SwingWorker<Algorithm.KeyPairPem, Void>() {
            @Override
            protected Algorithm.KeyPairPem doInBackground() throws CryptoException {
                return a.generateKeyPair();
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    Algorithm.KeyPairPem kp = get();
                    if (a == current) {
                        fields.get("public").value(kp.publicPem());
                        fields.get("private").value(kp.privatePem());
                        setStatus(Messages.get("ui.keypair.done", a.name()), Ui.good());
                    }
                } catch (ExecutionException e) {
                    showError(e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    /** Generates a key pair synchronously (screenshot tool). */
    public void generateKeyPairNow() throws CryptoException {
        Algorithm.KeyPairPem kp = current.generateKeyPair();
        fields.get("public").value(kp.publicPem());
        fields.get("private").value(kp.privatePem());
    }

    /**
     * "Use the result": a ciphertext goes back to the input to be decrypted; a digest, MAC, password hash or
     * signature goes to the field that verification needs, keeping the message.
     */
    private void swap() {
        if (lastOutput == null && output.getText().isEmpty()) {
            return;
        }
        Format f = (Format) outFormat.getSelectedItem();
        String text = output.getText();
        if (isVerification() && direction == Direction.FORWARD) {
            String target = switch (current.family()) {
                case PASSWORD -> "encoded";
                case SIGNATURE -> "signature";
                default -> "expected";
            };
            direction(Direction.BACKWARD);
            ParamField pf = fields.get(target);
            pf.format(f);
            pf.value(text);
        } else {
            input.setText(text);
            input.setCaretPosition(0);
            direction(direction == Direction.FORWARD ? Direction.BACKWARD : Direction.FORWARD);
            inFormat.setSelectedItem(f);
        }
        clearOutput();
        setStatus(Messages.get("ui.swapped"), null);
    }

    private void copy() {
        if (!output.getText().isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(output.getText()), null);
            setStatus(Messages.get("ui.copied"), null);
        }
    }

    private void paste() {
        try {
            Object s = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            input.setText(String.valueOf(s));
        } catch (Exception e) {
            setStatus(Messages.get("ui.paste.empty"), null);
        }
    }

    private void openFile() {
        JFileChooser ch = new JFileChooser();
        if (ch.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = ch.getSelectedFile();
        try {
            if (file.length() > 16 * 1024 * 1024) {
                setStatus(Messages.get("ui.file.big"), Ui.bad());
                return;
            }
            byte[] b = Files.readAllBytes(file.toPath());
            if (Codec.isUtf8(b)) {
                inFormat.setSelectedItem(Format.TEXT);
                input.setText(Codec.utf8(b));
            } else {
                inFormat.setSelectedItem(Format.BASE64);
                input.setText(Codec.base64(b));
            }
            input.setCaretPosition(0);
            setStatus(Messages.get("ui.file.loaded", file.getName(), b.length), null);
        } catch (IOException e) {
            setStatus(Messages.get("ui.file.error", file.getName()), Ui.bad());
        }
    }

    private void saveFile() {
        if (lastOutput == null) {
            return;
        }
        JFileChooser ch = new JFileChooser();
        if (ch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = ch.getSelectedFile();
        try {
            Format f = (Format) outFormat.getSelectedItem();
            // Binary results are saved as bytes when shown as text; otherwise as the text on screen.
            Files.write(file.toPath(), f == Format.TEXT ? lastOutput : Codec.utf8(output.getText()));
            setStatus(Messages.get("ui.file.saved", file.getName()), null);
        } catch (IOException e) {
            setStatus(Messages.get("ui.file.error", file.getName()), Ui.bad());
        }
    }

    // ------------------------------------------------------------------ scripting (screenshots)

    public Algorithm current() {
        return current;
    }

    /** The swap button: use the result for the other direction. */
    public void useResult() {
        swap();
    }

    public void setInput(String text, Format f) {
        input.setText(text);
        inFormat.setSelectedItem(f);
    }

    public void setParam(String id, String value, Format f) {
        ParamField pf = fields.get(id);
        pf.value(value);
        pf.format(f);
    }
}
