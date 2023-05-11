package tomato.logic.backend.action.statmaxing;

import tomato.logic.backend.action.Action;

public class SetCharacter implements Action {
    public int id;
    public boolean value;

    public SetCharacter(int id, boolean b) {
        this.id = id;
        this.value = b;
    }
}
