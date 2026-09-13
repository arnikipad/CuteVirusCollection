/*
 * Out-of-Control v2.0 — controlled demonstration
 *
 * Based on the original out_of_control.java.
 * This version intentionally does NOT perform mouse clicks and does not
 * run an unbounded mouse-control loop. Movement is opt-in, bounded to the
 * current screen, and has a visible Stop button plus ESC emergency stop.
 *
 * Build:
 *   javac out_of_control_v2.java
 * Run:
 *   java out_of_control_v2
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class out_of_control_v2 extends JFrame {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Random random = new Random();
    private Robot robot;
    private Thread worker;
    private long moves;

    private final JSpinner interval = new JSpinner(
            new SpinnerNumberModel(500, 100, 10000, 100));
    private final JLabel status = new JLabel("Stopped");
    private final JLabel position = new JLabel("Position: --, --");
    private final JLabel count = new JLabel("Moves: 0");
    private final JButton start = new JButton("Start");
    private final JButton stop = new JButton("Stop");

    public out_of_control_v2() {
        super("Out-of-Control v2.0 — Controlled Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 330);
        setLocationRelativeTo(null);

        try {
            robot = new Robot();
        } catch (AWTException ex) {
            start.setEnabled(false);
            JOptionPane.showMessageDialog(this,
                    "Mouse-control initialization failed:\n" + ex.getMessage(),
                    "Initialization Error", JOptionPane.ERROR_MESSAGE);
        }

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Controlled Mouse Movement");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        root.add(title, BorderLayout.NORTH);

        JPanel settings = new JPanel(new GridLayout(4, 2, 8, 8));
        settings.setBorder(BorderFactory.createTitledBorder("Configuration"));
        settings.add(new JLabel("Movement interval (ms):"));
        settings.add(interval);
        settings.add(new JLabel("Mouse clicks:"));
        settings.add(new JLabel("Disabled"));
        settings.add(new JLabel("Movement area:"));
        settings.add(new JLabel("Current screen"));
        settings.add(new JLabel("Emergency stop:"));
        settings.add(new JLabel("ESC"));
        root.add(settings, BorderLayout.CENTER);

        JPanel info = new JPanel(new GridLayout(3, 1));
        info.setBorder(BorderFactory.createTitledBorder("Status"));
        info.add(status);
        info.add(position);
        info.add(count);

        JPanel controls = new JPanel();
        stop.setEnabled(false);
        controls.add(start);
        controls.add(stop);
        JButton reset = new JButton("Reset");
        controls.add(reset);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(info, BorderLayout.CENTER);
        bottom.add(controls, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        start.addActionListener(e -> startMovement());
        stop.addActionListener(e -> stopMovement());
        reset.addActionListener(e -> reset());

        getRootPane().registerKeyboardAction(
                e -> stopMovement(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopMovement();
            }
        });
    }

    private void startMovement() {
        if (running.get() || robot == null) return;

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
                    count.setText("Moves: " + moves);
                });

                try {
                    Thread.sleep((Integer) interval.getValue());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "ControlledMouseMovement");

        worker.setDaemon(true);
        worker.start();
    }

    private void stopMovement() {
        running.set(false);
        if (worker != null) {
            worker.interrupt();
            worker = null;
        }
        start.setEnabled(true);
        stop.setEnabled(false);
        status.setText("Stopped");
    }

    private void reset() {
        stopMovement();
        interval.setValue(500);
        moves = 0;
        position.setText("Position: --, --");
        count.setText("Moves: 0");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new out_of_control_v2().setVisible(true));
    }
}
