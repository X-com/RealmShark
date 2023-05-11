package tomato.logic.backend.action.statmaxing;

import tomato.logic.backend.action.Action;

public class SetGiftChest implements Action {
    public boolean value;

    public SetGiftChest(boolean b) {
        this.value = b;
    }
}
