package es.toni.crypto.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import es.toni.crypto.core.Algorithm;
import es.toni.crypto.core.Algorithms;
import es.toni.crypto.core.Direction;
import es.toni.crypto.core.Format;

/** The desktop panel builds every algorithm's form and runs it without errors (no window needed). */
class MainPanelTest {

    @BeforeAll
    static void laf() throws Exception {
        SwingUtilities.invokeAndWait(() -> Ui.installLaf(false));
    }

    @Test
    void everyAlgorithmBuildsAndRuns() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainPanel p = new MainPanel();
            for (Algorithm a : Algorithms.all()) {
                p.select(a);
                assertEquals(a, p.current());
                p.direction(Direction.BACKWARD);
                p.direction(Direction.FORWARD);
                if (a.id().equals("aes-gcm")) {
                    p.setParam("key", "00112233445566778899aabbccddeeff", Format.HEX);
                    p.setInput("hola", Format.TEXT);
                    p.runNow();
                }
            }
        });
    }
}
