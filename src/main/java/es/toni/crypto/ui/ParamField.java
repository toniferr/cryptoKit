package es.toni.crypto.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.formdev.flatlaf.FlatClientProperties;

import es.toni.crypto.core.Codec;
import es.toni.crypto.core.Format;
import es.toni.crypto.core.Messages;
import es.toni.crypto.core.Param;
import es.toni.crypto.core.Rand;

/** The label and editor of one algorithm parameter, built from its {@link Param} description. */
final class ParamField {

    final Param param;
    final JLabel label;
    final JComponent row;
    private JTextComponent text;
    private JComboBox<String> choice;
    private JComboBox<Format> format;

    ParamField(Param p) {
        this.param = p;
        this.label = new JLabel(Messages.get(p.labelKey()));
        String hint = Messages.getOr(p.labelKey() + ".hint", "");
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        switch (p.kind()) {
            case CHOICE -> {
                choice = new JComboBox<>(p.choices().toArray(String[]::new));
                choice.setSelectedItem(p.defaultValue());
                panel.add(choice, BorderLayout.WEST);
            }
            case MULTILINE -> {
                JTextArea area = new JTextArea(4, 40);
                area.setFont(Ui.mono(11.5f));
                area.setLineWrap(true);
                area.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, hint);
                text = area;
                JScrollPane sp = new JScrollPane(area);
                sp.setMinimumSize(new java.awt.Dimension(120, 76));
                sp.setPreferredSize(new java.awt.Dimension(400, 76));
                panel.add(sp, BorderLayout.CENTER);
            }
            case PASSWORD -> {
                JPasswordField pw = new JPasswordField();
                pw.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
                pw.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, hint);
                text = pw;
                panel.add(pw, BorderLayout.CENTER);
            }
            default -> {
                JTextField f = new JTextField(p.defaultValue());
                f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, hint);
                if (p.kind() == Param.Kind.BYTES) {
                    f.setFont(Ui.mono(12.5f));
                }
                if (p.kind() == Param.Kind.NUMBER) {
                    f.setColumns(10);
                    panel.add(f, BorderLayout.WEST);
                } else {
                    panel.add(f, BorderLayout.CENTER);
                }
                text = f;
            }
        }
        if (p.kind() == Param.Kind.BYTES) {
            JPanel extras = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            extras.setOpaque(false);
            format = formatCombo();
            format.setSelectedItem(p.format());
            extras.add(format);
            if (p.randomBytes() > 0) {
                JButton dice = new JButton(Ui.icon("dice", 16));
                dice.setToolTipText(Messages.get("ui.random", p.randomBytes()));
                dice.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
                dice.addActionListener(e -> {
                    format.setSelectedItem(Format.HEX);
                    text.setText(Codec.hex(Rand.bytes(p.randomBytes())));
                });
                extras.add(dice);
            }
            panel.add(extras, BorderLayout.EAST);
        }
        if (p.kind() == Param.Kind.NUMBER && !hint.isEmpty()) {
            JLabel h = new JLabel(hint);
            h.setForeground(Ui.muted());
            panel.add(h, BorderLayout.CENTER);
        }
        this.row = panel;
        label.setLabelFor(text != null ? text : choice);
        if (!hint.isEmpty()) {
            label.setToolTipText(hint);
        }
    }

    static JComboBox<Format> formatCombo() {
        JComboBox<Format> c = new JComboBox<>(Format.values());
        c.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                   boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Format f) {
                    setText(Messages.get(f.key()));
                }
                return this;
            }
        });
        c.setToolTipText(Messages.get("ui.format.tip"));
        return c;
    }

    String value() {
        if (choice != null) {
            return String.valueOf(choice.getSelectedItem());
        }
        if (text instanceof JPasswordField pw) {
            return new String(pw.getPassword());
        }
        return text.getText();
    }

    void value(String v) {
        if (choice != null) {
            choice.setSelectedItem(v);
        } else {
            text.setText(v);
            text.setCaretPosition(0);
        }
    }

    Format format() {
        return format == null ? Format.TEXT : (Format) format.getSelectedItem();
    }

    void format(Format f) {
        if (format != null) {
            format.setSelectedItem(f);
        }
    }

    boolean multiline() {
        return param.kind() == Param.Kind.MULTILINE;
    }
}
