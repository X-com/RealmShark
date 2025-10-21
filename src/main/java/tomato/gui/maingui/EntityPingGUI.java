package tomato.gui.maingui;

import tomato.backend.data.TomatoData;
import tomato.realmshark.Sound;

public class EntityPingGUI extends CustomListGUI {

    public EntityPingGUI(TomatoData data) {
        super(data, "entityIdPings", "Entity ID Ping Customization", data.getEntityIdPings(), "Entity ID");
        this.validationErrorMessage = "Invalid Entity ID! Please enter a valid integer.";
    }

    public void open() {
        Sound.custom.play();
        super.open();
    }

    @Override
    protected boolean validateEntry(String entry) {
        try {
            Integer.parseInt(entry);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
