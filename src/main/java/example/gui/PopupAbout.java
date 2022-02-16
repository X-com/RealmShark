package example.gui;


import example.TomatoExample;
import util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * About window to display simple info about Tomato.
 * Removes itself when removing focus.
 */
class PopupAbout extends JFrame implements FocusListener {
    private String aboutTitle = " Tomato ";
    private String aboutTextLine1 = "Packet API for";
    private String aboutTextLine2 = "Realm of the Mad God";
    private String aboutTextLine3 = "MIT licence";
    private String aboutTextCredits = "Made by Anon";
    private PopupAbout popupAbout;
    private JPanel listPane, listCredit;
    private JLabel textLabelTitle, textLabelLine1, textLabelLine2,
            textLabelLine3, textLabelVersion, iconLabel,
            textLabelCredits;
    private ImageIcon icon;

    /**
     * Makes the popup About window without decorating boarders.
     *
     * @param frame Main frame of the TomatoGUI class.
     * @return returns this object PopupAbout when finished building.
     */
    public PopupAbout addPopup(JFrame frame) {
        popupAbout = new PopupAbout();
        popupAbout.setSize(200, 150);

        listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        popupAbout.add(listPane);
        listPane.setBackground(Color.white);
        listPane.setBorder(BorderFactory.createLineBorder(Color.black));

        textLabelTitle = new JLabel(aboutTitle);
        textLabelLine1 = new JLabel(aboutTextLine1);
        textLabelLine2 = new JLabel(aboutTextLine2);
        textLabelLine3 = new JLabel(aboutTextLine3);
        textLabelVersion = new JLabel(TomatoExample.version);
        textLabelTitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
        textLabelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabelLine1.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabelLine2.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabelLine3.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabelVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
        listPane.add(textLabelTitle);
        listPane.add(textLabelLine1);
        listPane.add(textLabelLine2);
        listPane.add(textLabelLine3);
        listPane.add(textLabelVersion);

        listCredit = new JPanel();
        listCredit.setLayout(new FlowLayout());
        listPane.add(listCredit);
        textLabelCredits = new JLabel(aboutTextCredits);
        textLabelCredits.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        textLabelCredits.setAlignmentX(Component.LEFT_ALIGNMENT);
        listCredit.add(textLabelCredits);
        icon = new ImageIcon(TomatoExample.tomatoIconURL);
        icon = new ImageIcon(icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
        iconLabel = new JLabel();
        iconLabel.setIcon(icon);
        iconLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        listCredit.setBackground(Color.white);
        listCredit.add(iconLabel);

        popupAbout.addFocusListener(popupAbout);
        popupAbout.setUndecorated(true);
        popupAbout.pack();
        popupAbout.setLocation(frame.getX() + frame.getWidth() / 2 - popupAbout.getWidth() / 2,
                frame.getY() + frame.getHeight() / 2 - popupAbout.getHeight() / 2);

        popupAbout.setVisible(true);
        return popupAbout;
    }

    /**
     * Does nothing
     *
     * @param e event listener
     */
    @Override
    public void focusGained(FocusEvent e) {
    }

    /**
     * Removes the About window or disposes it when losing focus.
     *
     * @param e event listener
     */
    @Override
    public void focusLost(FocusEvent e) {
        dispose();
    }
}
