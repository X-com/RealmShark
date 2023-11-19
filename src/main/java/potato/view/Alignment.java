package potato.view;

import potato.model.Config;
import potato.view.opengl.OpenGLPotato;
import potato.view.opengl.WindowGLFW;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Alignment {
    private int increments = 100;
    private JLabel mapTopLeftXLabel;
    private JLabel mapTopLeftYLabel;
    private JLabel mapWidthLabel;
    private JLabel mapHeightLabel;
    private static boolean open = false;

    public Alignment() {
        if (open) return;
        open = true;
        JFrame frame = new JFrame("Manual Aligner");
        JPanel p = new JPanel(new GridLayout(1, 1));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel jPanel = new JPanel(new GridLayout(3, 3));
        jPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        p.add(jPanel);

        jPanel.add(alignFieldTopLeft());
        jPanel.add(new JLabel());
        jPanel.add(new JLabel());
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(new JLabel("Top/Left Map"));
        top.add(Box.createVerticalGlue());
        jPanel.add(top);
        jPanel.add(radioButtons());
        JPanel bot = new JPanel();
        bot.setLayout(new BoxLayout(bot, BoxLayout.Y_AXIS));
        bot.add(Box.createVerticalGlue());
        bot.add(new JLabel("Width/Height Map"));
        jPanel.add(bot);
        jPanel.add(new JLabel());
        jPanel.add(new JLabel());
        jPanel.add(alignFieldBottomRight());
        OpenGLPotato.setColor(1);

        frame.add(p);
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                OpenGLPotato.setColor(2);
                open = false;
            }
        });
    }

    private JPanel radioButtons() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        ButtonGroup group = new ButtonGroup();

        JRadioButton radio1 = new JRadioButton("+1");
        JRadioButton radio10 = new JRadioButton("+10");
        JRadioButton radio100 = new JRadioButton("+100", true);
        panel.add(radio1);
        panel.add(radio10);
        panel.add(radio100);
        group.add(radio1);
        group.add(radio10);
        group.add(radio100);
        radio1.addActionListener(e -> {
            increments = 1;
        });
        radio10.addActionListener(e -> {
            increments = 10;
        });
        radio100.addActionListener(e -> {
            increments = 100;
        });

        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> {
            Config.instance.mapTopLeftX = 0;
            Config.instance.mapTopLeftY = 0;
            Config.instance.mapWidth = 100;
            Config.instance.mapHeight = 100;
            mapTopLeftXLabel.setText(" " + Config.instance.mapTopLeftX);
            mapTopLeftYLabel.setText(" " + Config.instance.mapTopLeftY);
            mapWidthLabel.setText(" " + Config.instance.mapWidth);
            mapHeightLabel.setText(" " + Config.instance.mapHeight);
            Config.save();
        });
        panel.add(reset);

        return panel;
    }

    private JPanel alignFieldTopLeft() {
        JPanel panel = new JPanel(new GridLayout(3, 3));
        JPanel panel2 = new JPanel(new GridLayout(2, 1));
        mapTopLeftXLabel = new JLabel(" " + Config.instance.mapTopLeftX);
        mapTopLeftYLabel = new JLabel(" " + Config.instance.mapTopLeftY);
        panel2.add(mapTopLeftXLabel);
        panel2.add(mapTopLeftYLabel);

        Button up = new Button("/\\");
        Button left = new Button("<");
        Button right = new Button(">");
        Button down = new Button("\\/");

        panel.add(new JLabel());
        panel.add(up);
        panel.add(new JLabel());
        panel.add(left);
        panel.add(panel2);
        panel.add(right);
        panel.add(new JLabel());
        panel.add(down);
        panel.add(new JLabel());

        up.addActionListener(e -> {
            Config.instance.mapTopLeftY -= increments;
            mapTopLeftYLabel.setText(" " + Config.instance.mapTopLeftY);
            WindowGLFW.viewChanged();
            Config.save();
        });
        left.addActionListener(e -> {
            Config.instance.mapTopLeftX -= increments;
            mapTopLeftXLabel.setText(" " + Config.instance.mapTopLeftX);
            WindowGLFW.viewChanged();
            Config.save();
        });
        right.addActionListener(e -> {
            Config.instance.mapTopLeftX += increments;
            mapTopLeftXLabel.setText(" " + Config.instance.mapTopLeftX);
            WindowGLFW.viewChanged();
            Config.save();
        });
        down.addActionListener(e -> {
            Config.instance.mapTopLeftY += increments;
            mapTopLeftYLabel.setText(" " + Config.instance.mapTopLeftY);
            WindowGLFW.viewChanged();
            Config.save();
        });

        return panel;
    }

    private JPanel alignFieldBottomRight() {
        JPanel panel = new JPanel(new GridLayout(3, 3));
        JPanel panel2 = new JPanel(new GridLayout(2, 1));
        mapWidthLabel = new JLabel(" " + Config.instance.mapWidth);
        mapHeightLabel = new JLabel(" " + Config.instance.mapHeight);
        panel2.add(mapWidthLabel);
        panel2.add(mapHeightLabel);

        Button up = new Button("/\\");
        Button left = new Button("<");
        Button right = new Button(">");
        Button down = new Button("\\/");

        panel.add(new JLabel());
        panel.add(up);
        panel.add(new JLabel());
        panel.add(left);
        panel.add(panel2);
        panel.add(right);
        panel.add(new JLabel());
        panel.add(down);
        panel.add(new JLabel());

        up.addActionListener(e -> {
            Config.instance.mapHeight -= increments;
            if (Config.instance.mapHeight < 20) Config.instance.mapHeight = 20;
            mapHeightLabel.setText(" " + Config.instance.mapHeight);
            WindowGLFW.viewChanged();
            Config.save();
        });
        left.addActionListener(e -> {
            Config.instance.mapWidth -= increments;
            if (Config.instance.mapWidth < 20) Config.instance.mapWidth = 20;
            mapWidthLabel.setText(" " + Config.instance.mapWidth);
            WindowGLFW.viewChanged();
            Config.save();
        });
        right.addActionListener(e -> {
            Config.instance.mapWidth += increments;
            mapWidthLabel.setText(" " + Config.instance.mapWidth);
            WindowGLFW.viewChanged();
            Config.save();
        });
        down.addActionListener(e -> {
            Config.instance.mapHeight += increments;
            mapHeightLabel.setText(" " + Config.instance.mapHeight);
            WindowGLFW.viewChanged();
            Config.save();
        });

        return panel;
    }
}
