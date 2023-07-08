package tomato.gui.security;

import tomato.gui.TomatoGUI;

import javax.swing.*;
import java.awt.*;

public class SecurityGUI extends JPanel {

    private static SecurityGUI INSTANCE;

    private JTextArea text;

    public SecurityGUI() {
        INSTANCE = this;
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        ParsePanelGUI parsePanel = new ParsePanelGUI();

        JPanel abilityUse = new JPanel();
        tabbedPane.addTab("Parse", parsePanel);
        tabbedPane.addTab("Ability Use", abilityUse);
        add(tabbedPane);

        abilityUse.setLayout(new BorderLayout());
        text = new JTextArea();
        JButton button = new JButton("Clear");
        button.addActionListener(e -> INSTANCE.text.setText(""));
        abilityUse.add(TomatoGUI.createTextArea(text), BorderLayout.CENTER);
        abilityUse.add(button, BorderLayout.SOUTH);
    }

    public static void updateAbilityUsage(String s) {
        INSTANCE.appendText(s);
    }

    private void appendText(String s) {
        text.append(s);
        text.append("\n");
    }
}
