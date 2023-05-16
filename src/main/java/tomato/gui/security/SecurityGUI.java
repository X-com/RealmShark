package tomato.gui.security;

import tomato.gui.security.ParsePanelGUI;

import javax.swing.*;
import java.awt.*;

public class SecurityGUI extends JPanel {

    public SecurityGUI() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        ParsePanelGUI parsePanel = new ParsePanelGUI();

        JPanel abilityPanel = new JPanel();
        tabbedPane.addTab("Parse", parsePanel);
        tabbedPane.addTab("Ability Use", abilityPanel);
        add(tabbedPane);
    }
}
