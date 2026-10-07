package es.toni.crypto.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.formdev.flatlaf.FlatClientProperties;

import es.toni.crypto.core.Algorithm;
import es.toni.crypto.core.Algorithms;
import es.toni.crypto.core.Family;
import es.toni.crypto.core.Messages;
import es.toni.crypto.core.Security;

/** The list of algorithms, grouped by family, with a search box and a legend of the security colours. */
final class Sidebar extends JPanel {

    private final JTree tree = new JTree();
    private final JTextField search = new JTextField();
    private final Consumer<Algorithm> onSelect;
    private boolean silent;

    Sidebar(Consumer<Algorithm> onSelect) {
        super(new BorderLayout(0, 8));
        this.onSelect = onSelect;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 10, 6));
        setPreferredSize(new Dimension(250, 10));

        search.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
            Messages.get("ui.search", Algorithms.all().size()));
        search.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Ui.icon("search", 14));
        search.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                rebuild();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                rebuild();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                rebuild();
            }
        });
        add(search, BorderLayout.NORTH);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setRowHeight(24);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new Renderer());
        tree.addTreeSelectionListener(e -> {
            if (silent) {
                return;
            }
            Object node = tree.getLastSelectedPathComponent();
            if (node instanceof DefaultMutableTreeNode n && n.getUserObject() instanceof Algorithm a) {
                onSelect.accept(a);
            }
        });
        JScrollPane scroll = new JScrollPane(tree);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
        add(legend(), BorderLayout.SOUTH);
        rebuild();
    }

    private JPanel legend() {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 2));
        p.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 0));
        for (Security s : Security.values()) {
            JLabel l = new JLabel(Messages.get(s.key()), Ui.dot(s), JLabel.LEADING);
            l.setToolTipText(Messages.get(s.key() + ".hint"));
            l.putClientProperty(FlatClientProperties.STYLE_CLASS, "small");
            p.add(l);
        }
        return p;
    }

    private void rebuild() {
        String q = search.getText().strip().toLowerCase(Locale.ROOT);
        Algorithm selected = selected();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for (Family f : Family.values()) {
            DefaultMutableTreeNode fn = new DefaultMutableTreeNode(f);
            for (Algorithm a : Algorithms.of(f)) {
                if (q.isEmpty() || matches(a, q)) {
                    fn.add(new DefaultMutableTreeNode(a));
                }
            }
            if (fn.getChildCount() > 0) {
                root.add(fn);
            }
        }
        silent = true;
        tree.setModel(new DefaultTreeModel(root));
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        silent = false;
        if (selected != null) {
            select(selected, false);
        }
    }

    private static boolean matches(Algorithm a, String q) {
        return a.name().toLowerCase(Locale.ROOT).contains(q) || a.id().contains(q)
            || Messages.get(a.family().key()).toLowerCase().contains(q)
            || Messages.get(a.security().key()).toLowerCase().contains(q);
    }

    Algorithm selected() {
        Object node = tree.getLastSelectedPathComponent();
        return node instanceof DefaultMutableTreeNode n && n.getUserObject() instanceof Algorithm a ? a : null;
    }

    void select(Algorithm a, boolean notify) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode fn = (DefaultMutableTreeNode) root.getChildAt(i);
            for (int j = 0; j < fn.getChildCount(); j++) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) fn.getChildAt(j);
                if (n.getUserObject() == a) {
                    TreePath path = new TreePath(n.getPath());
                    silent = !notify;
                    tree.setSelectionPath(path);
                    silent = false;
                    tree.scrollPathToVisible(path);
                    return;
                }
            }
        }
    }

    private static final class Renderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Object o = ((DefaultMutableTreeNode) value).getUserObject();
            if (o instanceof Family f) {
                setText(Messages.get(f.key()));
                setIcon(null);
                setFont(getFont().deriveFont(Font.BOLD));
                setToolTipText(null);
            } else if (o instanceof Algorithm a) {
                setText(a.name());
                setIcon(Ui.dot(a.security()));
                setFont(getFont().deriveFont(Font.PLAIN));
                setToolTipText(Messages.get(a.security().key()));
            }
            return this;
        }
    }
}
