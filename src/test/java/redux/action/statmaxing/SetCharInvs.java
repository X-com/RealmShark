package redux.action.statmaxing;

import redux.action.Action;

public class SetCharInvs implements Action {
    public boolean value;

    public SetCharInvs(boolean b) {
        this.value = b;
    }
}
