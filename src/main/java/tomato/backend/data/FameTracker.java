package tomato.backend.data;

import tomato.gui.fame.FameTrackerGUI;

import java.util.ArrayList;

public class FameTracker {
    private static ArrayList<Long> fameList = new ArrayList<>();
    private static long lastExp;

    public static void trackFame(int charId, long exp, long time) {

        long fame = (exp + 40071) / 2000;
//        if (exp != lastExp) {
//        System.out.println(time + " " + exp);
            lastExp = exp;
            fameList.add(time);
            fameList.add(exp);

            FameTrackerGUI.updateFame(charId, fame, time);
//        }
    }
}
