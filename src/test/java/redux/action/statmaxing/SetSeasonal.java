package redux.action.statmaxing;

import redux.action.Action;

public class SetSeasonal implements Action {
    public boolean value;

    public SetSeasonal(boolean b) {
        this.value = b;
    }
}
