package tomato.gui.chat;

import tomato.backend.data.TomatoData;
import tomato.gui.maingui.CustomListGUI;
import tomato.realmshark.Sound;

public class ChatPingGUI extends CustomListGUI {

    public ChatPingGUI(TomatoData data, ChatGUI main) {
        super(data, "chatPingMessages", "Chat Messages Ping Customization", main.getPingMessages(), "Chat Message");
    }

    public void open() {
        Sound.pm.play();
        super.open();
    }

    protected boolean validateEntry(String entry) {
        // no validation needed for item names/IDs
        return true;
    }
}
