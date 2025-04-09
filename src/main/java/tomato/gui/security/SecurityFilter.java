package tomato.gui.security;

import com.google.gson.Gson;
import tomato.realmshark.ParseEquipment;

import java.util.ArrayList;
import java.util.TreeMap;

public class SecurityFilter {
    public String name;
    public transient String json;
    public int exaltSkinPoints;
    public boolean[] statMaxed = new boolean[8];
    public boolean isWhitelistFilter = true; // default to whitelist
    public TreeMap<Integer, Integer> itemPoint = new TreeMap<>();
    public TreeMap<Integer, Integer> classPoint = new TreeMap<>();
    public TreeMap<Integer, Integer> minTier = new TreeMap<>();

    public final static int[] exaltedSkinIds = {
        9497, //Rogue
        9499, //Archer
        9501, //Wizard
        9503, //Priest
        9505, //Warrior
        9507, //Knight
        9509, //Paladin
        9511, //Assassin
        9513, //Necromancer
        9515, //Huntress
        9519, //Trickster
        9517, //Mystic
        9521, //Sorcerer
        9523, //Ninja
        9525, //Samurai
        9527, //Bard
        30721, //Summoner
        31238, //Kensei
    };

    public static SecurityFilter loadJson(String json) {
        try {
            SecurityFilter sf = new Gson().fromJson(json, SecurityFilter.class);
            sf.json = json;
            return sf;
        } catch (Exception e) {
            System.err.printf("Could not load security filters... \n%s%n", e.getMessage());
            return null;
        }
    }

    public ParsedPlayerObject parsePlayer(Player player) {
        ArrayList<String> missing = new ArrayList<>();
        for (int i = 0; i < this.statMaxed.length; i++) {
            if (this.statMaxed[i] && player.statMissing()[i] > 0) {
                missing.add("Stats not maxed");
                break;
            }
        }

        // equipment minimum reqs
        for (int slot : minTier.keySet()) {
            int equipmentId = player.inv[slot];
            ParseEquipment.Equipment equipment = ParseEquipment.getEquipmentById(equipmentId);
            Integer minimumTier = this.minTier.get(slot);
            boolean isSTUT = equipment.labels.contains("ST") || equipment.labels.contains("UT");

            // handle empty gear slots
            if (equipment == null) {
                missing.add("Gear missing: " + Player.equipmentNames[slot]);
                continue;
            }

            // some ST/UT items have the "TIERED" label also - these SHOULD be mutually exclusive
            if (equipment.labels.contains("TIERED") && equipment.tier < minimumTier && !isSTUT) {
                missing.add("Gear below reqs: T" + equipment.tier + " " + Player.equipmentNames[slot]);
            }
        }

        int point = 0;
        for (int eid : exaltedSkinIds) {
            if (player.getSkinId() == eid) {
                point += this.exaltSkinPoints;
                break;
            }
        }

        int classPoint = this.classPoint.get(player.playerEntity.objectType);
        for (int i = 0; i < 4; i++) {
            int item = player.inv[i];
            // skip empty item slots
            if (item == -1) continue;
            // skip non-parsable items
            if (!ParseEquipment.isParseItem(ParseEquipment.getEquipmentById(item))) continue;
            Integer ip = this.itemPoint.get(item);
            if ((ip == null && isWhitelistFilter) || (ip != null && !isWhitelistFilter)) {
                missing.add("Blacklisted item: " + Player.equipmentNames[i]);
            } else {
                if (ip != null) point += ip;
            }
        }

        boolean isUnderReqs = !missing.isEmpty() || point < classPoint;
        return new ParsedPlayerObject(player, missing, point, classPoint, isUnderReqs);
    }

    public static class ParsedPlayerObject {
        public Player player;
        public ArrayList<String> missing;
        public int points;
        public int classPoints;
        public boolean isUnderReqs;

        public ParsedPlayerObject(Player player, ArrayList<String> missing, int points, int classPoints, boolean isUnderReqs) {
            this.player = player;
            this.missing = missing;
            this.points = points;
            this.classPoints = classPoints;
            this.isUnderReqs = isUnderReqs;
        }
    }
}
