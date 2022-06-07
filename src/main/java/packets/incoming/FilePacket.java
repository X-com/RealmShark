package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

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
    public void deserialize(BufferReader buffer) throws Exception {
        fileName = buffer.readString();
        file = buffer.readStringUTF32();
    }

    @Override
    public String toString() {
        return "FilePacket{" +
                "\n   fileName=" + fileName +
                "\n   file=" + file;
    }
}