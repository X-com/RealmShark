package packets.packetcapture.encryption;

/**
 * Aligner for RC4 by brute forcing the alignment with known conditions. One method uses
 * a sequence of two consecutive numbers and searches for the sequence by attempting to
 * decrypt two tick packets in sequence with known bytes between the two. The other brute
 * forces text packets with a known name as the sender.
 */
public class RC4Aligner {
    public static final int SEARCH_SIZE = 10000000;

    /**
     * String to byte converter.
     *
     * @param s String needing to be converted into bytes
     * @return Returns the byte array of the string.
     */
    public static byte[] encodeString(String s) {
        byte[] ret = new byte[2 + s.length()];
        ret[1] = (byte) (s.length() & 0xff);
        ret[0] = (byte) ((s.length() >> 8) & 0xff);
        System.arraycopy(s.getBytes(), 0, ret, 2, s.length());
        return ret;
    }

    /**
     * Brute force RC4 cracker to find the alignment using a known key using a Text packet
     * with a known name.
     *
     * @param cipher   A RC4 cipher with a given key and given index.
     * @param textData Text packet data.
     * @param name     Given name of the player sending the message.
     * @return returning the index of the RC4 ciphers index from initial condition
     * constructed by key.
     */
    public static int syncCipher(RC4 cipher, byte[] textData, String name) {
        RC4 finder = cipher.fork();
        byte[] target = encodeString(name);
        int nameOffset = 5;
        int offset = -1;
        outer:
        while (offset < SEARCH_SIZE) {
            offset++;
            byte xor = finder.getXor();
            if (target[0] != (textData[nameOffset] ^ xor))
                continue;
            RC4 forked = finder.fork();
            for (int i = 1; i < target.length; i++) {
                xor = forked.getXor();
                if (target[i] != (textData[i + nameOffset] ^ xor)) {
                    continue outer;
                }
            }
            break;
        }
        if (offset == SEARCH_SIZE) {
            return -1;
        }

        cipher.skip(offset);
        return offset;
    }

    /**
     * Brute force RC4 cracker to find the alignment using a known key using two
     * consecutive Tick packets. Any tick packet sent from server have ticks incremented
     * by one from the previous tick packet. The cracker finds any index of the cipher
     * by finding A + 1 == B where B is the numeral of a tick packet directly followed by
     * a tick packet A with known bytes received between the two Tick packets.
     *
     * @param cipher A RC4 cipher with a given key and given index.
     * @param A      Tick packet A
     * @param B      Tick packet B
     * @param delta  Known packets between packet A and B
     * @return returning the index of the RC4 ciphers index from initial condition
     * constructed by key.
     */
    public static int syncCipher(RC4 cipher, byte[] A, byte[] B, int delta) {
        RC4 finderA = cipher.fork();
        RC4 finderB = cipher.fork();
        finderB.skip(delta);
        int offset = -1;
        while (offset < SEARCH_SIZE) {
            offset++;
            RC4 tmp = finderA.fork();
            int a = decodeInt(A, tmp);
            tmp = finderB.fork();
            int b = decodeInt(B, tmp);

            if ((a + 1) != b) {
                finderA.getXor();
                finderB.getXor();
                continue;
            }
            break;
        }
        if (offset == SEARCH_SIZE) {
            return -1;
        }

        cipher.skip(offset);
        return offset;
    }

    /**
     * A fast method to return an integer from the head of an array XORed with a given
     * cipher with a specific index.
     *
     * @param bytes  Byte array where integer is
     * @param cipher A RC4 cipher object with a specific index.
     * @return returning the resulting integer
     */
    private static int decodeInt(byte[] bytes, RC4 cipher) {
        return ((Byte.toUnsignedInt((byte) (bytes[0] ^ cipher.getXor()))) << 24) | ((Byte.toUnsignedInt((byte) (bytes[1] ^ cipher.getXor()))) << 16) | ((Byte.toUnsignedInt((byte) (bytes[2] ^ cipher.getXor()))) << 8) | ((Byte.toUnsignedInt((byte) (bytes[3] ^ cipher.getXor()))));
    }
}