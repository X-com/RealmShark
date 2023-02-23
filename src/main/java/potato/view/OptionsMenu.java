package potato.view;

import lc.kra.system.keyboard.event.GlobalKeyEvent;
import lc.kra.system.mouse.event.GlobalMouseEvent;
import potato.Potato;
import potato.control.InputController;
import potato.model.Config;
import potato.view.opengl.OpenGLPotato;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
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

    private static void makeOptionsWindow() {
        resetButton();
        alwaysCoordsButton();
        hotkeys();
        shapes();
        colors();
//        alignment();
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
        addToggleButton("Always Show Coords", Config.instance.alwaysShowCoords, e -> {
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
        label("Map Alignment");

        addToggleButton("Manually Align", Config.instance.manualAlignment, e -> {
            JToggleButton f = (JToggleButton) e.getSource();
            Config.instance.manualAlignment = f.isSelected();
            Config.save();

            for (Component c : alignComps) {
                c.setEnabled(Config.instance.manualAlignment);
            }
        });

        alignerFields("Top Left Corner", Integer.toString(Config.instance.mapTopLeftX), Integer.toString(Config.instance.mapTopLeftY));
        alignerFields("Map Size", Integer.toString(Config.instance.mapWidth), Integer.toString(Config.instance.mapHeight));

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
        label.setFont(new Font("Dialog", 0, 20));
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

    private static void alignerFields(String name, String setX, String setY) {
        JPanel aligner = new JPanel();
        JLabel label = new JLabel(name);
        JLabel labelX = new JLabel("X:");
        JTextField X = new JTextField(5);
        X.setText(setX);
        JLabel labelY = new JLabel("Y:");
        JTextField Y = new JTextField(5);
        Y.setText(setY);
        alignComps.add(labelX);
        alignComps.add(X);
        alignComps.add(labelY);
        alignComps.add(Y);
        panels.add(aligner);

        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setBorder(new EmptyBorder(0, 0, 0, 5));

        aligner.setLayout(new GridLayout(1, 2));
        aligner.add(label);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(labelX);
        p.add(X);
        p.add(labelY);
        p.add(Y);
        aligner.add(p);

        X.addCaretListener(e -> {
            JTextField field = (JTextField) e.getSource();
            String text = field.getText();
            String reset = "";
            if (name.equals("Top Left Corner")) {
                reset = Integer.toString(Config.instance.mapTopLeftX);
            } else if (name.equals("Map Size")) {
                reset = Integer.toString(Config.instance.mapWidth);
            }
            if (!text.equals("") && !text.equals("-")) {
                try {
                    int i = Integer.parseInt(text);

                    if (Math.abs(i) > 100000) {
                        setTextLater(reset, field);
                        return;
                    } else if (name.equals("Top Left Corner") && Config.instance.mapTopLeftX != i) {
                        Config.instance.mapTopLeftX = i;
                    } else if (name.equals("Map Size") && Config.instance.mapWidth != i) {
                        Config.instance.mapWidth = i;
                    } else {
                        return;
                    }
                } catch (NumberFormatException ignore) {
                    setTextLater(reset, field);
                    return;
                }
                Config.save();
            }
        });
        Y.addCaretListener(e -> {
            JTextField field = (JTextField) e.getSource();
            String text = field.getText();
            String reset = "";
            if (name.equals("Top Left Corner")) {
                reset = Integer.toString(Config.instance.mapTopLeftY);
            } else if (name.equals("Map Size")) {
                reset = Integer.toString(Config.instance.mapHeight);
            }
            if (!text.equals("") && !text.equals("-")) {
                try {
                    int i = Integer.parseInt(text);

                    if (Math.abs(i) > 100000) {
                        setTextLater(reset, field);
                        return;
                    } else if (name.equals("Top Left Corner") && Config.instance.mapTopLeftY != i) {
                        Config.instance.mapTopLeftY = i;
                    } else if (name.equals("Map Size") && Config.instance.mapHeight != i) {
                        Config.instance.mapHeight = i;
                    } else {
                        return;
                    }
                } catch (NumberFormatException ignore) {
                    setTextLater(reset, field);
                    return;
                }
                Config.save();
            }
        });

        X.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                JTextField field = (JTextField) e.getSource();
                String text = field.getText();
                if (text.equals("") || text.equals("-")) {
                    if (name.equals("Top Left Corner")) {
                        field.setText(Integer.toString(Config.instance.mapTopLeftX));
                    } else if (name.equals("Map Size")) {
                        field.setText(Integer.toString(Config.instance.mapWidth));
                    }
                }
            }
        });
        Y.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                JTextField field = (JTextField) e.getSource();
                String text = field.getText();
                if (text.equals("") || text.equals("-")) {
                    if (name.equals("Top Left Corner")) {
                        field.setText(Integer.toString(Config.instance.mapTopLeftY));
                    } else if (name.equals("Map Size")) {
                        field.setText(Integer.toString(Config.instance.mapHeight));
                    }
                }
            }
        });
    }

    private static void resetAll() {
        Config.instance.showMap = true;
        Config.instance.showHeroes = true;
        Config.instance.showInfo = true;

        Config.instance.keyValues = new int[]{200, 201, 0, 0, 0, 0};
        Config.instance.keyString = new String[]{"MW_UP", "MW_DOWN", "", "", "", ""};

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
        frame.setSize(500, 850);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }
}



