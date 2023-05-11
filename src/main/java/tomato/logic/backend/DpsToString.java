package tomato.logic.backend;

import assets.AssetMissingException;
import assets.IdToAsset;
import tomato.logic.backend.data.Damage;
import tomato.logic.backend.data.Entity;
import tomato.logic.backend.data.TomatoData;

import java.util.*;
import java.util.stream.Collectors;


public class DpsToString {

    private int displayIndex = 0;
    TomatoData data;

    public DpsToString(TomatoData data) {
        this.data = data;
    }

    /**
     * Full output string from damage logs of all hostile mobs.
     *
     * @return logged dps output as a string.
     */
    public static String stringDmg(TomatoData data) {
        StringBuilder sb = new StringBuilder();

        List<Entity> sortedList = Arrays.stream(data.getEntityHitList()).sorted(Comparator.comparingLong(Entity::getLastDamageTaken).reversed()).collect(Collectors.toList());
        for (Entity e : sortedList) {
            if (e.maxHp() == 0) continue;
            sb.append(display(e, new Filter())).append("\n");
        }

        return sb.toString();
    }

//    /**
//     * Find the next dps log to display in the dps calculator.
//     */
//    public void nextDisplay() {
//        if (displayIndex < (entityLogs.size() - 1)) {
//            displayIndex++;
//            Entity[] e = entityLogs.get(displayIndex);
//            String s = stringDmg(e, filter);
//            String l = (displayIndex + 1) + "/" + (entityLogs.size() + 1);
//            TomatoGUI.setTextAreaAndLabelDPS(s, l, true);
//        } else if (displayIndex < entityLogs.size()) {
//            displayIndex++;
//            String l = (displayIndex + 1) + "/" + (entityLogs.size() + 1);
//            TomatoGUI.setTextAreaAndLabelDPS(stringDmg(firstPage, filter), l, false);
//        }
//    }

//    /**
//     * Find the previous dps log to display in the dps calculator.
//     */
//    public void previousDisplay() {
//        if (displayIndex > 0) {
//            displayIndex--;
//            Entity[] e = entityLogs.get(displayIndex);
//            String s = stringDmg(e, filter);
//            String l = (displayIndex + 1) + "/" + (entityLogs.size() + 1);
//            TomatoGUI.setTextAreaAndLabelDPS(s, l, true);
//        }
//    }

//    private void addToLogs() {
//        boolean selectable = true;
//        String text = null;
//        if (displayIndex == entityLogs.size()) {
//            displayIndex++;
//            selectable = false;
//            text = "";
//        }
//        firstPage = new Entity[0];
//        entityLogs.add(displayList());
//        String labelText = (displayIndex + 1) + "/" + (entityLogs.size() + 1);
//        TomatoGUI.setTextAreaAndLabelDPS(text, labelText, selectable);
//    }

//    /**
//     * Clears all dps logs
//     */
//    public void clearTextLogs() {
//        entityLogs.clear();
//        displayIndex = 1;
//        TomatoGUI.setTextAreaAndLabelDPS("", "1/1", false);
//    }

    public static String showInv(int equipmentFilter, Entity owner) {
        if (owner.getStatName() == null || equipmentFilter == 0) {
            return "";
        } else if (equipmentFilter == 1) {
            StringBuilder s = new StringBuilder();
            try {
                s.append("[");
                s.append(IdToAsset.getDisplayName(owner.stat.INVENTORY_0_STAT.statValue));
                s.append(" / ");
                s.append(IdToAsset.getDisplayName(owner.stat.INVENTORY_1_STAT.statValue));
                s.append(" / ");
                s.append(IdToAsset.getDisplayName(owner.stat.INVENTORY_2_STAT.statValue));
                s.append(" / ");
                s.append(IdToAsset.getDisplayName(owner.stat.INVENTORY_3_STAT.statValue));
                s.append("]");
            } catch (AssetMissingException e) {
                e.printStackTrace();
            }
            return s.toString();
        }
        StringBuilder s = new StringBuilder();
        if (equipmentFilter == 3) s.append("\n");
        for (int inventory = 0; inventory < 4; inventory++) {
            s.append("<");
//
//            if (inv[inventory].size() == 0) {
//                s.append("  ");
//            } else if (inv[inventory].size() == 1) {
//                s.append(String.format("%s %.1fsec %s\n", getName(inv[inventory].get(0).left().statValue), (float) (entityTime - entityStartTime) / 1000, "100% Equipped:1 "));
//            } else {
//                HashMap<Integer, Equipment> gear = new HashMap<>();
//                Pair<StatData, Long> pair2 = null;
//                long firstTime = 0;
//                for (int i = 1; i < inv[inventory].size(); i++) {
//                    Pair<StatData, Long> pair1 = inv[inventory].get(i - 1);
//                    pair2 = inv[inventory].get(i);
//                    long time1 = pair1.right();
//                    if (time1 == 0) time1 = entityStartTime;
//                    if (firstTime == 0) firstTime = time1;
//                    addGear(gear, time1, pair2.right(), pair1.left().statValue, pair1.left().statValue == pair2.left().statValue);
//                }
//                long totalTime = entityTime - firstTime;
//                addGear(gear, pair2.right(), entityTime, pair2.left().statValue, false);
//
//                Stream<Map.Entry<Integer, Equipment>> sorted2 = gear.entrySet().stream().sorted(comparingByValue());
//                for (Map.Entry<Integer, Equipment> m : sorted2.collect(Collectors.toList())) {
//                    s.append(String.format("%s %.1fsec %.2f%% Equipped:%d / ", getName(m.getKey()), ((float) m.getValue().time / 1000), ((float) m.getValue().time * 100 / totalTime), m.getValue().swaps));
//                }
//            }
//            s = new StringBuilder(s.substring(0, s.length() - 3));
//            s.append("> ");
//            if (equipmentFilter == 3) s.append("\n");
        }
        return s.substring(0, s.length() - 1);
    }

    public static String display(Entity entity, Filter filter) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.name()).append(" HP: ").append(entity.maxHp()).append("\n");
        List<Damage> playerDamageList = entity.getPlayerDamageList();
        int counter = 0;
        for (Damage dmg : playerDamageList) {
            counter++;
            String name = dmg.owner.getStatName();
            if (filter.nameFilter && filter.filteredStrings.length > 0) {
                boolean found = false;
                for (String n : filter.filteredStrings) {
                    if (name.toLowerCase().startsWith(n.toLowerCase())) {
                        found = true;
                        break;
                    }
                }
                if (!found) continue;
            }
            String extra = "    ";
            String isMe = dmg.owner.isUser() ? "->" : "  ";
            int index = name.indexOf(',');
            if (index != -1) name = name.substring(0, index);
            float pers = ((float) dmg.damage * 100 / (float) entity.maxHp());
            if (dmg.dammahCountered && dmg.chancellorDammahDmg) {
                extra = String.format("[Dammah Counter Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            } else if (dmg.walledGardenReflectors) {
                extra = String.format("[Garden Counter Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            }
            String inv = showInv(filter.equipmentFilter, dmg.owner);
            sb.append(String.format("%s %3d %10s DMG: %7d %6.3f%% %s %s\n", isMe, counter, name, dmg.damage, pers, extra, inv));
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Filter options class
     */
    private static class Filter {
        int equipmentFilter;
        String[] filteredStrings;
        boolean nameFilter;
    }

    /**
     * Class used to display player Equipment
     */
    private static class Equipment implements Comparable {
        int id;
        long time;
        int swaps;

        public Equipment(int id, long time, int swaps) {
            this.id = id;
            this.time = time;
            this.swaps = swaps;
        }

        @Override
        public int compareTo(Object o) {
            return (int) (time - ((Equipment) o).time);
        }
    }
}
