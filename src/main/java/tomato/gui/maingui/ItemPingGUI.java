package tomato.gui.maingui;

import tomato.backend.data.TomatoData;
import tomato.realmshark.Sound;
import util.PropertiesManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ItemPingGUI extends JPanel {

    private static ArrayList<JTextField> textFieldNames = new ArrayList<>();

    public ItemPingGUI(ArrayList<String> pingMessages) {
        setLayout(new BorderLayout());

        JPanel boxScroll = new JPanel();
        JScrollPane scrollPane = new JScrollPane(boxScroll);

        int w = 260;
        int h = 150;
        scrollPane.setBounds(0, 0, w + 15, h);
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);
        JPanel contentPane = new JPanel(null);
        contentPane.setPreferredSize(new Dimension(w, h));
        contentPane.add(scrollPane);
        add(contentPane, BorderLayout.CENTER);

        addTextFields(boxScroll, pingMessages);
    }

    private void addTextFields(JPanel mainPanel, ArrayList<String> pingMessages) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Item ID or Name to ping on."), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        topPanel.add(body, BorderLayout.CENTER);

        if (pingMessages != null && !pingMessages.isEmpty()) {
            for (String s : pingMessages) {
                JTextField comp1 = new JTextField();
                comp1.setText(s);
                textFieldNames.add(comp1);
                body.add(comp1);
            }
        } else {
            JTextField comp1 = new JTextField();
            textFieldNames.add(comp1);
            body.add(comp1);
        }

        JPanel bot = new JPanel();
        JButton addButton = new JButton("+");
        addButton.addActionListener(e -> {
            JTextField comp2 = new JTextField();
            textFieldNames.add(comp2);
            body.add(comp2);
            revalidate();
        });
        bot.setPreferredSize(new Dimension(250, 34));
        bot.add(addButton);
        topPanel.add(bot, BorderLayout.SOUTH);

        mainPanel.add(topPanel);
    }

    public static void open(TomatoData data) {
        Sound.custom.play();
        textFieldNames.clear();
        ItemPingGUI itemPing = new ItemPingGUI(data.getItemPings());

        JButton close = new JButton("Save");
        JOptionPane pane = new JOptionPane(itemPing, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new JButton[]{close}, close);
        close.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(close);
            pane.setValue(-1);
            w.dispose();
            ArrayList<String> arr = new ArrayList<>();
            for (JTextField f : textFieldNames) {
                String v = f.getText().trim();
                if (!arr.contains(v) && !v.isEmpty()) {
                    arr.add(v);
                }
            }
            setPingIds(data, arr);
        });
        JDialog dialog = pane.createDialog(null, "Custom Item Drop Ping");
//        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setVisible(true);
    }

    public static void setPingIds(TomatoData data, ArrayList<String> pingList) {
        data.setItemPing(pingList);
        if (pingList.isEmpty()) {
            PropertiesManager.setProperties("itemPings", "");
            return;
        }
        StringBuilder s = new StringBuilder();
        for (String i : pingList) {
            s.append("§").append(i);
        }
        PropertiesManager.setProperties("itemPings", s.substring(1));
    }

    public static void loadIdPing(TomatoData data) {
        ArrayList<String> arr = new ArrayList<>();
        String messages = PropertiesManager.getProperty("itemPings");
        if (messages == null) return;
        for (String s : messages.split("§")) {
            if (!s.isEmpty()) {
                arr.add(s);
            }
        }
        data.setItemPing(arr);
    }
}
