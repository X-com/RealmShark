package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * A packet which contains a file.
 */
public class FilePacket extends Packet {
    /**
     * The name of the received file.
     */
    public String fileName;
    /**
     * The bytes of the file. Don't ask me why this is a String,
     * that's just how it is in the source code of the game.
     */
    public String file;

    @Override
    public void deserialize(PBuffer buffer) {
        fileName = buffer.readString();
        file = buffer.readStringUTF32();
    }
}