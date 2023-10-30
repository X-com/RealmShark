package potato.view;

import lc.kra.system.keyboard.event.GlobalKeyEvent;
import lc.kra.system.mouse.event.GlobalMouseEvent;
import potato.Potato;
import potato.control.InputController;
import potato.control.ServerSynch;
import potato.model.Config;
import potato.view.opengl.OpenGLPotato;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;

public class OptionsMenu {

    static JFrame frame;

    private static ArrayList<Component> shapeComps;
    private static ArrayList<Component> alignComps = new ArrayList<>();

    private static ArrayList<JPanel> panels = new ArrayList<>();

    private static HashMap<Integer, String> keyMap = new HashMap<>();

    private static JPanel tempVirtualPanel;
    private static String tempKeyString;
    private static JTextField tempVirtualKey;
    private static int tempKeyArray;

    public static void main(String[] args) {
        frame.setVisible(true);
        new InputController(null, null, null);
    }

    static {
        Config.load();
        getFieldNamesKeys();
        mainFrame();
        makeOptionsWindow();
    }

    public static void load() {
    }

    private static void makeOptionsWindow() {
        resetButton();
        alwaysCoordsButton();
        hotkeys();
        shapes();
        colors();
        alignment();
        addServer();
        addPanels();
    }

    private static void resetButton() {
        JPanel p = new JPanel();
        JButton button = new JButton("Reset");
        button.addActionListener(e -> {
            resetAll();
        });
        button.setPreferredSize(new Dimension(80, 20));
        p.add(button);
        panels.add(p);
    }

    private static void alwaysCoordsButton() {
        addToggleButton("Always Show Info", Config.instance.alwaysShowCoords, e -> {
            JToggleButton f = (JToggleButton) e.getSource();
            Config.instance.alwaysShowCoords = f.isSelected();
            System.out.println("test");
            Config.save();
        });
    }

    private static void hotkeys() {
        label("Hotkeys");
        addHotkey("Zoom Map In", 0);
        addHotkey("Zoom Map Out", 1);
        addHotkey("Toggle Map", 2);
        addHotkey("Toggle Heroes", 3);
        addHotkey("Toggle Info", 4);
        addHotkey("Toggle All", 5);
        addHotkey("Toggle Recording", 6);
    }

    private static void shapes() {
        label("Shapes");

        shapeSlider("Text Size", 100, Config.instance.textSize, e -> {
            JSlider f = (JSlider) e.getSource();
            Config.instance.textSize = f.getValue();
            Config.save();
        });
        shapeSlider("Shape Size", 100, Config.instance.shapeSize, e -> {
            JSlider f = (JSlider) e.getSource();
            Config.instance.shapeSize = f.getValue();
            Config.save();
        });
    }

    private static void colors() {
        label("Colors");

        shapeSlider("Text Transparency", 255, Config.instance.textTransparency, e -> {
            JSlider f = (JSlider) e.getSource();
            Config.instance.textTransparency = f.getValue();
            Config.save();
        });
        shapeSlider("Map Transparency", 255, Config.instance.mapTransparency, e -> {
            JSlider f = (JSlider) e.getSource();
            Config.instance.mapTransparency = f.getValue();
            OpenGLPotato.setMapAlpha(f.getValue());
            Config.save();
        });
        addColor("Visited Heroes", Config.instance.visitedColor);
        addColor("Active Heroes", Config.instance.activeColor);
        addColor("Dead Heroes", Config.instance.deadColor);

        addToggleButton("Single Colored Shapes", Config.instance.singleColorShapes, e -> {
            JToggleButton f = (JToggleButton) e.getSource();
            Config.instance.singleColorShapes = f.isSelected();
            Config.save();

            for (Component c : shapeComps) {
                c.setEnabled(Config.instance.singleColorShapes);
            }
        });

        shapeComps = addColor("Shapes", Config.instance.shapesColor);

        for (Component c : shapeComps) {
            c.setEnabled(Config.instance.singleColorShapes);
        }
    }

    private static void alignment() {
        label("Map");

        addToggleButton("Manually Align", Config.instance.manualAlignment, e -> {
            JToggleButton f = (JToggleButton) e.getSource();
            Config.instance.manualAlignment = f.isSelected();
            Config.save();

            for (Component c : alignComps) {
                c.setEnabled(Config.instance.manualAlignment);
            }
        });

        JButton button = new JButton("Align");
        button.addActionListener(e -> {
            new Alignment();
        });
        button.setPreferredSize(new Dimension(80, 20));

        alignComps.add(button);
        JPanel aligner = new JPanel();
        aligner.add(button);
        panels.add(aligner);

        addToggleButton("Show Player Coords", Config.instance.showPlayerCoords, e -> {
            JToggleButton f = (JToggleButton) e.getSource();
            Config.instance.showPlayerCoords = f.isSelected();
            Config.save();
        });

        for (Component c : alignComps) {
            c.setEnabled(Config.instance.manualAlignment);
        }
    }

    private static ArrayList<Component> addColor(String name, int color) {
        ArrayList<Component> components = new ArrayList<>();

        int r = color >>> 24;
        int g = color << 8 >>> 24;
        int b = color << 16 >>> 24;
        int a = color << 24 >>> 24;

        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();

        JLabel labelName = new JLabel(name);
        panel1.add(labelName);

        JLabel labelR = new JLabel("R:");
        JSlider sliderR = new JSlider(0, 255, r);
        components.add(labelR);
        components.add(sliderR);
        panel2.add(labelR);
        panel2.add(sliderR);

        JLabel labelG = new JLabel("G:");
        JSlider sliderG = new JSlider(0, 255, g);
        components.add(labelG);
        components.add(sliderG);
        panel2.add(labelG);
        panel2.add(sliderG);

        JPanel panel3 = new JPanel();
        JLabel labelB = new JLabel("B:");
        JSlider sliderB = new JSlider(0, 255, b);
        components.add(labelB);
        components.add(sliderB);
        panel3.add(labelB);
        panel3.add(sliderB);

        JLabel labelA = new JLabel("A:");
        JSlider sliderA = new JSlider(0, 255, a);
        panel3.add(labelA);
        panel3.add(sliderA);

        panels.add(panel1);
        panels.add(panel2);
        panels.add(panel3);

        sliderR.addChangeListener(e -> {
            saveColor(name, sliderR, sliderG, sliderB, sliderA);
        });
        sliderG.addChangeListener(e -> {
            saveColor(name, sliderR, sliderG, sliderB, sliderA);
        });
        sliderB.addChangeListener(e -> {
            saveColor(name, sliderR, sliderG, sliderB, sliderA);
        });
        sliderA.addChangeListener(e -> {
            saveColor(name, sliderR, sliderG, sliderB, sliderA);
        });

        return components;
    }

    private static void saveColor(String name, JSlider sliderR, JSlider sliderG, JSlider sliderB, JSlider sliderA) {
        int color = 0;
        color |= sliderR.getValue() << 24;
        color |= sliderG.getValue() << 16;
        color |= sliderB.getValue() << 8;
        color |= sliderA.getValue();

        switch (name) {
            case "Visited Heroes":
                Config.instance.visitedColor = color;
                Config.intColorToVecColor(color, Config.instance.visitedColorVec);
                break;
            case "Active Heroes":
                Config.instance.activeColor = color;
                Config.intColorToVecColor(color, Config.instance.activeColorVec);
                break;
            case "Dead Heroes":
                Config.instance.deadColor = color;
                Config.intColorToVecColor(color, Config.instance.deadColorVec);
                break;
            case "Shapes":
                Config.instance.shapesColor = color;
                Config.intColorToVecColor(color, Config.instance.shapesColorVec);
        }
        Config.save();
    }

    private static void shapeSlider(String name, int size, int set, ChangeListener l) {
        JPanel shape = new JPanel();
        JLabel label = new JLabel(name);
        JSlider slider = new JSlider(0, size, set);
        slider.addChangeListener(l);
        shape.setLayout(new GridBagLayout());
        label.setPreferredSize(new Dimension(110, 16));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        shape.add(label);
        shape.add(slider);

        panels.add(shape);
    }

    private static void addToggleButton(String name, boolean align, ActionListener l) {
        JPanel shapeColors = new JPanel();
        JLabel shapeColorsLabel = new JLabel(name);
        JToggleButton toggleShapeColors = new JToggleButton();
        toggleShapeColors.setSelected(align);
        toggleShapeColors.addActionListener(l);
        shapeColors.add(shapeColorsLabel);
        shapeColors.add(toggleShapeColors);
        panels.add(shapeColors);
    }

    private static void label(String name) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(name);
        label.setFont(new Font("Dialog", 0, 18));
        panel.add(label);
        panels.add(panel);
    }

    private static void addHotkey(String name, int valueNum) {
        JPanel hotkey = new JPanel();
        JLabel titleMap = new JLabel(name);
        JTextField keyField = new JTextField(15);
        keyField.setText(Config.instance.keyString[valueNum]);

        titleMap.setHorizontalAlignment(SwingConstants.RIGHT);
        titleMap.setBorder(new EmptyBorder(0, 0, 0, 5));

        hotkey.setLayout(new GridLayout(1, 2));
        hotkey.add(titleMap);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(keyField);
        hotkey.add(p);

        panels.add(hotkey);

        keyField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                tempKeyString = keyField.getText();
                keyField.setText("");
                tempVirtualKey = keyField;
                tempVirtualPanel = hotkey;
                tempKeyArray = valueNum;
            }

            @Override
            public void focusLost(FocusEvent e) {
                keyField.setText(tempKeyString);
                tempKeyString = "";
                tempVirtualKey = null;
                tempVirtualPanel = null;
                tempKeyArray = -1;
            }
        });
    }

    public static void keyChanges(EventObject change, int keyMod) {
        if (tempVirtualKey != null) {
            if (change instanceof GlobalKeyEvent) {
                GlobalKeyEvent e = (GlobalKeyEvent) change;
                setModifierText(e, tempVirtualKey);
                int i = e.getVirtualKeyCode();
                if (i == 16 || i == 17 || i == 18) {
                    return;
                } else if (i == 27) {
                    tempVirtualPanel.requestFocus();
                    return;
                }
                String s = tempVirtualKey.getText();
                String key = String.valueOf(i);
                if (keyMap.containsKey(i)) {
                    key = keyMap.get(i);
                }
                tempVirtualKey.setText(s + key);
                tempKeyString = s + key;
                if (tempKeyArray >= 0) {
                    Config.instance.keyValues[tempKeyArray] = i | keyMod;
                    Config.instance.keyString[tempKeyArray] = s + key;
                }
                tempVirtualPanel.requestFocus();
                Config.save();
            } else if (change instanceof GlobalMouseEvent) {
                GlobalMouseEvent e = (GlobalMouseEvent) change;
                if (e.getTransitionState() == GlobalMouseEvent.TS_WHEEL) {
                    int d = e.getDelta();
                    String key = "";
                    int i = 0;
                    if (d == 120) {
                        key = "MW_UP";
                        i = 200;
                    } else if (d == -120) {
                        key = "MW_DOWN";
                        i = 201;
                    }
                    String s = tempVirtualKey.getText();
                    tempVirtualKey.setText(s + key);
                    tempKeyString = s + key;
                    if (tempKeyArray >= 0) {
                        Config.instance.keyValues[tempKeyArray] = i | keyMod;
                        Config.instance.keyString[tempKeyArray] = s + key;
                    }
                    tempVirtualPanel.requestFocus();
                    Config.save();
                } else if (e.getTransitionState() == GlobalMouseEvent.TS_DOWN) {
                    int b = e.getButton();
                    int i = 1;
                    int k = 0;
                    for (int j = 1; j < 15; j++) {
                        if (b == i) {
                            k = j;
                            break;
                        }
                        i = i << 1;
                    }
                    String s = tempVirtualKey.getText();
                    String key = "M_B" + k;
                    if (keyMap.containsKey(k) && k < 3) {
                        key = keyMap.get(k);
                    }
                    tempVirtualKey.setText(s + key);
                    tempKeyString = s + key;
                    if (tempKeyArray >= 0) {
                        Config.instance.keyValues[tempKeyArray] = k | keyMod;
                        Config.instance.keyString[tempKeyArray] = s + key;
                    }
                    tempVirtualPanel.requestFocus();
                    Config.save();
                }
            }
        }
    }

    private static void setModifierText(GlobalKeyEvent change, JTextField field) {
        String s = "";
        if (change.isShiftPressed()) {
            s += "Shift + ";
        }
        if (change.isControlPressed()) {
            s += "Ctrl + ";
        }
        if (change.isMenuPressed()) {
            s += "Alt + ";
        }

        field.setText(s);
    }

    private static void getFieldNamesKeys() {
        Field[] fields = GlobalKeyEvent.class.getFields();
        try {
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                f.setAccessible(true);
                Object o = f.get(null);
                String name = f.getName();
                if (name.startsWith("VK_")) {
                    keyMap.put((int) o, name.substring(3));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void resetAll() {
        Config.instance.showMap = true;
        Config.instance.showHeroes = true;
        Config.instance.showInfo = true;

        Config.instance.serverIp = "167.114.3.98:6000";

        Config.instance.keyValues = new int[]{200, 201, 0, 0, 0, 0, 0};
        Config.instance.keyString = new String[]{"MW_UP", "MW_DOWN", "", "", "", "", ""};

        Config.instance.textSize = 32;
        Config.instance.shapeSize = 32;
        Config.instance.singleColorShapes = false;
        Config.instance.alwaysShowCoords = true;

        Config.instance.textTransparency = 200;
        Config.instance.mapTransparency = 150;
        Config.instance.visitedColor = 0x00FF0064;
        Config.instance.activeColor = 0xFF000064;
        Config.instance.deadColor = 0xFFFFFF50;
        Config.instance.shapesColor = 0xFFFFFF80;

        Config.instance.manualAlignment = false;

        Config.save();
        Config.setColors();

        panels.clear();
        frame.getContentPane().removeAll();
        makeOptionsWindow();
        frame.validate();
        frame.repaint();
    }

    private static void setTextLater(String reset, JTextField field) {
        Runnable doLater = () -> {
            field.setText(reset);
        };
        SwingUtilities.invokeLater(doLater);
    }

    public static void showOptions() {
        frame.setVisible(true);
    }

    private static void addServer() {
        label("Server");

        JPanel con = new JPanel();

        JButton connect = new JButton("Connect");
        connect.setPreferredSize(new Dimension(90, 20));
        connect.addActionListener(e -> ServerSynch.connectServer());

        con.add(connect);

        panels.add(con);
        JPanel server = new JPanel();
        server.setLayout(new GridLayout(1, 3));
        server.setPreferredSize(new Dimension(70, 18));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel field = new JLabel("Server Ip:");

        JLabel serverIpLabel = new JLabel();
        serverIpLabel.setText(Config.instance.serverIp);

        field.setHorizontalAlignment(SwingConstants.RIGHT);
        field.setBorder(new EmptyBorder(0, 0, 0, 5));
        left.add(field);

        server.add(left);

        JButton paste = new JButton("Paste");
        paste.setPreferredSize(new Dimension(90, 20));
        paste.addActionListener(e -> pasteServerIp(serverIpLabel));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.LEFT));
        right.add(serverIpLabel);
        right.add(paste);
        server.add(right);

        panels.add(server);
    }

    private static void pasteServerIp(JLabel serverIpLabel) {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable t = c.getContents(null);
        if (t == null)
            return;
        try {
            String s = (String) t.getTransferData(DataFlavor.stringFlavor);
            String filtered = s.replaceAll(" ", "");
            String[] dotSplit = filtered.split("\\.");
            if (dotSplit.length == 4) {
                String[] colunSplit = dotSplit[3].split(":");
                if (colunSplit.length == 2) {
                    for (int i = 0; i < 3; i++) {
                        int num = Integer.parseInt(dotSplit[i]);
                        if (num < 0 || num > 255) {
                            JOptionPane.showMessageDialog(null, "Can't paste non-ip values, copy the server ip with the port then click paste. Ip example: 111.222.333.444:5555", "Non IP paste error", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                    }

                    int num4 = Integer.parseInt(colunSplit[0]);
                    if (num4 >= 0 && num4 <= 255) {
                        int port = Integer.parseInt(colunSplit[1]);
                        if (port >= 0 && port <= 65535) {
                            Config.instance.serverIp = filtered;
                            serverIpLabel.setText(Config.instance.serverIp);
                            Config.save();
                            return;
                        }
                    }
                }
            }
            System.out.println(filtered);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, "Can't paste non-ip values, copy the server ip with the port then click paste. Ip example: 111.222.333.444:5555", "Non IP paste error", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void addPanels() {
        int size = panels.size();
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(size, 1));

        for (JPanel p : panels) {
            panel.add(p);
        }
        frame.add(panel);
    }

    public static void mainFrame() {
        frame = new JFrame("    Potato Options    ");
        try {
            frame.setIconImage(ImageIO.read(Potato.imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        frame.setSize(500, 950);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }
}



