package tomato.logic;

import packets.Packet;
import packets.data.QuestData;
import packets.incoming.QuestFetchResponsePacket;
import packets.outgoing.HelloPacket;
import tomato.gui.TomatoGUI;
import util.IdToName;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestPackets {
    private static String token;

    /**
     * Quest packets to be processed.
     *
     * @param packet The event notifier packet.
     */
    public static void questPacket(Packet packet) {
        if (packet instanceof HelloPacket) {
            HelloPacket p = (HelloPacket) packet;
            token = p.accessToken;
        } else if (packet instanceof QuestFetchResponsePacket) {
            QuestFetchResponsePacket p = (QuestFetchResponsePacket) packet;
            StringBuilder s = new StringBuilder();
            Stream<QuestData> list = Arrays.stream(p.quests).sorted(Comparator.comparing(questData -> questData.category));
            for (QuestData qd : list.collect(Collectors.toList())) {
                StringBuilder sbReqs = new StringBuilder();
                int memId = 0;
                int memCount = 0;
                boolean first = true;
                if (qd.requirements[0] == 34734 || // Ancient schematics
                        qd.requirements[0] == 34683 || // Schematics
                        qd.requirements[0] == 10023 || // Rune shield
                        qd.requirements[0] == 10024 || // Rune helm
                        qd.requirements[0] == 10022 || // Rune sword
                        qd.requirements[0] == 23547 || // Shard weapons
                        qd.requirements[0] == 23583 || // Shard armor
                        qd.requirements[0] == 32403 || // Shard ring
                        qd.requirements[0] == 23619 || // Shard ability
                        qd.requirements[0] == 47360 || // Common ore
                        qd.requirements[0] == 19210 || // Red crystal
                        qd.requirements[0] == 12055) // Blue crystal
                    continue; // ignore list

                for (int i = 0; i < qd.requirements.length; i++) {
                    int id = qd.requirements[i];
                    if (first) {
                        first = false;
                        memId = id;
                        memCount = 1;
                    } else if (memId != id) {
                        if (memCount > 1) sbReqs.append(memCount).append("x ");
                        sbReqs.append(IdToName.objectName(memId));
                        if (i < qd.requirements.length - 1) sbReqs.append(", ");
                        memId = id;
                        memCount = 1;
                    } else {
                        memCount++;
                    }
                }
                if (memCount > 1) sbReqs.append(memCount).append("x ");
                sbReqs.append(IdToName.objectName(memId));
                StringBuilder sbRew = new StringBuilder();
                for (int i = 0; i < qd.rewards.length; i++) {
                    int id = qd.rewards[i];
                    sbRew.append(IdToName.objectName(id));
                    if (i < qd.rewards.length - 1) sbRew.append(", ");
                }

                s.append(String.format("%s\n%s\n[%s] for [%s]\n\n", qd.name, qd.description, sbReqs, sbRew));
            }
            TomatoGUI.setTextAreaQuests(s.toString());

            getCharList();
        }
    }

    private static void getCharList() {
        try {
            CharList.getChartList(token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
