package redux.action.statmaxing;

import redux.action.Action;

public class SetPotStorage implements Action {
    public boolean value;

    public SetPotStorage(boolean b) {
        this.value = b;
    }
}
