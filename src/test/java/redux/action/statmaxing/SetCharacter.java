package redux.action.statmaxing;

import redux.action.Action;

public class SetCharacter implements Action {
    public int id;
    public boolean value;

    public SetCharacter(int id, boolean b) {
        this.id = id;
        this.value = b;
    }
}
