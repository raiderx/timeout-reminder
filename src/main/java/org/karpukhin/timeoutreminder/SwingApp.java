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
import java.io.File;
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

    private final OptionsStorage optionsStorage;
    private final ScheduledExecutorService executor;
    private final Reminder reminder;

    private final JLabel workLabel;
    private final JTextField workField;
    private final JLabel breakLabel;
    private final JTextField breakField;
    private final JLabel todoLabel;
    private final JCheckBox startAutomaticallyCheckBox;
    private final JButton startButton;
    private final JButton pauseButton;
    private final JLabel statusLabel;
    private final JFrame mainFrame;

    private String message = "Надо сделать перерыв";

    protected SwingApp() {
        optionsStorage = new MemoryOptionsStorage();
        executor = Executors.newScheduledThreadPool(2);
        reminder = new Reminder(executor);

        workLabel = new JLabel("Work");
        workField = new JTextField(10);
        breakLabel = new JLabel("Pause");
        breakField = new JTextField(10);
        todoLabel = new JLabel("What to do");
        startAutomaticallyCheckBox = new JCheckBox("Start automatically");
        startButton = new JButton("Start");
        pauseButton = new JButton("Pause");

        statusLabel = new JLabel();
        mainFrame = new JFrame(TITLE);
    }

    protected void init() {

        String applicationHome = configPath(System.getProperty("os.name"), System.getProperty("user.home"));
        File file = new File(applicationHome);
        if (!file.exists()) {
            if (!file.mkdir()) {
                throw new RuntimeException("Can not create application home directory");
            }
        } else if (!file.isDirectory() || !file.canRead()) {
            throw new RuntimeException("Can not read from application home directory");
        }

        initComponents();
        initMainFrame();
        createLayout();
        readOptions(applicationHome);
        scheduleFetching();
    }

    static String configPath(String os, String home) {
        if (os.contains("Linux")) {
            return home + "/.config/timeout-reminder";
        }
        return home + File.separator + ".timeout-reminder";
    }

    private void initComponents() {
        workField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = workField.getText();
                try {
                    int workDuration = Integer.parseInt(text);
                    Options options = optionsStorage.load();
                    options.setWorkDuration(workDuration);
                    optionsStorage.save(options);
                } catch (NumberFormatException error) {
                    JOptionPane.showMessageDialog(mainFrame, "Expected integer but got: " + text, "Error", JOptionPane.ERROR_MESSAGE);
                    workField.requestFocus();
                }
            }
        });
        breakField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = breakField.getText();
                try {
                    int breakDuration = Integer.parseInt(text);
                    Options options = optionsStorage.load();
                    options.setWorkDuration(breakDuration);
                    optionsStorage.save(options);
                } catch (NumberFormatException error) {
                    JOptionPane.showMessageDialog(mainFrame, "Expected integer but got: " + text, "Error", JOptionPane.ERROR_MESSAGE);
                    breakField.requestFocus();
                }
            }
        });
        startAutomaticallyCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Options options = optionsStorage.load();
                options.setStartAutomatically(startAutomaticallyCheckBox.isSelected());
                optionsStorage.save(options);
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    startReminder();
                } catch (Exception error) {
                    JOptionPane.showMessageDialog(mainFrame, "Start error: " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                    JOptionPane.showMessageDialog(mainFrame, "Pause/Resume error: " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup()
                                        .addComponent(workField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(breakField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                )
                .addComponent(todoLabel)
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
                .addComponent(todoLabel)
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

    void readOptions(String home) {
        Options options = optionsStorage.load();
        workField.setText(Integer.toString(options.getWorkDuration()));
        breakField.setText(Integer.toString(options.getBreakDuration()));
        startAutomaticallyCheckBox.setSelected(options.isStartAutomatically());
    }

    private void scheduleFetching() {
        Runnable monitorTask = new Runnable() {
            @Override
            public void run() {
                String status;
                if (reminder.getState() == State.NotStarted) {
                    status = "Not started";
                } else {
                    long elapsed = reminder.getElapsed();
                    String format = reminder.getState() == State.Paused ? "Paused, left {0}:{1,number,00} s" : "Running, left {0}:{1,number,00} s";
                    status = MessageFormat.format(format, elapsed / 60, elapsed % 60);
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
                    Runtime.getRuntime().exec(new String[]{"zenity", "--info", "--title=" + TITLE, "--text=" + message});
                    System.out.println("Message was shown");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Options options = optionsStorage.load();
        reminder.start(options.getWorkDuration(), command);
        startButton.setEnabled(false);
        pauseButton.setText("Pause");
    }

    private void pauseReminder() {
        reminder.pause();
        startButton.setEnabled(true);
        pauseButton.setText("Resume");
    }

    private void resumeReminder() {
        reminder.resume();
        startButton.setEnabled(false);
        pauseButton.setText("Pause");
    }

    protected void show() {
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingApp app = new SwingApp();
                app.init();
                app.show();
            }
        });
    }
}
