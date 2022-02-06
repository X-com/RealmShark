package packets.buffer.data;

import packets.buffer.PBuffer;

/**
 * DECA integer compression
 */
public class CompressedInt {

    /**
     * Deserialize for compressed integer.
     *
     * @param buffer The data that needs deserializing
     * @return Deserialized integer.
     */
    public int deserialize(PBuffer buffer) {
        int uByte = buffer.readUnsignedByte();
        boolean isNegative = (uByte & 64) != 0;
        int shift = 6;
        int value = uByte & 63;

        while ((uByte & 128) == 128) {
            uByte = buffer.readUnsignedByte();
            value = value | (uByte & 127) << shift;
            shift += 7;
        }

        if (isNegative) {
            value = -value;
        }
        return value;
    }
}
