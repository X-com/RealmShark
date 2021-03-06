package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.Arrays;

/**
 * A packet which contains a bitmap image
 */
public class PicPacket extends Packet {
    /**
     * The width of the image.
     */
    public int width;
    /**
     * The height of the image.
     */
    public int height;
    /**
     * The bitmap data of the image.
     */
    public byte[] bitmapData;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        width = buffer.readInt();
        height = buffer.readInt();
        bitmapData = buffer.readBytes(width * height * 4);
    }

    @Override
    public String toString() {
        return "PicPacket{" +
                "\n   width=" + width +
                "\n   height=" + height +
                "\n   bitmapData=" + Arrays.toString(bitmapData);
    }
}