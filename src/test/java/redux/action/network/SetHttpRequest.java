package redux.action.network;

import redux.action.Action;
import tomato.logic.backend.data.RealmCharacter;

import java.util.ArrayList;

public class SetHttpRequest implements Action {
    public ArrayList<RealmCharacter> value;

    public SetHttpRequest(ArrayList<RealmCharacter> c) {
        this.value = c;
    }
}
