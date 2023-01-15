package potato.view;

import javax.swing.*;

public class OptionsMenu {

    private static boolean show = false;
    static JFrame frame;

    static {
        mainFrame();
        makeOptionsWindow();
    }

    private static void makeOptionsWindow() {

        // color values
          // -text
          // -unvisited
          // -visited
          // -active
          // -dead
        // text size
        // shape size
        // shape single color
        // hotkeys
        // manual alignment
    }

    public static void showOptions() {
        frame.setVisible(show);
    }

    public static void mainFrame() {
        frame = new JFrame("    Potato    ");
//        frame.setIconImage(icon);
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
//        frame.add(jMenuBar);
//        frame.setJMenuBar(jMenuBar);
//        menuBar.setFrame(frame);
//        frame.setContentPane(mainPanel);
//        frame.setVisible(true);
    }
}
