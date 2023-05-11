package tomato.logic.backend.state;

import packets.Packet;
import packets.incoming.*;
import packets.outgoing.EnemyHitPacket;
import packets.outgoing.PlayerShootPacket;
import tomato.gui.TomatoGUI;
import tomato.logic.backend.DpsToString;
import tomato.logic.backend.action.Action;
import tomato.logic.backend.action.network.SetHttpRequest;
import tomato.logic.backend.action.network.SetNetwork;
import tomato.logic.backend.data.RealmCharacter;
import tomato.logic.backend.data.TomatoData;

import java.util.ArrayList;

public class NetworkState {
    public TomatoData data = new TomatoData();

    public static NetworkState reduce(NetworkState state, Action action) {
        if (action instanceof SetNetwork) {
            Packet packet = ((SetNetwork) action).value;
            if (packet instanceof NewTickPacket) {
                NewTickPacket p = (NewTickPacket) packet;
                state.data.updateNewTick(p);
                TomatoGUI.setTextAreaAndLabelDPS(DpsToString.stringDmg(state.data), "123", false);
            } else if (packet instanceof UpdatePacket) {
                UpdatePacket p = (UpdatePacket) packet;
                state.data.update(p);
            } else if (packet instanceof PlayerShootPacket) {
                PlayerShootPacket p = (PlayerShootPacket) packet;
                state.data.playerShoot(p);
            } else if (packet instanceof ServerPlayerShootPacket) {
                ServerPlayerShootPacket p = (ServerPlayerShootPacket) packet;
                state.data.serverPlayerShoot(p);
            } else if (packet instanceof EnemyHitPacket) {
                EnemyHitPacket p = (EnemyHitPacket) packet;
                state.data.enemtyHit(p);
            } else if (packet instanceof DamagePacket) {
                DamagePacket p = (DamagePacket) packet;
                state.data.damage(p);
            } else if (packet instanceof TextPacket) {
                TextPacket p = (TextPacket) packet;
                state.data.text(p);
            } else if (packet instanceof MapInfoPacket) {
                MapInfoPacket p = (MapInfoPacket) packet;
                state.data.setNewRealm(p);
            } else if (packet instanceof CreateSuccessPacket) {
                CreateSuccessPacket p = (CreateSuccessPacket) packet;
                state.data.setUserId(p.objectId, p.charId);
            } else if (packet instanceof ExaltationUpdatePacket) {
                ExaltationUpdatePacket p = (ExaltationUpdatePacket) packet;
                state.data.exaltUpdate(p);
            } else if (packet instanceof VaultContentPacket) {
                VaultContentPacket p = (VaultContentPacket) packet;
                state.data.vaultPacketUpdate(p);
            }
            return state;
        } else if(action instanceof SetHttpRequest) {
            ArrayList<RealmCharacter> chars = ((SetHttpRequest) action).value;
            state.data.characterListUpdate(chars);
        }
        return state;
    }
}
