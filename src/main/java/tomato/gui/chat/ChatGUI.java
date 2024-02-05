package tomato.gui.chat;

import packets.incoming.TextPacket;
import tomato.gui.TomatoGUI;
import tomato.realmshark.AudioNotification;
import util.Util;

import javax.swing.*;
import java.awt.*;

public class ChatGUI extends JPanel {

    private static JTextArea textAreaChat;
    public static boolean save;
    public static boolean ping;

    public ChatGUI() {
        setLayout(new BorderLayout());
        textAreaChat = new JTextArea();
        add(TomatoGUI.createTextArea(textAreaChat));
    }

    /**
     * Add text to the chat text area.
     *
     * @param s The text to be added at the end of text area.
     */
    public static void appendTextAreaChat(String s) {
        if (textAreaChat != null) textAreaChat.append(s);
    }

    /**
     * Clears the chat text area.
     */
    public static void clearTextAreaChat() {
        textAreaChat.setText("");
    }

    /**
     * Sets the font of the text area.
     *
     * @param font Font to be set.
     */
    public static void editFont(Font font) {
        textAreaChat.setFont(font);
    }

    /**
     * Updates chat with chat message.
     *
     * @param p Text packet with chat data.
     */
    public static void updateChat(TextPacket p) {
        String a = "";
        if (p.recipient.contains("*Guild*")) {
            a = "[G]";
        } else if (!p.recipient.trim().isEmpty()) {
            if (ping) {
                AudioNotification.playNotificationSound();
            }
            a = "[P]";
        }
        String s = String.format("%s[%s]: %s", a, p.name, p.text);
        ChatGUI.appendTextAreaChat(s + "\n");
        if (save) {
            Util.print("chat/chat", s);
        }
    }
}
