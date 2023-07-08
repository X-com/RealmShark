package tomato.backend;

import assets.AssetMissingException;
import assets.IdToAsset;
import tomato.backend.data.Damage;
import tomato.gui.dps.DpsDisplayOptions;
import tomato.backend.data.Entity;
import tomato.backend.data.TomatoData;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class DpsToString {

    private TomatoData data;

    public DpsToString(TomatoData data) {
        this.data = data;
    }

    /**
     * Real time string display.
     *
     * @return logged dps output as a string.
     */
    public static String stringDmgRealtime(Entity[] data) {
        StringBuilder sb = new StringBuilder();

        List<Entity> sortedList = Arrays.stream(data).sorted(Comparator.comparingLong(Entity::getLastDamageTaken).reversed()).collect(Collectors.toList());
        for (Entity e : sortedList) {
            if (e.maxHp() <= 0) continue;
            sb.append(display(e)).append("\n");
        }

        return sb.toString();
    }

    public static String showInv(int equipmentFilter, Entity owner, Entity entity) {
        if (equipmentFilter == 0 || owner.getStatName() == null) return "";

        HashMap<Integer, Equipment>[] inv = new HashMap[4];

        for (int i = 0; i < 4; i++) {
            AtomicInteger tot = new AtomicInteger(0);
            if (inv[i] == null) inv[i] = new HashMap<>();
            for (Damage d : entity.getDamageList()) {
                if (d.owner == null || d.owner.id != owner.id || d.ownerInv == null) continue;

                Equipment equipment = inv[i].computeIfAbsent(d.ownerInv[i], id -> new Equipment(id, tot));
                equipment.add(d.damage);
            }
        }

        if (equipmentFilter == 1) {
            StringBuilder s = new StringBuilder();
            s.append("[");
            for (int i = 0; i < 4; i++) {
                Equipment max = inv[i].values().stream().max(Comparator.comparingInt(e -> e.dmg)).orElseThrow(NoSuchElementException::new);
                try {
                    if (i != 0) s.append(" / ");
                    s.append(IdToAsset.objectName(max.id));
                } catch (AssetMissingException ex) {
                    throw new RuntimeException(ex);
                }
            }
            s.append("]");
            return s.toString();
        } else if (equipmentFilter == 2) {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                s.append("\n");
                try {
                    Collection<Equipment> list = inv[i].values();
                    s.append("       ");
                    boolean first = true;
                    for (Equipment e : list) {
                        if (list.size() > 1) {
                            if (!first) s.append(" /");
                            s.append(String.format(" %.1f%% ", 100f * e.dmg / e.totalDmg.get()));
                            s.append(IdToAsset.objectName(e.id));
                        } else {
                            s.append(" ");
                            s.append(IdToAsset.objectName(e.id));
                        }
                        first = false;
                    }
                } catch (AssetMissingException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return s.toString();
        }

        return "";
    }

    public static String display(Entity entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.name()).append(" HP: ").append(entity.maxHp()).append("\n");
        List<Damage> playerDamageList = entity.getPlayerDamageList();
        int counter = 0;
        for (Damage dmg : playerDamageList) {
            counter++;
            String name = dmg.owner.getStatName();
            if (DpsDisplayOptions.nameFilter && DpsDisplayOptions.filteredStrings.length > 0) {
                boolean found = false;
                for (String n : DpsDisplayOptions.filteredStrings) {
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
            if (Damage.dammahCountered && dmg.chancellorDammahDmg) {
                extra = String.format("[Dammah Counter Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            } else if (dmg.walledGardenReflectors) {
                extra = String.format("[Garden Counter Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            }
            String inv = showInv(DpsDisplayOptions.equipmentOption, dmg.owner, entity);
            sb.append(String.format("%s %3d %10s DMG: %7d %6.3f%% %s %s\n", isMe, counter, name, dmg.damage, pers, extra, inv));
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Class used to display player Equipment
     */
    private static class Equipment {
        int id;
        int count;
        int dmg;
        AtomicInteger totalDmg;

        public Equipment(int id, AtomicInteger tot) {
            this.id = id;
            count = 0;
            totalDmg = tot;
        }

        public void add(int damage) {
            this.dmg += damage;
            count++;
            totalDmg.set(totalDmg.get() + damage);
        }
    }
}
