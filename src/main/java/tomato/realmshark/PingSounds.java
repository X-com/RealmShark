package tomato.realmshark;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import util.PropertiesManager;

/**
 * Per-category ping sounds.
 *
 * Every alert used to share the single Sound.custom clip, so a ping told you
 * that something happened but not what. Each category now owns its own
 * configurable sound.
 *
 * Sounds are chosen from the Windows notification library (C:\Windows\Media)
 * plus Tomato's bundled sound folder, and any other .wav can be pointed at
 * directly. Selections persist per category as an absolute path.
 *
 * Clips are loaded lazily and cached; changing a path drops the cached clip so
 * the next play picks the new file up.
 */
public class PingSounds {

    /** Windows ships ~70 notification wavs here. Absent on mac/linux. */
    private static final String WINDOWS_MEDIA_DIR = "C:/Windows/Media";

    /** Tomato's own bundled sounds, relative to the working directory. */
    private static final String BUNDLED_SOUND_DIR = "sound";

    /** Used when nothing else resolves - the historical shared ping sound. */
    private static final String FALLBACK = "sound/custom.wav";

    public enum Type {
        ENCHANT("pingSound.enchant", "Enchant ping", "Windows Notify.wav"),
        DUNGEON("pingSound.dungeon", "Dungeon ping", "Windows Ding.wav"),
        ITEM("pingSound.item", "Item ping", "chimes.wav"),
        ENTITY("pingSound.entity", "Entity ID ping", "Windows Balloon.wav");

        public final String key;
        public final String label;
        /** Preferred Windows sound; only used if that file actually exists. */
        private final String preferredDefault;

        Type(String key, String label, String preferredDefault) {
            this.key = key;
            this.label = label;
            this.preferredDefault = preferredDefault;
        }
    }

    private static final Map<Type, Sound> CACHE = new EnumMap<>(Type.class);

    private PingSounds() {}

    /**
     * Configured sound file for a category.
     *
     * Falls back to a distinct Windows notification sound so the categories are
     * audibly different out of the box - which is the whole point - and to the
     * bundled clip when those are unavailable (non-Windows, or trimmed install).
     */
    public static String getPath(Type type) {
        String saved = PropertiesManager.getProperty(type.key);
        if (saved != null && !saved.trim().isEmpty()) {
            return saved.trim();
        }
        File preferred = new File(WINDOWS_MEDIA_DIR, type.preferredDefault);
        if (preferred.isFile()) {
            return preferred.getAbsolutePath();
        }
        return FALLBACK;
    }

    /**
     * Sets the sound file for a category and drops the cached clip.
     *
     * @param path Absolute path to a .wav, or null/empty to restore the default.
     */
    public static void setPath(Type type, String path) {
        PropertiesManager.setProperties(type.key, path == null ? "" : path);
        synchronized (CACHE) {
            CACHE.remove(type);
        }
    }

    /**
     * Plays the ping for a category. Never throws - a bad sound file must not
     * break loot handling.
     */
    public static void play(Type type) {
        try {
            Sound s = get(type);
            if (s != null) s.play();
        } catch (Throwable t) {
            System.out.println(
                "[PingSounds] failed to play " + type.label + ": " + t
            );
        }
    }

    private static Sound get(Type type) {
        synchronized (CACHE) {
            Sound cached = CACHE.get(type);
            if (cached != null) return cached;

            String path = getPath(type);
            Sound s = new Sound(path);
            if (!s.isLoaded() && !FALLBACK.equals(path)) {
                // Configured file is missing or an unsupported format.
                System.out.println(
                    "[PingSounds] could not load \"" + path +
                    "\" for " + type.label + ", using default sound"
                );
                s = new Sound(FALLBACK);
            }
            CACHE.put(type, s);
            return s;
        }
    }

    /** Forces every clip to reload on next play. */
    public static void clearCache() {
        synchronized (CACHE) {
            CACHE.clear();
        }
    }

    /**
     * Selectable sounds: Tomato's bundled clips first, then the Windows
     * notification library.
     *
     * @return Absolute paths of every .wav found.
     */
    public static List<String> availableSounds() {
        List<String> out = new ArrayList<>();
        addWavsFrom(new File(BUNDLED_SOUND_DIR), out);
        addWavsFrom(new File(WINDOWS_MEDIA_DIR), out);
        return out;
    }

    private static void addWavsFrom(File dir, List<String> out) {
        if (dir == null || !dir.isDirectory()) return;
        File[] files = dir.listFiles((d, n) ->
            n.toLowerCase().endsWith(".wav")
        );
        if (files == null) return;
        Arrays.sort(files, Comparator.comparing(f -> f.getName().toLowerCase()));
        for (File f : files) {
            out.add(f.getAbsolutePath());
        }
    }

    /** Trims a path down to just the file name, for display. */
    public static String displayName(String path) {
        if (path == null || path.isEmpty()) return "(default)";
        return new File(path).getName();
    }
}
