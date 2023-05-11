package tomato.logic.backend.action.statmaxing;

import tomato.logic.backend.action.Action;

public class SetCharInvs implements Action {
    public boolean value;

    public SetCharInvs(boolean b) {
        this.value = b;
    }
}
