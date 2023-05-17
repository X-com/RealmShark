package redux.action.statmaxing;

import redux.action.Action;

public class SetMainVault implements Action {
    public boolean value;

    public SetMainVault(boolean b) {
        this.value = b;
    }
}
