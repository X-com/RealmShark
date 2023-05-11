package tomato.logic.backend.action.statmaxing;

import tomato.logic.backend.action.Action;

public class SetPotStorage implements Action {
    public boolean value;

    public SetPotStorage(boolean b) {
        this.value = b;
    }
}
