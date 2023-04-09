package bugfixingtools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;

public class PacketDisplay {
    private JPanel displayPanel;
    private JPanel selectionPanel;
    private JPanel rowLittleBig;
    private byte[] bytes;
    private JLabel[] byteDisplay;
    private ArrayList<JPanel> rowList;
    private HashSet<JPanel> rowHighlight = new HashSet<>();


    public static void main(String[] args) {
        new PacketDisplay();
    }

    public PacketDisplay() {
        JFrame frame = new JFrame("Packets");
        frame.setSize(500, 500);
        frame.add(createPanels());
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private JPanel createPanels() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        selectionPanel = new JPanel();
        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
        JTextField field = new JTextField("[0, 0, 0, 25, -90, 0, 3, -57, -56, 1, 38, 0, 0, 0, 0, 0, 0, -1, -1, -1, 22, 64, -112, 0, 0]");
        field.addActionListener(e -> {
            System.out.println("field: " + field.getText());
            getByteArray(field.getText());
            updateDisplay();
            selectionSection();
            mainPanel.requestFocus();
        });

        rowLittleBig = littleBig();
        selectionPanel.removeAll();
        selectionSection();
        displayPanel = displayResult();
        mainPanel.add(displayPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(selectionPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(40);
        mainPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.add(field, BorderLayout.SOUTH);
        return mainPanel;
    }

    private void updateDisplay() {
        displayPanel.removeAll();
        displayPanel.setPreferredSize(new Dimension(480, 50));
        if (bytes != null) {
            for (JLabel l : byteDisplay) {
                displayPanel.add(l);
            }
        } else {
            displayPanel.add(new JLabel("Cant parse"));
        }
        displayPanel.revalidate();
    }

    public void getByteArray(String byteString) {
        String[] list;
        boolean hex = false;
        if (byteString.contains("Hex stream")) {
            hex = true;
            list = byteString.replace("  Hex stream: ", "").split(" ");
        } else {
            list = byteString.replaceAll("[\\[\\] ]", "").split(",");
        }
        byte[] b = new byte[list.length];
        try {
            for (int i = 0; i < list.length; i++) {
                String s = list[i];
                if (hex) {
                    b[i] = (byte) ((Character.digit(s.charAt(0), 16) << 4) + Character.digit(s.charAt(1), 16));
                } else {
                    b[i] = Byte.parseByte(s);
                }
            }
        } catch (NumberFormatException e) {
            return;
        }

        bytes = b;
        byteDisplay = new JLabel[b.length];
        for (int i = 0; i < b.length; i++) {
            byteDisplay[i] = new JLabel("" + b[i]);
            byteDisplay[i].setToolTipText("Byte: " + (i + 1));
        }
    }

    private JPanel displayResult() {
        JPanel displayPanel = new JPanel();
        JTextArea area = new JTextArea();
        area.setPreferredSize(new Dimension(480, 50));
        displayPanel.add(area);

        return displayPanel;
    }

    private JPanel littleBig() {
        JPanel row = new JPanel();
        JRadioButton radioBig = new JRadioButton("Big", true);
        JRadioButton radioLittle = new JRadioButton("Little");
        ActionListener l = e -> {
            selectionSection();
        };
        radioBig.addActionListener(l);
        radioLittle.addActionListener(l);
        ButtonGroup g = new ButtonGroup();
        g.add(radioBig);
        g.add(radioLittle);
        row.add(radioBig);
        radioLittle.setEnabled(false);
        row.add(radioLittle);
        return row;
    }

    private void selectionSection() {
        selectionPanel.removeAll();
        selectionPanel.add(rowLittleBig);
        if (bytes != null) {
            rowList = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                JPanel row = addedSection(i, bytes.length);
                rowList.add(row);
            }
            for (int i = 5; i < bytes.length; i++) {
                selectionPanel.add(new JLabel("Byte: " + i));
                JPanel row = addedSection(i, bytes.length);
                rowList.add(row);
                selectionPanel.add(row);
            }
        }
        selectionPanel.revalidate();
    }

    public static short decodeShort(byte[] bytes, int offset) {
        return (short) ((Byte.toUnsignedInt(bytes[0 + offset]) << 8) | Byte.toUnsignedInt(bytes[1 + offset]));
    }

    public static int decodeInt(byte[] bytes, int offset) {
        return (Byte.toUnsignedInt(bytes[0 + offset]) << 24) | (Byte.toUnsignedInt(bytes[1 + offset]) << 16) | (Byte.toUnsignedInt(bytes[2 + offset]) << 8) | Byte.toUnsignedInt(bytes[3 + offset]);
    }

    private JPanel addedSection(int i, int length) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
//        row.setBorder(BorderFactory.createLineBorder(Color.gray));
        JCheckBox byteBox = new JCheckBox("Byte:        " + Byte.toUnsignedInt(bytes[i]));
        row.add(byteBox);
        byteBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!rowHighlight.contains(((JCheckBox) e.getSource()).getParent())) {
                    byteDisplay[i].setForeground(Color.MAGENTA);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!rowHighlight.contains(((JCheckBox) e.getSource()).getParent())) {
                    byteDisplay[i].setForeground(Color.BLACK);
                }
            }
        });
        byteBox.addActionListener(e -> {
            if (((JCheckBox) e.getSource()).isSelected()) {
                byteDisplay[i].setForeground(Color.LIGHT_GRAY);
                for (int j = 0; j < 1; j++) {
                    rowHighlight.add(rowList.get(i + j));
                }
            } else {
                for (int j = 0; j < 1; j++) {
                    rowHighlight.remove(rowList.get(i + j));
                }
                byteDisplay[i].setForeground(Color.BLACK);
            }
        });

        if (i + 1 >= length) return row;

        JCheckBox shortBox = new JCheckBox("Short:      " + Short.toUnsignedInt(decodeShort(bytes, i)));
        shortBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!rowHighlight.contains(((JCheckBox) e.getSource()).getParent())) {
                    for (int j = 0; j < 2; j++) {
                        byteDisplay[i + j].setForeground(Color.MAGENTA);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!rowHighlight.contains(((JCheckBox) e.getSource()).getParent())) {
                    for (int j = 0; j < 2; j++) {
                        byteDisplay[i + j].setForeground(Color.BLACK);
                    }
                }
            }
        });
        shortBox.addActionListener(e -> {
            if (((JCheckBox) e.getSource()).isSelected()) {
                for (Component c : rowList.get(i + 1).getComponents()) {
                    c.setEnabled(false);
                }
                for (int j = 0; j < 2; j++) {
                    byteDisplay[i + j].setForeground(Color.LIGHT_GRAY);
                }
                for (int j = 0; j < 2; j++) {
                    rowHighlight.add(rowList.get(i + j));
                }
            } else {
                for (int j = 0; j < 2; j++) {
                    rowHighlight.remove(rowList.get(i + j));
                }
                for (int j = 0; j < 2; j++) {
                    byteDisplay[i + j].setForeground(Color.BLACK);
                }
                for (Component c : rowList.get(i + 1).getComponents()) {
                    c.setEnabled(true);
                }
            }
        });
        row.add(shortBox);

        if (i + 3 >= length) return row;

        JCheckBox intBox = new JCheckBox("Integer:   " + Integer.toUnsignedLong(decodeInt(bytes, i)));
        intBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!rowHighlight.contains(((JCheckBox) e.getSource()).getParent())) {
                    for (int j = 0; j < 4; j++) {
                        byteDisplay[i + j].setForeground(Color.MAGENTA);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!rowHighlight.contains(((JCheckBox) e.getSource()).getParent())) {
                    for (int j = 0; j < 4; j++) {
                        byteDisplay[i + j].setForeground(Color.BLACK);
                    }
                }
            }
        });
        intBox.addActionListener(e -> {
            if (((JCheckBox) e.getSource()).isSelected()) {
                for (int j = 1; j < 4; j++) {
                    for (Component c : rowList.get(i + j).getComponents()) {
                        c.setEnabled(false);
                    }
                }
                for (int j = 0; j < 4; j++) {
                    byteDisplay[i + j].setForeground(Color.LIGHT_GRAY);
                }
                for (int j = 0; j < 4; j++) {
                    rowHighlight.add(rowList.get(i + j));
                }
            } else {
                for (int j = 0; j < 4; j++) {
                    rowHighlight.remove(rowList.get(i + j));
                }
                for (int j = 0; j < 4; j++) {
                    byteDisplay[i + j].setForeground(Color.BLACK);
                }
                for (int j = 0; j < 4; j++) {
                    for (Component c : rowList.get(i + j).getComponents()) {
                        c.setEnabled(true);
                    }
                }
            }
        });
        row.add(intBox);

        JCheckBox floatBox = new JCheckBox("Float:       " + Float.intBitsToFloat(decodeInt(bytes, i)));
        floatBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!rowHighlight.contains(((JCheckBox) e.getSource()).getParent())) {
                    for (int j = 0; j < 4; j++) {
                        byteDisplay[i + j].setForeground(Color.MAGENTA);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!rowHighlight.contains(((JCheckBox) e.getSource()).getParent())) {
                    for (int j = 0; j < 4; j++) {
                        byteDisplay[i + j].setForeground(Color.BLACK);
                    }
                }
            }
        });
        floatBox.addActionListener(e -> {
            if (((JCheckBox) e.getSource()).isSelected()) {
                for (int j = 1; j < 4; j++) {
                    for (Component c : rowList.get(i + j).getComponents()) {
                        c.setEnabled(false);
                    }
                }
                for (int j = 0; j < 4; j++) {
                    byteDisplay[i + j].setForeground(Color.LIGHT_GRAY);
                }
                for (int j = 0; j < 4; j++) {
                    rowHighlight.add(rowList.get(i + j));
                }
            } else {
                for (int j = 0; j < 4; j++) {
                    rowHighlight.remove(rowList.get(i + j));
                }
                for (int j = 0; j < 4; j++) {
                    byteDisplay[i + j].setForeground(Color.BLACK);
                }
                for (int j = 0; j < 4; j++) {
                    for (Component c : rowList.get(i + j).getComponents()) {
                        c.setEnabled(true);
                    }
                }
            }
        });
        row.add(floatBox);

        if (i + 1 >= length) return row;
        int len = Short.toUnsignedInt(decodeShort(bytes, i));
        if (i + len >= length) return row;
        String ss = "";
        for (int s = 0; s < len; s++) {
            ss += (char) bytes[i + s];
        }
        JCheckBox stringBox = new JCheckBox("String:     \"" + ss + "\"");
        stringBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!rowHighlight.contains(((JCheckBox) e.getSource()).getParent())) {
                    for (int j = 0; j < 2; j++) {
                        byteDisplay[i + j].setForeground(Color.MAGENTA);
                    }
                    for (int j = 0; j < len; j++) {
                        byteDisplay[i + 2 + j].setForeground(Color.RED);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!rowHighlight.contains(((JCheckBox) e.getSource()).getParent())) {
                    for (int j = 0; j < 2 + len; j++) {
                        byteDisplay[i + j].setForeground(Color.BLACK);
                    }
                }
            }
        });
        stringBox.addActionListener(e -> {
            if (((JCheckBox) e.getSource()).isSelected()) {
                for (int j = 1; j < 2 + len; j++) {
                    for (Component c : rowList.get(i + j).getComponents()) {
                        c.setEnabled(false);
                    }
                }
                for (int j = 0; j < 2 + len; j++) {
                    byteDisplay[i + j].setForeground(Color.LIGHT_GRAY);
                }
                for (int j = 0; j < 2 + len; j++) {
                    rowHighlight.add(rowList.get(i + j));
                }
            } else {
                for (int j = 0; j < 2 + len; j++) {
                    rowHighlight.remove(rowList.get(i + j));
                }
                for (int j = 0; j < 2 + len; j++) {
                    byteDisplay[i + j].setForeground(Color.BLACK);
                }
                for (int j = 0; j < 2 + len; j++) {
                    for (Component c : rowList.get(i + j).getComponents()) {
                        c.setEnabled(true);
                    }
                }
            }
        });
        row.add(stringBox);

        return row;
    }
}
