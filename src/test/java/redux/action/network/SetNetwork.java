package redux.action.network;

import packets.Packet;
import redux.action.Action;

public class SetNetwork implements Action {
    public Packet value;

    public SetNetwork(Packet p) {
        this.value = p;
    }
}
