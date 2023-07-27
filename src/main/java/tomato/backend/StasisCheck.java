package tomato.backend;

import packets.data.StatData;
import packets.data.enums.StatType;
import packets.incoming.StasisPacket;
import tomato.backend.data.Entity;
import tomato.backend.data.TomatoData;
import tomato.gui.security.SecurityGUI;
import util.Util;

public class StasisCheck {

    /**
     * Incoming packets updates after observing an entity becoming stasised.
     *
     * @param p    Stasis packet
     * @param data
     */
    public static void stasis(StasisPacket p, TomatoData data) {
        if (p.unknownByteArray[1] != 22) return;
        float stasisDuration = p.stasisDuration;

        for (Entity player : data.playerListUpdated.values()) {
            if (player.stasisCounter == data.time) continue;
            int item = player.stat.INVENTORY_1_STAT.statValue;
            if (StasisOrbs.usingOrb(item, stasisDuration)) {
                player.stasisCounter = 2;
            }
        }
    }

    /**
     * Checks if the player used mana when stasis is detected.
     *
     * @param entity Players that used stasis orbs.
     * @param stats  Stats of the player to check their mana use.
     */
    public static void checkManaFromStasis(Entity entity, StatData[] stats) {
        if (entity.stasisCounter > 0) {
            entity.stasisCounter--;
            for (StatData sd : stats) {
                if (sd.statType == StatType.MP_STAT) {
                    if (entity.stat.MP_STAT.statValue <= sd.statValue) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("[").append(Util.getHourTime()).append("] ");
                        sb.append(entity.name()).append(": ");
                        sb.append(StasisOrbs.getName(entity.stat.INVENTORY_1_STAT.statValue));
                        SecurityGUI.updateAbilityUsage(sb.toString());
                    }
                }
            }
        }
    }
}
