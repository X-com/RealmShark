package tomato.logic.backend.action.network;

import packets.Packet;
import tomato.logic.backend.action.Action;

public class SetNetwork implements Action {
    public Packet value;

    public SetNetwork(Packet p) {
        this.value = p;
    }
}
