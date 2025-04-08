package tomato.gui.security;

import assets.IdToAsset;
import packets.data.enums.StatType;
import tomato.backend.data.Entity;
import tomato.realmshark.enums.CharacterClass;

import java.util.ArrayList;
import java.util.Arrays;

public class Player {
    int[] inv = new int[4];
    String[] itemName = new String[4];
    Entity playerEntity;

    public final static String[] equipmentNames = {"weapon", "ability", "armor", "ring"};
    private final static String[] statNames = new String[]{"HP", "MP", "Atk", "Def", "Spd", "Dex", "Vit", "Wis"};

    public Player(Entity playerEntity) {
        this.playerEntity = playerEntity;
        this.updateInv();
    }

    public boolean updateInv() {
        int[] newInv = new int[4];

        newInv[0] = playerEntity.stat.get(StatType.INVENTORY_0_STAT).statValue;
        newInv[1] = playerEntity.stat.get(StatType.INVENTORY_1_STAT).statValue;
        newInv[2] = playerEntity.stat.get(StatType.INVENTORY_2_STAT).statValue;
        newInv[3] = playerEntity.stat.get(StatType.INVENTORY_3_STAT).statValue;

        boolean didInvChange = !Arrays.equals(inv, newInv);
        this.inv = newInv;

        return didInvChange;
    }

    /**
     * Computes the missing pots needed to max the character.
     */
    public int[] statMissing() {
        Entity player = this.playerEntity;
        int[] stats = new int[8];
        stats[0] = (int) Math.ceil((CharacterClass.getLife(player.objectType) - player.baseStats[0]) / 5.0);
        stats[1] = (int) Math.ceil((CharacterClass.getMana(player.objectType) - player.baseStats[1]) / 5.0);
        stats[2] = CharacterClass.getAtk(player.objectType) - player.baseStats[2];
        stats[3] = CharacterClass.getDef(player.objectType) - player.baseStats[3];
        stats[4] = CharacterClass.getSpd(player.objectType) - player.baseStats[4];
        stats[5] = CharacterClass.getDex(player.objectType) - player.baseStats[5];
        stats[6] = CharacterClass.getVit(player.objectType) - player.baseStats[6];
        stats[7] = CharacterClass.getWis(player.objectType) - player.baseStats[7];

        return stats;
    }

    /**
     * Gets the characters maxed stat count.
     */
    public int statsMaxed() {
        Entity player = this.playerEntity;
        int outOf8 = 0;
        if (CharacterClass.getLife(player.objectType) == player.baseStats[0]) outOf8++;
        if (CharacterClass.getMana(player.objectType) == player.baseStats[1]) outOf8++;
        if (CharacterClass.getAtk(player.objectType) == player.baseStats[2]) outOf8++;
        if (CharacterClass.getDef(player.objectType) == player.baseStats[3]) outOf8++;
        if (CharacterClass.getSpd(player.objectType) == player.baseStats[4]) outOf8++;
        if (CharacterClass.getDex(player.objectType) == player.baseStats[5]) outOf8++;
        if (CharacterClass.getVit(player.objectType) == player.baseStats[6]) outOf8++;
        if (CharacterClass.getWis(player.objectType) == player.baseStats[7]) outOf8++;

        return outOf8;
    }

    /**
     * Computes the skin ID for a player.
     */
    public int getSkinId() {
        Entity player = this.playerEntity;
        int skinId = player.stat.get(StatType.SKIN_ID).statValue;
        if (skinId == 0) skinId = player.objectType;

        return skinId;
    }

    public String toString() {
        int type = playerEntity.objectType;
        String clazz = CharacterClass.getName(type);
        int level = playerEntity.stat.get(StatType.LEVEL_STAT).statValue;
        boolean seasonal = playerEntity.isSeasonal();
        boolean crucible = playerEntity.isCrucible();
        int stat = statsMaxed();
        int[] missing = statMissing();

        StringBuilder sb = new StringBuilder();
        sb.append("\t{\n");
        sb.append("\t\t").append("\"name\":\"").append(playerEntity.name()).append("\",\n");
        sb.append("\t\t").append("\"class\":\"").append(clazz).append("\",\n");
        sb.append("\t\t").append("\"level\":").append(level).append(",\n");
        sb.append("\t\t").append("\"guild\":\"").append(playerEntity.getStatGuild()).append("\",\n");
        sb.append("\t\t").append("\"seasonal\":").append(seasonal ? "true" : "false").append(",\n");
        sb.append("\t\t").append("\"crucible\":").append(crucible ? "true" : "false").append(",\n");

        sb.append("\t\t").append("\"equipment\":{\n");
        for (int i = 0; i < 4; i++) {
            sb.append("\t\t\t").append("\"").append(equipmentNames[i]).append("\":\"").append(IdToAsset.objectName(inv[i])).append("\",\n");
            sb.append("\t\t\t").append("\"").append(equipmentNames[i]).append("id\":").append(inv[i]).append(i != 3 ? "," : "").append("\n");
        }
        sb.append("\t\t").append("},\n");

        sb.append("\t\t").append("\"maxstats\":").append(stat).append(",\n");
        sb.append("\t\t").append("\"missingstats\":{\n");
        ArrayList<String> l = new ArrayList<>();
        for (int i = 0; i < missing.length; i++) {
            if (missing[i] == 0) continue;
            l.add(String.format("\t\t\t\"%s\":%d", statNames[i], missing[i]));
        }
        for (int i = 0; i < l.size(); i++) {
            sb.append(l.get(i)).append(i < l.size() - 1 ? "," : "").append("\n");
        }
        sb.append("\t\t").append("}\n");

        sb.append("\t}");
        return sb.toString();
    }
}