package tomato.logic.backend.action.statmaxing;

import tomato.logic.backend.action.Action;

public class SetSeasonal implements Action {
    public boolean value;

    public SetSeasonal(boolean b) {
        this.value = b;
    }
}
