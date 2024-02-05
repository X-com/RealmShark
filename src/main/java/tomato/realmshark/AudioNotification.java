package tomato.realmshark;

import tomato.gui.keypop.KeypopGUI;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AudioNotification {

    private static final String NOTIFICATION_SOUND_FILE = "/sound/Notification.wav";
    private static Clip notificationSoundClip;

    /**
     * Loads auto clip to be played later
     */
    static {
        try {
            InputStream audioInputStream = KeypopGUI.class.getResourceAsStream(NOTIFICATION_SOUND_FILE);

            if (audioInputStream == null) {
                System.err.println("Error: Could not load audio file.");
            } else {
                InputStream bufferedIn = new BufferedInputStream(audioInputStream);
                AudioInputStream stream = AudioSystem.getAudioInputStream(bufferedIn);

                AudioFormat baseFormat = stream.getFormat();
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        44100,  // Sample rate (Hz)
                        16,     // Bit depth
                        1,      // Channels (1 for mono, 2 for stereo)
                        2,      // Frame size in bytes
                        44100,  // Frame rate (frames per second)
                        false   // Big-endian byte order
                );

                if (!AudioSystem.isConversionSupported(decodedFormat, baseFormat)) {
                    System.err.println("Error: Conversion not supported.");
                } else {
                    AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, stream);
                    notificationSoundClip = AudioSystem.getClip();
                    notificationSoundClip.open(decodedStream);
                }
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays loaded sound
     */
    public static void playNotificationSound() {
        if (notificationSoundClip != null) {
            notificationSoundClip.setFramePosition(0); // Rewind to the beginning
            notificationSoundClip.start();
        }
    }

    /**
     * Stops sound being played.
     */
    public static void stopNotificationSound() {
        if (notificationSoundClip != null && notificationSoundClip.isRunning()) {
            notificationSoundClip.stop();
        }
    }
}
