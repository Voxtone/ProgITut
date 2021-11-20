package de.fhws.core;

import javax.swing.*;

public class Window extends JFrame {
    public static final int WIDTH = 700, HEIGHT = 900;

    private final JPanel root = new JPanel();

    //JFrame stuff
    private final JTextArea infoText = new JTextArea();
    private final JTextArea commentText = new JTextArea();
    // enum with btns?

    public Window() {
        super("Comment");
        initWindow();
        super.add(root);
        super.setSize(WIDTH, HEIGHT);
        super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        super.setLocationRelativeTo(null);
        super.setVisible(true);
        super.pack();

    }

    private void initWindow() {
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        infoText.setSize(WIDTH, HEIGHT / 2);
        infoText.setEditable(false);
        infoText.setCursor(null);
        infoText.setOpaque(false);
        infoText.setFocusable(false);
        infoText.setLineWrap(true);
        infoText.setWrapStyleWord(true);
        root.add(infoText);
        commentText.setSize(WIDTH, HEIGHT / 2);
        root.add(commentText);
    }

    public void setInfoText(String s) {
        infoText.setText(s);
        this.pack();
    }

    public void setCommentText(String s) {
        commentText.setText(s);
        this.pack();
    }

    public String getCommentText() {
        return commentText.getText();
    }
}
