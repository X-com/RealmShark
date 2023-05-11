package tomato.logic.backend.action.statmaxing;

import tomato.logic.backend.action.Action;

public class SetMainVault implements Action {
    public boolean value;

    public SetMainVault(boolean b) {
        this.value = b;
    }
}
