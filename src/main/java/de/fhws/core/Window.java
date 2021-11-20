package de.fhws.core;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Window extends JFrame implements WindowListener {
    public static final int WIDTH = 1600, HEIGHT = 900;
    SubmissionTester tester;

    //JFrame stuff
    private final JTextArea info = new JTextArea();
    private final JButton[] btns = new JButton[6];
    // enum with btns?

    public Window(SubmissionTester tester) {
        super("Submission Tester");
        super.setSize(WIDTH, HEIGHT);
        super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        super.setLocationRelativeTo(null);

        initWindow();

        super.setVisible(true);
        super.pack();

        super.addWindowListener(this);

        this.tester = tester;
    }

    private void initWindow() {
        super.add(info);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {
        tester.save();
        System.exit(0);
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        tester.save();
    }
}
