package redux.state;

import redux.action.Action;
import redux.action.statmaxing.*;
import tomato.realmshark.backend.action.statmaxing.*;

import java.util.HashMap;

public class StatMaxingGuiState {
    public boolean isCharInv, isMainVault, isPotStorage, isGiftChest;
    public boolean isSeasonal;
    public HashMap<Integer, Boolean> characters;

    public StatMaxingGuiState() {
        isCharInv = false;
        isMainVault = false;
        isPotStorage = false;
        isGiftChest = false;
        isSeasonal = false;
        characters = new HashMap<>();
    }

    public static StatMaxingGuiState reduce(StatMaxingGuiState state, Action action) {
        if (action instanceof SetCharInvs) {
            state.isCharInv = ((SetCharInvs) action).value;
            return state;
        } else if (action instanceof SetGiftChest) {
            state.isGiftChest = ((SetGiftChest) action).value;
            return state;
        } else if (action instanceof SetMainVault) {
            state.isMainVault = ((SetMainVault) action).value;
            return state;
        } else if (action instanceof SetPotStorage) {
            state.isPotStorage = ((SetPotStorage) action).value;
            return state;
        } else if (action instanceof SetSeasonal) {
            state.isSeasonal = ((SetSeasonal) action).value;
            return state;
        } else if (action instanceof SetCharacter) {
            SetCharacter a = ((SetCharacter) action);
            state.characters.put(a.id, a.value);
            return state;
        } else {
            return state;
        }
    }
}
