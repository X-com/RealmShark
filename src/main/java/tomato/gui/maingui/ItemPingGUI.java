package tomato.gui.maingui;

import tomato.backend.data.TomatoData;
import tomato.realmshark.Sound;

public class ItemPingGUI extends CustomListGUI {

    public ItemPingGUI(TomatoData data) {
        super(data, "itemPings", "Item Ping Customization", data.getItemPings(), "Item ID or Name");
    }

    public void open() {
        Sound.custom.play();
        super.open();
    }

    @Override
    protected boolean validateEntry(String entry) {
        // no validation needed for item names/IDs
        return true;
    }
}
