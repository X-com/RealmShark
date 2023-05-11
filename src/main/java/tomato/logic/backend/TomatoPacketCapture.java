package tomato.logic.backend;

import packets.Packet;
import packets.incoming.*;
import packets.outgoing.EnemyHitPacket;
import packets.outgoing.PlayerShootPacket;
import tomato.gui.TomatoGUI;
import tomato.logic.backend.action.network.SetHttpRequest;
import tomato.logic.backend.action.network.SetNetwork;
import tomato.logic.backend.data.RealmCharacter;
import tomato.logic.backend.data.TomatoData;
import tomato.logic.backend.redux.Store;

import java.util.Arrays;

/**
 * Main packet handling class for incoming packets.
 */
public class TomatoPacketCapture implements Controller {

    private TomatoData data;

    public TomatoPacketCapture(TomatoData data) {
        this.data = data;
    }

    /**
     * @param packet incoming packets to be processed.
     */
    public void packetCapture(Packet packet) {
        if (packet instanceof NewTickPacket) {
            NewTickPacket p = (NewTickPacket) packet;
            data.updateNewTick(p);
            TomatoGUI.setTextAreaAndLabelDPS(DpsToString.stringDmg(data), "123", false);
        } else if (packet instanceof UpdatePacket) {
            UpdatePacket p = (UpdatePacket) packet;
            data.update(p);
        } else if (packet instanceof PlayerShootPacket) {
            PlayerShootPacket p = (PlayerShootPacket) packet;
            data.playerShoot(p);
        } else if (packet instanceof ServerPlayerShootPacket) {
            ServerPlayerShootPacket p = (ServerPlayerShootPacket) packet;
            data.serverPlayerShoot(p);
        } else if (packet instanceof EnemyHitPacket) {
            EnemyHitPacket p = (EnemyHitPacket) packet;
            data.enemtyHit(p);
        } else if (packet instanceof DamagePacket) {
            DamagePacket p = (DamagePacket) packet;
            data.damage(p);
        } else if (packet instanceof TextPacket) {
            TextPacket p = (TextPacket) packet;
            data.text(p);
        } else if (packet instanceof MapInfoPacket) {
            MapInfoPacket p = (MapInfoPacket) packet;
            data.setNewRealm(p);
        } else if (packet instanceof CreateSuccessPacket) {
            CreateSuccessPacket p = (CreateSuccessPacket) packet;
            data.setUserId(p.objectId, p.charId);
        } else if (packet instanceof ExaltationUpdatePacket) {
            ExaltationUpdatePacket p = (ExaltationUpdatePacket) packet;
            data.exaltUpdate(p);
        } else if (packet instanceof VaultContentPacket) {
            VaultContentPacket p = (VaultContentPacket) packet;
            data.vaultPacketUpdate(p);
        }
    }

    @Override
    public void dispose() {

    }
}
