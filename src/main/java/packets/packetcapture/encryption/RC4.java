package packets.packetcapture.encryption;

import util.Util;

import java.nio.ByteBuffer;

/**
 * RC4 cipher used to decrypt packets.
 */
public class RC4 {
    int[] state = new int[256], initState = new int[256];
    int i;
    int j;

    /**
     * Constructor of RC4 needing a string key.
     *
     * @param key A key in the form of a string.
     */
    public RC4(String key) {
        this(Util.hexStringToByteArray(key));
    }

    /**
     * A RC4 object with specific internal states.
     *
     * @param state     The current state of the key.
     * @param initState The initial state of the key.
     * @param i         The i index of the RC4 state.
     * @param j         The j index of the RC4 state.
     */
    private RC4(int[] state, int[] initState, int i, int j) {
        this.state = state;
        this.initState = initState;
        this.i = i;
        this.j = j;
    }

    /**
     * Constructor of RC4 class needing a hex-number-key in a byte array.
     *
     * @param key Key in the form of hex numbers in a byte array.
     */
    public RC4(byte[] key) {
        i = 0;
        j = 0;

        for (int i = 0; i < 256; i++) {
            state[i] = i;
        }

        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + state[i] + Byte.toUnsignedInt(key[i % key.length])) % 256;
            int tmp = state[i];
            state[i] = state[j];
            state[j] = tmp;
            // state[i] ^= state[j] ^= state[i] ^= state[j];
        }
        System.arraycopy(state, 0, initState, 0, state.length);
    }

    /**
     * Returning the result of Xor:ing the byte with the RC4 cipher. This also results in
     * incrementing the internal state by 1.
     *
     * @return The resulting byte after Xor:ing with the cipher.
     */
    public synchronized byte getXor() {
        i = (i + 1) % 256;
        j = (j + state[i]) % 256;
        int tmp = state[i];
        state[i] = state[j];
        state[j] = tmp;
        return (byte) state[(state[i] + state[j]) % 256];
    }

    /**
     * A crude brute force skip method to increment the ciphers internal state by an amount.
     *
     * @param amount The amount needed to increment the cipher.
     * @return Returning this object for inlining.
     */
    public RC4 skip(int amount) {
        for (int k = 0; k < amount; k++) {
            i = (i + 1) % 256;
            j = (j + state[i]) % 256;
            int tmp = state[i];
            state[i] = state[j];
            state[j] = tmp;
        }
        return this;
    }

    /**
     * Decrypting the bytes in an array with the cipher. Then directly inserting the decrypted
     * bytes back into the same array with same index.
     *
     * @param offset Offset from the start of the array needing to be decrypted.
     * @param array  Array with bytes needing decrypting.
     */
    public void decrypt(int offset, byte[] array) {
        for (int b = offset; b < array.length; b++) {
            array[b] = (byte) (array[b] ^ getXor());
        }
    }

    /**
     * Decrypting the bytes in an array with the cipher. Then directly inserting the decrypted
     * bytes back into the same array with same index.
     *
     * @param array Array with bytes needing decrypting.
     */
    public void decrypt(byte[] array) {
        decrypt(0, array);
    }

    /**
     * Decrypting the bytes in a ByteBuffer with the cipher. First extracting the byte array
     * out of the ByteBuffer object and mutating it (for speed purposes) the array decrypted
     * bytes back into the same array with same index. This will directly modify the ByteBuffer.
     *
     * @param offset     Offset from the start of the array needing to be decrypted.
     * @param byteBuffer ByteBuffer with bytes needing decrypting.
     */
    public void decrypt(int offset, ByteBuffer byteBuffer) {
        byte[] array = byteBuffer.array();
        decrypt(offset, array);
    }

    public void decrypt(ByteBuffer byteBuffer) {
        decrypt(0, byteBuffer);
    }

    public RC4 fork() {
        int[] state2 = new int[state.length];
        System.arraycopy(state, 0, state2, 0, state2.length);
        return new RC4(state2, initState, i, j);
    }

    public void reset() {
        System.arraycopy(initState, 0, state, 0, state.length);
        i = 0;
        j = 0;
    }

    public static int convertByteArrayToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
}