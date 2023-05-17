package redux.action.statmaxing;

import redux.action.Action;

public class SetGiftChest implements Action {
    public boolean value;

    public SetGiftChest(boolean b) {
        this.value = b;
    }
}
