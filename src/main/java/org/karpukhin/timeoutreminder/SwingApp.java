package org.karpukhin.timeoutreminder;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Pavel Karpukhin
 * @since 08.07.15
 */
public class SwingApp {

    private static final String TITLE = "Timeout Reminder";
    private static final int WIDTH = 400;
    private static final int HEIGHT = 250;

    private final MessageSource messageSource;
    private final Options options;
    private final ScheduledExecutorService executor;
    private final Reminder reminder;

    private final JLabel workLabel;
    private final JTextField workField;
    private final JLabel breakLabel;
    private final JTextField breakField;
    private final JLabel messageLabel;
    private final JTextField messageField;
    private final JCheckBox startAutomaticallyCheckBox;
    private final JButton startButton;
    private final JButton pauseButton;
    private final JLabel statusLabel;
    private final JFrame mainFrame;

    protected SwingApp() {
        messageSource = new ResourceBundleMessageSource("messages");
        options = defaultOptions();
        executor = Executors.newScheduledThreadPool(2);
        reminder = new Reminder(executor);

        workLabel = new JLabel(messageSource.getMessage("label.work"));
        workField = new JTextField(10);
        breakLabel = new JLabel(messageSource.getMessage("label.break"));
        breakField = new JTextField(10);
        messageLabel = new JLabel(messageSource.getMessage("label.message"))
        ;
        messageField = new JTextField(20);
        startAutomaticallyCheckBox = new JCheckBox(messageSource.getMessage("label.start.automatically"));
        startButton = new JButton(messageSource.getMessage("label.start"));
        pauseButton = new JButton(messageSource.getMessage("label.pause"));

        statusLabel = new JLabel();
        mainFrame = new JFrame(TITLE);
    }

    protected void init() {
        initComponents();
        initMainFrame();
        createLayout();
    }

    private void initComponents() {
        workField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
                String text = workField.getText();
                try {
                    int workDuration = Integer.parseInt(text);
                    options.setWorkDuration(workDuration);
                } catch (NumberFormatException error) {
                    showError(messageSource.getMessage("label.expected.integer"), text);
                    workField.requestFocus();
                }
            }
        });
        breakField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
                String text = breakField.getText();
                try {
                    int breakDuration = Integer.parseInt(text);
                    options.setBreakDuration(breakDuration);
                } catch (NumberFormatException error) {
                    showError(messageSource.getMessage("label.expected.integer"), text);
                    breakField.requestFocus();
                }
            }
        });
        startAutomaticallyCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                options.setStartAutomatically(startAutomaticallyCheckBox.isSelected());
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    startReminder();
                } catch (Exception error) {
                    showError(messageSource.getMessage("label.start.error"), error.getMessage());
                }
            }
        });
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    if (reminder.getState() == State.Running) {
                        pauseReminder();
                    } else if (reminder.getState() == State.Paused) {
                        resumeReminder();
                    }
                } catch (Exception error) {
                    showError("Pause/Resume error: " + error.getMessage());
                }
            }
        });
    }

    private void initMainFrame() {
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setSize(WIDTH, HEIGHT);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension d = toolkit.getScreenSize();
        mainFrame.setLocation(
            (int) (d.getWidth() - mainFrame.getSize().getWidth()) / 2,
            (int) (d.getHeight() - mainFrame.getSize().getHeight()) / 2);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (executor != null) {
                    executor.shutdown();
                }
            }
        });
    }

    private void createLayout() {

        JPanel mainPanel = new JPanel();

        GroupLayout layout = new GroupLayout(mainPanel);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(workLabel)
                                .addComponent(breakLabel)
                                .addComponent(messageLabel)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(workField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(breakField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(messageField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                )
                .addComponent(startAutomaticallyCheckBox)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(startButton)
                        .addComponent(pauseButton)
                )
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(workLabel)
                        .addComponent(workField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(breakLabel)
                        .addComponent(breakField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(messageLabel)
                        .addComponent(messageField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                )
                .addComponent(startAutomaticallyCheckBox)
                .addGroup(layout.createParallelGroup()
                        .addComponent(startButton)
                        .addComponent(pauseButton)
                )
        );
        mainPanel.setLayout(layout);

        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(statusLabel);

        mainFrame.getContentPane().add(mainPanel);
        mainFrame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
    }

    private void scheduleReminding() {
        Runnable monitorTask = new Runnable() {
            @Override
            public void run() {
                String status;
                if (reminder.getState() == State.NotStarted) {
                    status = messageSource.getMessage("label.not.started");
                } else {
                    long elapsed = reminder.getElapsed();
                    status = messageSource.getMessage(reminder.getState() == State.Paused ? "label.paused" : "label.running");
                    status += ", " + MessageFormat.format(messageSource.getMessage("label.left"), elapsed / 60, elapsed % 60);
                }
                statusLabel.setText(status);
            }
        };
        executor.scheduleAtFixedRate(monitorTask, 1L, 1L, TimeUnit.SECONDS);

        if (startAutomaticallyCheckBox.isSelected()) {
            startReminder();
        }
    }

    private void startReminder() {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Time to break!");
                    Runtime.getRuntime().exec(new String[]{"zenity", "--info", "--title=" + TITLE, "--text=" + messageField.getText()});
                    System.out.println("Message was shown");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        reminder.start(options.getWorkDuration(), command);
        startButton.setEnabled(false);
        pauseButton.setText(messageSource.getMessage("label.pause"));
    }

    private void pauseReminder() {
        reminder.pause();
        startButton.setEnabled(true);
        pauseButton.setText(messageSource.getMessage("label.resume"));
    }

    private void resumeReminder() {
        reminder.resume();
        startButton.setEnabled(false);
        pauseButton.setText(messageSource.getMessage("label.pause"));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, messageSource.getMessage("label.error"),
            JOptionPane.ERROR_MESSAGE);
    }

    private void showError(String pattern, Object ... arguments) {
        JOptionPane.showMessageDialog(mainFrame, MessageFormat.format(pattern, arguments),
            messageSource.getMessage("label.error"), JOptionPane.ERROR_MESSAGE);
    }

    protected void show() {
        mainFrame.setVisible(true);
    }

    private void loadOptions() {
        workField.setText(Integer.toString(options.getWorkDuration()));
        breakField.setText(Integer.toString(options.getBreakDuration()));
        startAutomaticallyCheckBox.setSelected(options.isStartAutomatically());
        messageField.setText(options.getMessage());
    }

    private Options defaultOptions() {
        Options options = new Options();
        options.setWorkDuration(35);
        options.setBreakDuration(10);
        options.setStartAutomatically(true);
        options.setMessage(messageSource.getMessage("default.message"));
        return options;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingApp app = new SwingApp();
                app.init();
                app.show();
                app.loadOptions();
                app.scheduleReminding();
            }
        });
    }
}
