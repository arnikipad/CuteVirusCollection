/*
 * out_of_control.java v1.0 — Controlled Mouse Movement Demo
 *
 * Based on the original demonstration. Mouse clicking is intentionally
 * disabled. Movement starts only after the user presses Start and can be
 * stopped immediately with Stop or ESC.
 *
 * Build:
 *   javac out_of_control.java
 * Run:
 *   java out_of_control
 */

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class out_of_control extends JFrame {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Random random = new Random();
    private final JLabel status = new JLabel("Stopped");
    private final JLabel position = new JLabel("Position: --, --");
    private final JLabel movesLabel = new JLabel("Moves: 0");
    private final JSpinner interval = new JSpinner(
            new SpinnerNumberModel(300, 100, 10000, 100));

    private Robot robot;
    private Thread worker;
    private long moves;

    public out_of_control() {
        super("Out of Control v1.0 — Controlled Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(470, 300);
        setLocationRelativeTo(null);

        try {
            robot = new Robot();
        } catch (AWTException ex) {
            status.setText("Robot unavailable");
        }

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setLayout(new java.awt.GridLayout(0, 2, 8, 8));

        panel.add(new JLabel("Movement interval (ms):"));
        panel.add(interval);
        panel.add(new JLabel("Mouse clicks:"));
        panel.add(new JLabel("Disabled"));
        panel.add(new JLabel("Movement area:"));
        panel.add(new JLabel("Current screen"));
        panel.add(new JLabel("Emergency stop:"));
        panel.add(new JLabel("ESC"));
        panel.add(new JLabel("Status:"));
        panel.add(status);
        panel.add(new JLabel("Position:"));
        panel.add(position);
        panel.add(new JLabel("Move count:"));
        panel.add(movesLabel);

        JButton start = new JButton("Start");
        JButton stop = new JButton("Stop");
        JButton reset = new JButton("Reset");
        stop.setEnabled(false);

        start.addActionListener(e -> startMovement(start, stop));
        stop.addActionListener(e -> stopMovement(start, stop));
        reset.addActionListener(e -> {
            stopMovement(start, stop);
            moves = 0;
            position.setText("Position: --, --");
            movesLabel.setText("Moves: 0");
            status.setText("Stopped");
            interval.setValue(300);
        });

        JPanel buttons = new JPanel();
        buttons.add(start);
        buttons.add(stop);
        buttons.add(reset);

        setLayout(new java.awt.BorderLayout());
        add(panel, java.awt.BorderLayout.CENTER);
        add(buttons, java.awt.BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(
                e -> stopMovement(start, stop),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopMovement(start, stop);
            }
        });
    }

    private void startMovement(JButton start, JButton stop) {
        if (running.get() || robot == null) {
            return;
        }

        running.set(true);
        moves = 0;
        start.setEnabled(false);
        stop.setEnabled(true);
        status.setText("Running — ESC to stop");

        worker = new Thread(() -> {
            while (running.get()) {
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                int x = random.nextInt(Math.max(1, screen.width));
                int y = random.nextInt(Math.max(1, screen.height));

                robot.mouseMove(x, y);
                moves++;

                SwingUtilities.invokeLater(() -> {
                    position.setText("Position: " + x + ", " + y);
                    movesLabel.setText("Moves: " + moves);
                });

                try {
                    Thread.sleep((Integer) interval.getValue());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "OutOfControl-Movement");

        worker.setDaemon(true);
        worker.start();
    }

    private void stopMovement(JButton start, JButton stop) {
        running.set(false);

        if (worker != null) {
            worker.interrupt();
            worker = null;
        }

        start.setEnabled(robot != null);
        stop.setEnabled(false);
        status.setText("Stopped");
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("A graphical desktop environment is required.");
            return;
        }

        SwingUtilities.invokeLater(() -> new out_of_control().setVisible(true));
    }
}
