package gameoflife;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class GameOfLifeApp extends JFrame implements ActionListener, Runnable {

    static final boolean T = true, F = false;

    final GameOfLife gameOfLife;

    GameOfLifeCanvas gCanvas;
    JButton startStopButton;
    JSlider timeSlider;
    JComboBox magnifyComboBox;

    volatile boolean running = false;
    volatile int sleepTime = 600;

    Thread logicThread;

    public GameOfLifeApp(GameOfLife gameOfLife) {
        super("Conway's Game of Life");
        this.gameOfLife = gameOfLife;

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override public void run() {
                    createAndShowGUI();
                }
            });
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        catch(InvocationTargetException e) {
            e.printStackTrace();
        }
        logicThread = new Thread(this);
    }

    public GameOfLifeApp(boolean[][] seed, String rule) {
        this(new GameOfLife(seed, rule));
    }

    private void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch(UnsupportedLookAndFeelException e) { }
        catch(IllegalAccessException e) { }
        catch(ClassNotFoundException e) { }
        catch(InstantiationException e) { }

        gCanvas = new GameOfLifeCanvas(gameOfLife);
        gCanvas.setPreferredSize(new Dimension(GameOfLifeCanvas.SIZE, GameOfLifeCanvas.SIZE));

        JPanel buttonPanel = new JPanel();

        startStopButton = new JButton("Start");
        startStopButton.addActionListener(this);

        timeSlider = new JSlider(0, 2000, sleepTime);
        timeSlider.setPaintLabels(true);
        timeSlider.setPaintTicks(true);
        timeSlider.setMajorTickSpacing(400);
        timeSlider.setBorder(BorderFactory.createTitledBorder("Speed"));
        timeSlider.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                sleepTime = ((JSlider) e.getSource()).getValue();
            }
        });

        magnifyComboBox = new JComboBox(new String[] {"1x", "2x", "4x", "5x", "8x"});
        magnifyComboBox.setSelectedIndex(0);
        magnifyComboBox.setMaximumSize(new Dimension(20, 25));
        magnifyComboBox.addActionListener(this);

        buttonPanel.add(startStopButton);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(timeSlider);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(magnifyComboBox);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Container contentPane = getContentPane();
        contentPane.add(gCanvas, BorderLayout.NORTH);
        contentPane.add(buttonPanel, BorderLayout.PAGE_END);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        pack();
    }

    public void run() {
        while(true) {
            if(running) {
                gCanvas.paint();
                gameOfLife.next();

                try {
                    Thread.sleep(sleepTime);
                }
                catch(InterruptedException e) { }
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == startStopButton) {
            if(logicThread.getState() == Thread.State.NEW) {
                logicThread.start();
            }
            startStopButton.setText((running = !running) ? "Stop" : "Start");
        }
        else if(e.getSource() == magnifyComboBox) {
            String s = (String) ((JComboBox) e.getSource()).getSelectedItem();
            gCanvas.magnification = Character.digit(s.charAt(0), 10);
        }
    }

    public static void main(String[] args) throws Exception {
        new GameOfLifeApp(RLEReader.load(args[0]));
    }
}
