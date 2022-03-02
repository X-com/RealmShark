package example.gui;

import example.ExampleModTomato;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Menu bar builder class
 */
public class TomatoMenuBar implements ActionListener {
    private JMenuItem about, borders, bandwidth;
    private JMenu file, edit, info;
    private JMenuBar jMenuBar;
    private JFrame frame;
    private static JMenuItem sniffer;

    /**
     * Main builder for menus for the Tomato GUI.
     *
     * @return returns this jMenuBar object to be added to the main frame.
     */
    public JMenuBar make() {
        jMenuBar = new JMenuBar();

        sniffer = new JMenuItem("Start Sniffer");
        sniffer.addActionListener(this);
        sniffer.setMargin(new Insets(2, -20, 2, 2));
        file = new JMenu("File");
        file.add(sniffer);
        jMenuBar.add(file);

        borders = new JMenuItem("Borders");
        borders.addActionListener(this);
        borders.setMargin(new Insets(2, -20, 2, 2));
        edit = new JMenu("Edit");
        edit.add(borders);
        jMenuBar.add(edit);

        about = new JMenuItem("About");
        about.addActionListener(this);
        about.setMargin(new Insets(2, -20, 2, 2));
        bandwidth = new JMenuItem("Bandwidth");
        bandwidth.addActionListener(this);
        bandwidth.setMargin(new Insets(2, -20, 2, 2));
        info = new JMenu("Info");
        info.add(about);
        info.add(bandwidth);
        jMenuBar.add(info);
        return jMenuBar;
    }

    /**
     * Sets the frame object for access to the frame.
     *
     * @param f The frame object.
     */
    public void setFrame(JFrame f) {
        frame = f;
    }

    /**
     * Action listiner for using the menu options.
     *
     * @param e event listener.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == about) { // Opens about window
            new TomatoPopupAbout().addPopup(frame);
        } else if (e.getSource() == borders) { // Removes the boarder of the window
            frame.dispose();
            frame.setUndecorated(!frame.isUndecorated());
            frame.setVisible(true);
        } else if (e.getSource() == sniffer) { // Starts and stops the sniffer
            if (sniffer.getText().contains("Start")) {
                sniffer.setText("Stop Sniffer");
                ExampleModTomato.startPacketSniffer();
                TomatoGUI.setStateOfSniffer(true);
            } else {
                stopPacketSniffer();
            }
        } else if (e.getSource() == bandwidth) { // Opens bandwidth window
            new TomatoBandwidth().make(frame);
        }
    }

    /**
     * Stops sniffer and changes GUI settings. TODO: temporary till better stream constructor solution is found.
     */
    public static void stopPacketSniffer() {
        sniffer.setText("Start Sniffer");
        ExampleModTomato.stopPacketSniffer();
        TomatoGUI.setStateOfSniffer(false);
    }
}