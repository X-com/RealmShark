package tomato.logic;

import packets.Packet;
import packets.data.QuestData;
import packets.incoming.QuestFetchResponsePacket;
import packets.outgoing.HelloPacket;
import tomato.gui.TomatoGUI;
import assets.AssetMissingException;
import assets.IdToAsset;

import java.io.IOException;
import java.util.ArrayList;
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
            Stream<QuestData> list = Arrays.stream(p.quests).sorted(Comparator.comparing(questData -> questData.category));
            TomatoGUI.updateQuests(list.toArray(QuestData[]::new));

            getCharList();
        }
    }

    private static void getCharList() {
        try {
            String s = CharList.getChartList(token);
            System.out.println("s: " + s);
            ArrayList<Character> l = CharList.getCharList(s);
            TomatoGUI.updateCharacters(l);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
