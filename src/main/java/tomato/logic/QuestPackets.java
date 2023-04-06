package tomato.logic;

import packets.Packet;
import packets.data.QuestData;
import packets.incoming.QuestFetchResponsePacket;
import packets.outgoing.HelloPacket;
import tomato.Tomato;
import tomato.gui.TomatoGUI;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Handles quest packets when entering daily quest room.
 * As a side bonus, handles character list data at the same time.
 */
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

    /**
     * Handles character data by sending char list request to rotmg servers while in the daily quest room.
     * This is done here given pet yard and daily quest instance is the only instances where the char list
     * request can be done without being rejected by rotmg servers.
     */
    private static void getCharList() {
        try {
            String s = HttpCharListRequest.getChartList(token);
            Tomato.updateCharacterData(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
