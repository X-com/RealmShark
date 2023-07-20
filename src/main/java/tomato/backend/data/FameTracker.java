package tomato.backend.data;

import tomato.gui.fame.FameTrackerGUI;

import java.util.ArrayList;

public class FameTracker {
    private static ArrayList<Long> fameList = new ArrayList<>();
    private static long lastFame;

    public static void trackFame(int charId, long exp, long time) {

        long fame = (exp + 40071) / 2000;
        if (fame != lastFame) {
            time = System.currentTimeMillis();
//            System.out.println(time + " " + exp + " " + fame);
            lastFame = fame;
//            fameList.add(time);
//            fameList.add(exp);

            FameTrackerGUI.updateFame(charId, fame, time);
        }
    }
}
