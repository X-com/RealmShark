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
    private static final ArrayList<Grouping> groupList = new ArrayList<>();
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
        JTextField field = new JTextField("[0, 0, 0, 12, 0, -9, -116, -12, 0, 2, 0, -52, 102, -57, 67, 30, -119, -70, 67, 6, 74, 56, 0, -52, 103, 43, 67, 30, -119, -70, 67, 6, 74, 56]");
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
            groupList.clear();
            for (int i = 0; i < bytes.length; i++) {
                selectionPanel.add(new JLabel("Byte: " + i));
                JPanel row = addedSection(i, bytes.length);
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

    public int[] readCompressedInt(byte[] bytes, int offset) {
        int uByte = Byte.toUnsignedInt(bytes[offset]);
        boolean isNegative = (uByte & 64) != 0;
        int shift = 6;
        int value = uByte & 63;
        int size = 1;

        while ((uByte & 128) != 0) {
            if (offset + size + 1 >= bytes.length) return new int[]{-1, -1};
            uByte = Byte.toUnsignedInt(bytes[offset + size]);
            value |= (uByte & 127) << shift;
            shift += 7;
            size++;
        }

        if (isNegative) {
            value = -value;
        }
        return new int[]{value, size};
    }

    private JPanel addedSection(int i, int length) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
//        row.setBorder(BorderFactory.createLineBorder(Color.gray));
        JCheckBox byteBox = new JCheckBox("Byte:        " + bytes[i] + "    [" + Byte.toUnsignedInt(bytes[i]) + "]");
        row.add(byteBox);
        Grouping.add(i, byteBox, 1, byteDisplay);

        if (i + 1 >= length) return row;

        JCheckBox shortBox = new JCheckBox("Short:      " + decodeShort(bytes, i) + "    [" + Short.toUnsignedInt(decodeShort(bytes, i)) + "]");
        Grouping.add(i, shortBox, 2, byteDisplay);
        row.add(shortBox);

        if (i + 3 >= length) return row;

        JCheckBox intBox = new JCheckBox("Integer:   " + decodeInt(bytes, i) + "    [" + Integer.toUnsignedLong(decodeInt(bytes, i)) + "]");
        Grouping.add(i, intBox, 4, byteDisplay);
        row.add(intBox);

        JCheckBox floatBox = new JCheckBox("Float:       " + Float.intBitsToFloat(decodeInt(bytes, i)));
        Grouping.add(i, floatBox, 4, byteDisplay);
        row.add(floatBox);

        if (i + 1 >= length) return row;
        int[] compInt = readCompressedInt(bytes, i);
        if (compInt[0] == -1) return row;
        JCheckBox compIntBox = new JCheckBox("Comp:     " + compInt[0]);
        Grouping.add(i, compIntBox, compInt[1], byteDisplay);
        row.add(compIntBox);

        if (i + 1 >= length) return row;
        int len = Short.toUnsignedInt(decodeShort(bytes, i));
        if (i + len >= length) return row;
        String ss = "";
        for (int s = 0; s < len; s++) {
            ss += (char) bytes[i + s];
        }
        JCheckBox stringBox = new JCheckBox("String:     \"" + ss + "\"");
        Grouping.add(i, stringBox, 2 + len, byteDisplay);
        row.add(stringBox);

        return row;
    }

    public static class Grouping {
        int index;
        int len;
        JCheckBox checkbox;

        public Grouping(int index, JCheckBox checkbox, int len) {
            this.index = index;
            this.len = len;
            this.checkbox = checkbox;
        }

        public static void add(int index, JCheckBox checkbox, int len, JLabel[] displays) {
            Grouping group = new Grouping(index, checkbox, len);
            groupList.add(group);

            checkbox.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    JCheckBox source = (JCheckBox) e.getSource();
                    if (source.isEnabled() && !source.isSelected()) {
                        for (int j = 0; j < len; j++) {
                            displays[index + j].setForeground(Color.MAGENTA);
                        }
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    JCheckBox source = (JCheckBox) e.getSource();
                    if (source.isEnabled() && !source.isSelected()) {
                        for (int j = 0; j < len; j++) {
                            displays[index + j].setForeground(Color.BLACK);
                        }
                    }
                }
            });

            checkbox.addActionListener(e -> {
                JCheckBox source = (JCheckBox) e.getSource();
                if (source.isSelected()) {
                    for (Grouping g : groupList) {
                        if (!g.checkbox.equals(source)
                                &&
                                (
                                        (g.index < index && g.index + g.len - 1 >= index) ||
                                                (g.index >= index && g.index < index + len)
                                )
                        ) {
                            g.checkbox.setEnabled(false);
                        }
                    }
                    for (int j = 0; j < len; j++) {
                        displays[index + j].setForeground(Color.LIGHT_GRAY);
                    }
                } else {
                    for (int j = 0; j < len; j++) {
                        displays[index + j].setForeground(Color.BLACK);
                    }
                    for (Grouping g : groupList) {
                        if (!g.checkbox.equals(source) &&
                                (
                                        (g.index < index && g.index + g.len - 1 >= index) ||
                                                (g.index >= index && g.index < index + len)
                                )
                        ) {
                            g.checkbox.setEnabled(true);
                        }
                    }
                }
            });
        }
    }
}
