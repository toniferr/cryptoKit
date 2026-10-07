package es.toni.crypto.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import es.toni.crypto.core.Messages;
import es.toni.crypto.core.Security;

/** A rounded pill with the security level of the current algorithm. */
final class Badge extends JComponent {

    private Security security = Security.RECOMMENDED;

    void set(Security s) {
        security = s;
        setToolTipText(Messages.get(s.key() + ".hint"));
        revalidate();
        repaint();
    }

    private String text() {
        return Messages.get(security.key()).toUpperCase();
    }

    private Font badgeFont() {
        return getFont().deriveFont(Font.BOLD, getFont().getSize2D() - 1.5f);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(badgeFont());
        return new Dimension(fm.stringWidth(text()) + 22, fm.getHeight() + 8);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Color c = Ui.color(security);
        int h = getHeight();
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 38));
        g2.fillRoundRect(0, 0, getWidth() - 1, h - 1, h, h);
        g2.setColor(c);
        g2.drawRoundRect(0, 0, getWidth() - 1, h - 1, h, h);
        g2.setFont(badgeFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text(), 11, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
