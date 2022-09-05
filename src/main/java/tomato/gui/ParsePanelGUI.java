package tomato.gui;

import util.ImageBuffer;
import util.Pair;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class ParsePanelGUI extends JPanel {

    private static ImageIcon empty;
    private JPanel listContainer;
    int i = 0;
    GridBagConstraints gridBagConstraint;
    JScrollPane scroll;

    public ParsePanelGUI() {
        empty = new ImageIcon(createTransparentBufferedImage(8, 8).getScaledInstance(30, 30, Image.SCALE_DEFAULT));
        setLayout(new BorderLayout());

        listContainer = new JPanel();
        listContainer.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.weighty = 1;
        listContainer.add(new JPanel(), gbc);

        gridBagConstraint = (GridBagConstraints) gbc.clone();
        gridBagConstraint.weighty = 0;
        gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;

        scroll = new JScrollPane(listContainer);
//        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
//        scroll.setAutoscrolls(true);
        new SmartScroller(scroll);
        add(scroll, BorderLayout.CENTER);

        JButton button = new JButton("Add");
        button.addActionListener(e -> {
            final JPanel newPanel = new JPanel();
            newPanel.add(new JLabel("Label " + i++));
            newPanel.setBorder(new LineBorder(Color.GRAY));
            newPanel.setSize(100, 100);

            listContainer.add(newPanel, gridBagConstraint, listContainer.getComponentCount() - 1);
            listContainer.revalidate();
            SwingUtilities.invokeLater(() -> newPanel.scrollRectToVisible(newPanel.getBounds()));
        });
        add(button, BorderLayout.SOUTH);

        validate();
        repaint();
    }

    static public BufferedImage createTransparentBufferedImage(int width, int height) {
        // BufferedImage is actually already transparent on my system, but that isn't
        // guaranteed across platforms.
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();

        // To be sure, we use clearRect, which will (unlike fillRect) totally replace
        // the current pixels with the desired color, even if it's fully transparent.
        graphics.setBackground(new Color(0, true));
        graphics.clearRect(0, 0, width, height);
        graphics.dispose();

        return bufferedImage;
    }

    public void addPlayers(ArrayList<Pair<String, int[]>> players) {
        if (scroll != null) {
            remove(scroll);
            revalidate();
            repaint();
        }
        listContainer.removeAll();
        for (Pair<String, int[]> player : players) {
            final JPanel newPanel = new JPanel();
            String[] name = player.left().split(",");
            JLabel label = new JLabel(name[0]);
            label.setFont(new Font("Monospaced", 0, 14));
            label.setPreferredSize(new Dimension(90, 20));
            newPanel.add(label);
            int[] inv = player.right();
            for (int i = 0; i < 4; i++) {
                int id = inv[i];
                try {
                    BufferedImage img = ImageBuffer.getImage(id);
                    if (img == null) {
                        newPanel.add(new JLabel(empty));
                        continue;
                    }
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(30, 30, Image.SCALE_DEFAULT));
                    newPanel.add(new JLabel(icon));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            newPanel.setBorder(new LineBorder(Color.GRAY));
            newPanel.setSize(100, 100);
            listContainer.add(newPanel, gridBagConstraint, listContainer.getComponentCount() - 1);
            SwingUtilities.invokeLater(() -> newPanel.scrollRectToVisible(newPanel.getBounds()));
        }
        add(scroll, BorderLayout.CENTER);
        listContainer.revalidate();
        revalidate();
    }
}
