package bugfixingtools;

import assets.AssetMissingException;
import assets.IdToAsset;
import packets.Packet;
import packets.data.GroundTileData;
import packets.data.ObjectData;
import packets.data.ObjectStatusData;
import packets.data.StatData;
import packets.data.enums.StatType;
import packets.incoming.*;
import packets.incoming.ip.IpAddress;
import packets.incoming.pets.ActivePetPacket;
import packets.incoming.pets.HatchPetMessage;
import packets.outgoing.*;
import packets.outgoing.pets.ActivePetUpdateRequestPacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import tomato.realmshark.enums.CharacterClass;
import util.Util;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PacketRead {
    public static int ip = 0;

    public static void main(String[] args) {
        Util.saveLogs = false;
        Register.INSTANCE.registerAll(PacketRead::readAll);
        PacketProcessor packetProcessor = new PacketProcessor();
        packetProcessor.start();
    }

    public static void readAll(Packet packet) {

//        if(true){
//            System.out.println(packet);
//            return;
//        }

        // spammy
        if (packet instanceof PingPacket) return;
        if (packet instanceof PongPacket) return;
        if (packet instanceof MovePacket) return;
        if (packet instanceof PlayerShootPacket) return;
        if (packet instanceof ServerPlayerShootPacket) return;
        if (packet instanceof UpdateAckPacket) return;
        // common
        if (packet instanceof InvResultPacket) return;
        if (packet instanceof InvDropPacket) return;
        if (packet instanceof PlaySoundPacket) return;
        if (packet instanceof ShowEffectPacket) return;
        if (packet instanceof TextPacket) return;
        if (packet instanceof ChangeAllyShootPacket) return;
        if (packet instanceof EnemyShootPacket) return;
        if (packet instanceof EnemyHitPacket) return;
        if (packet instanceof DamagePacket) return;
        if (packet instanceof OtherHitPacket) return;
        if (packet instanceof AoePacket) return;
        if (packet instanceof AoeAckPacket) return;
        if (packet instanceof PlayerHitPacket) return;
        if (packet instanceof PlayerTextPacket) return;
        if (packet instanceof GotoPacket) return;
        // death and create new char
        if (packet instanceof EditAccountListPacket) return;
        if (packet instanceof NewCharacterInfoPacket) return;
        if (packet instanceof CreatePacket) return;
        if (packet instanceof DeathPacket) return;
        // pet
        if (packet instanceof ActivePetPacket) return;
        if (packet instanceof ActivePetUpdateRequestPacket) return;
        if (packet instanceof HatchPetMessage) return;
        // trade
        if (packet instanceof TradeDonePacket) return;
        if (packet instanceof AcceptTradePacket) return;
        if (packet instanceof TradeAcceptedPacket) return;
        if (packet instanceof TradeStartPacket) return;
        if (packet instanceof RequestTradePacket) return;
        if (packet instanceof TradeRequestedPacket) return;
        // load packet
        if (packet instanceof QuestObjectIdPacket) return;
        if (packet instanceof LoadPacket) return;
        if (packet instanceof ReconnectPacket) return;
        if (packet instanceof ExaltationUpdatePacket) return;
        if (packet instanceof ShootAckCounterPacket) return;
        if (packet instanceof NotificationPacket) return;
        if (packet instanceof ForgeUnlockedBlueprints) return;
        if (packet instanceof QuestFetchResponsePacket) return;
        if (packet instanceof QueueInfoPacket) return;
        if (packet instanceof FailurePacket) return;
        if (packet instanceof HelloPacket) {
            tileCounter = 0;
            System.out.println(((HelloPacket) packet).accessToken);
            return;
        }
        // usage
        if (packet instanceof UseItemPacket) return;
        if (packet instanceof UsePortalPacket) return;
        if (packet instanceof ClientStatPacket) return;
        if (packet instanceof DashPacket) return;
        if (packet instanceof DashAckPacket) return;
        if (packet instanceof EscapePacket) return;
        if (packet instanceof InvSwapPacket) return;
        if (packet instanceof GroundDamagePacket) return;
        if (packet instanceof TeleportPacket) return;
        // unknown
        if (packet instanceof UnknownPacket139) return;
        if (packet instanceof GotoAckPacket) return;
        // RealmHeroesLeftPacket
        if (packet instanceof RealmHeroesLeftPacket) return;
        if (packet instanceof CreateSuccessPacket) return;
        if (packet instanceof CreepMoveMessagePacket) return;
        if (packet instanceof StasisPacket) return;
        if (packet instanceof IpAddress) {
            IpAddress p = (IpAddress) packet;
            ip = p.srcAddressAsInt;
            return;
        }

        if (packet instanceof NewTickPacket) {
//            newtick((NewTickPacket) packet);
//            updateEntity((NewTickPacket) packet);
            return;
        }

        if (packet instanceof MapInfoPacket) {
//            MapInfoPacket p = (MapInfoPacket) packet;
//            if (p.name.equals("Realm of the Mad God")) {
//                System.out.println(p.seed + "   " + ip);
//            }
            types.clear();
            System.out.println("clearconsole");
            return;
        }

        if (packet instanceof UpdatePacket) {
//            crystalTPRange((UpdatePacket) packet);
//            realmIdentifier((UpdatePacket) packet);
//            showPlayer((UpdatePacket) packet);
//            countTiles((UpdatePacket) packet);
//            isSeasonalCharacter((UpdatePacket) packet);
//            entityName((UpdatePacket) packet);
            return;
        }
        if (packet instanceof VaultContentPacket) {
//            countPots((VaultContentPacket) packet);
//            return;
        }

        //RealmHeroesLeftPacket

        System.out.println(packet);
    }

    static HashSet<Integer> types = new HashSet<>();

    private static void updateEntity(NewTickPacket packet) {
        for (ObjectStatusData o : packet.status) {
            if(o.objectId == 240694) {
                System.out.println(o);
            }
        }
    }

    private static void entityName(UpdatePacket packet) {
        for (ObjectData o : packet.newObjects) {
            int objectType = o.objectType;
            if (!types.contains(objectType)) {
                types.add(objectType);
                if(CharacterClass.isPlayerCharacter(objectType)) continue;
                try {
                    System.out.println(IdToAsset.objectName(objectType) + " " + objectType);
                } catch (AssetMissingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static HashMap<Integer, Boolean> playerIsSeasonal = new HashMap<>();

    private static void isSeasonalCharacter(UpdatePacket packet) {
        for (ObjectData od : packet.newObjects) {
            int type = od.objectType;
            if (isPlayer(type)) {
                int id = od.status.objectId;
                boolean isSeasonal = false;
                for (StatData sd : od.status.stats) {
                    if (sd.statType == StatType.SEASONAL) {
                        isSeasonal = sd.statValue == 1;
                    }
                }
                playerIsSeasonal.put(id, isSeasonal);
            }
        }
    }

    private static boolean isPlayer(int type) {
        return CharacterClass.isPlayerCharacter(type);
    }

    static int tileCounter = 0;

    private static void countTiles(UpdatePacket packet) {
        for (GroundTileData g : packet.tiles) {
            tileCounter++;
            System.out.println(tileCounter);
        }
    }

    // UNKNOWN24 seasonal == 1
    // UNKNOWN25 skinId
    // UNKNOWN125 animationId
    private static void showPlayer(UpdatePacket packet) {
        for (ObjectData objectData : packet.newObjects) {
            if (CharacterClass.isPlayerCharacter(objectData.objectType)) {
                System.out.println("--");
                for (int i = 0; i < objectData.status.stats.length; i++) {
                    StatData stats = objectData.status.stats[i];
                    if (stats.statType == StatType.NAME_STAT) {
                        System.out.println(stats);
//                    } else if (stats.statType == StatType.UNKNOWN23) {
//                        System.out.println(stats);
                    } else if (stats.statType == StatType.SEASONAL) {
                        System.out.println(stats);
//                    } else if(stats.statType == StatType.UNKNOWN25) {
//                        System.out.println(stats);
                    }
                }
            }
        }
    }

    static boolean log = false;
    static long time = 0;
    static long timeStored = 0;
    static long timeIndex = 0;
    static long timeAdd = 180000;


    private static void newtick(NewTickPacket packet) {
//        if (packet.serverRealTimeMS - time > 10000) {
//            System.out.println("10 sec");
//        }dwd
        time = packet.serverRealTimeMS;
        if (timeIndex == 0) timeIndex = (int) (time / 180000) - 1;

        if (timeStored < time) {
            Toolkit.getDefaultToolkit().beep();
            timeStored = timeIndex * timeAdd + 10000;
            timeIndex++;
            System.out.println(timeIndex);
        }
    }

    private static void mapinfo(MapInfoPacket packet) {
        if (packet.displayName.equals("{s.rotmg}")) {
            log = true;
        } else {
            log = false;
        }
    }

    private static void realmIdentifier(UpdatePacket packet) {
//        if (log && packet.pos1.x != 0 && packet.pos1.y != 0) {
//            DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss");
//            LocalDateTime dateTime = LocalDateTime.now();
//            System.out.printf("%s x:%f y:%f %d -map\n", dateTimeFormat.format(dateTime), packet.pos1.x, packet.pos1.y, time);
//        }
    }

    private static void countPots(VaultContentPacket p) {
        HashMap<Integer, Integer> pots = new HashMap<>();
        for (int i : p.potionContents) {
            if (pots.containsKey(i)) {
                pots.put(i, pots.get(i) + 1);
            } else {
                pots.put(i, 1);
            }
        }
        for (int i : p.vaultContents) {
            if (pots.containsKey(i)) {
                pots.put(i, pots.get(i) + 1);
            } else {
                pots.put(i, 1);
            }
        }
        for (int i : p.giftContents) {
            if (pots.containsKey(i)) {
                pots.put(i, pots.get(i) + 1);
            } else {
                pots.put(i, 1);
            }
        }
        int[] stats = new int[8];
        String[] names = {"Attack", "Defense", "Speed", "Vitality", "Wisdom", "Dexterity", "Life", "Mana"};
        for (Map.Entry<Integer, Integer> m : pots.entrySet()) {
            int k = m.getKey();
            int v = m.getValue();
            int mult = 1;
            if (k >= 9064 && k <= 9071) mult = 2;
            addPotion(stats, k, v * mult);
        }
        for (int index = 0; index < 8; index++) {
            System.out.printf("%s=%d\n", names[index], stats[index]);
        }
    }

    private static void addPotion(int[] stats, int id, int i) {
        if (2591 == id) stats[0] += i;             // Attack
        if (2592 == id) stats[1] += i;             // Defense
        if (2593 == id) stats[2] += i;             // Speed
        if (2612 == id) stats[3] += i;             // Vitality
        if (2613 == id) stats[4] += i;             // Wisdom
        if (2636 == id) stats[5] += i;             // Dexterity
        if (2793 == id) stats[6] += i;             // Life
        if (2794 == id) stats[7] += i;             // Mana
        if (5465 == id) stats[0] += i;             // Attack
        if (5466 == id) stats[1] += i;             // Defense
        if (5467 == id) stats[2] += i;             // Speed
        if (5468 == id) stats[3] += i;             // Vitality
        if (5469 == id) stats[4] += i;             // Wisdom
        if (5470 == id) stats[5] += i;             // Dexterity
        if (5471 == id) stats[6] += i;             // Life
        if (5472 == id) stats[7] += i;             // Mana
        if (9064 == id) stats[0] += i;             // Attack
        if (9065 == id) stats[1] += i;             // Defense
        if (9066 == id) stats[2] += i;             // Speed
        if (9067 == id) stats[3] += i;             // Vitality
        if (9068 == id) stats[4] += i;             // Wisdom
        if (9069 == id) stats[5] += i;             // Dexterity
        if (9070 == id) stats[6] += i;             // Life
        if (9071 == id) stats[7] += i;             // Mana
    }

    static float xsave = 0;
    static float ysave = 0;

    // did tp
    // crystal:111.85   TP spot:105.85
    // crystal:119.52   TP spot:119.49

    // not tp
    // crystal:120.04   TP spot:114.68
    // crystal:121.08   TP spot:127.08
    private static void crystalTPRange(UpdatePacket packet) {
//        float x = packet.pos1.x;
//        float y = packet.pos1.y;
//        for (ObjectData od : packet.newObjects) {
//            if (od.objectType == 10025) {
//                System.out.println(od.status.pos);
//                xsave = od.status.pos.x;
//                ysave = od.status.pos.y;
//            }
//        }
//        if (x != 0 && y != 0) {
//            System.out.printf("crystal:%.2f   TP spot:%.2f\n", dist(x, y, xsave, ysave), dist(x, y, xsave, ysave + 6));
//        }
    }

    private static double dist(float x1, float y1, float x2, float y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
}
