package tomato.gui;

import assets.AssetMissingException;
import assets.ImageBuffer;
import tomato.logic.HttpCharListRequest;
import tomato.logic.backend.data.RealmCharacter;
import tomato.logic.enums.CharacterStatistics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class CharacterStatsGUI extends JPanel {

    public CharacterStatsGUI() {
        update();
    }

    public void update() {
        FileInputStream is = null;
        try {
            is = new FileInputStream("tiles/assets/request");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        String result = new java.io.BufferedReader(new java.io.InputStreamReader(is)).lines().collect(java.util.stream.Collectors.joining("\n"));
        ArrayList<RealmCharacter> l = HttpCharListRequest.getCharList(result);

        System.out.println("before remove");
        removeAll();
        System.out.println("after remove");
        JPanel right = new JPanel();
        int dungeonCount = l.get(0).charStats.dungeonStats.length;
        int charCount = l.size();
        right.setLayout(new GridLayout(charCount, dungeonCount));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.gridx = 0;
//        main.setBorder(BorderFactory.createLineBorder(Color.black));
//        for (int i = 0; i < dungeonCount; i++) {
//            JPanel p = new JPanel();
//            p.setBorder(BorderFactory.createLineBorder(Color.black));
//            right.add(p);
//        }

//        JPanel p1 = new JPanel();
//        constraints.gridy = 0;
//        constraints.anchor = constraints.EAST;
//        p1.setBorder(BorderFactory.createLineBorder(Color.black));
//        left.add(p1, constraints);

        JPanel left = new JPanel();
        left.setLayout(new GridBagLayout());
        constraints.anchor = constraints.EAST;
        constraints.gridx = 0;

        ImageIcon empty = new ImageIcon(ImageBuffer.getEmptyImg().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
        JLabel emptyLabel = new JLabel(empty);
        JPanel p23 = new JPanel();
        p23.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
        p23.add(emptyLabel);
        constraints.gridy = 0;
        left.add(p23, constraints);
        for (int i = 0; i < charCount; i++) {
            RealmCharacter c = l.get(i);
            JLabel player = playerIcon(c);
            JPanel p = new JPanel();
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
            p.add(player);

            constraints.gridy = i + 1;
            left.add(p, constraints);

            for (int j = 0; j < dungeonCount; j++) {
                int v = c.charStats.dungeonStats[j];
                JPanel p2 = new JPanel();
                p2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
                p2.add(new JLabel("" + v));
                right.add(p2);
            }
        }

        JPanel top = new JPanel();

//        JPanel top = new JPanel();
//        top.setLayout(new GridBagLayout());
//        JPanel topLeft = new JPanel();
//        topLeft.add(new JLabel("    "));
//        top.add(topLeft);
//        top.add(topRight);

        JScrollPane spTop = new JScrollPane(top);
        JScrollPane spLeft = new JScrollPane(left);
        JScrollPane spRight = new JScrollPane(right);

        // have the two scroll pane's share the same BoundedRangeModel(s)
        spRight.getHorizontalScrollBar().setModel(spTop.getHorizontalScrollBar().getModel());
        spRight.getVerticalScrollBar().setModel(spLeft.getVerticalScrollBar().getModel());
        // sp2.getHorizontalScrollBar().setModel( sp1.getHorizontalScrollBar().getModel() );

        // hide one of the scroll bars
        spTop.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spLeft.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

//        JPanel content = new JPanel(new GridLayout(1, 0));
//        content.add(sp1);
//        content.add(sp2);

//        JScrollPane scrollPaneChars = new JScrollPane(right);
//        scrollPaneChars.getVerticalScrollBar().setUnitIncrement(40);
        JPanel r = new JPanel();
        r.setLayout(new BorderLayout());
        r.add(spTop, BorderLayout.NORTH);
        r.add(spRight, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(spLeft, BorderLayout.WEST);
        add(r, BorderLayout.CENTER);
        validate();

        top.setLayout(new GridLayout(1, dungeonCount));
        Dimension d = right.getComponents()[0].getSize();
        System.out.println(d);
        for (int j = 0; j < dungeonCount; j++) {
            BufferedImage img = null;
            try {
                int id = CharacterStatistics.DUNGEONS.get(j);
                img = ImageBuffer.getImage(id);
            } catch (IOException | AssetMissingException e) {
                e.printStackTrace();
            }
            ImageIcon icon = new ImageIcon(img.getScaledInstance(18, 18, Image.SCALE_DEFAULT));
            JLabel dungeonIcon = new JLabel(icon, JLabel.CENTER);

            JPanel p = new JPanel();
            p.setSize(d);
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
            p.add(dungeonIcon);
            top.add(p);
        }
        validate();
        Dimension dd = top.getComponents()[0].getSize();
        System.out.println(dd);
    }

    JLabel playerIcon(RealmCharacter c) {
        try {
            int eq = c.skin;
            if (eq == 0) eq = c.classNum;
            BufferedImage img = ImageBuffer.getImage(eq);
            ImageIcon icon = new ImageIcon(img.getScaledInstance(15, 15, Image.SCALE_DEFAULT));
            JLabel characterLabel = new JLabel(c.classString + " " + c.fame, icon, JLabel.CENTER);
            return characterLabel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
