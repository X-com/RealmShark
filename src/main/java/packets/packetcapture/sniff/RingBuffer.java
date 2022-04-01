/**
 * Class made by PrgmTrouble. Big thanks to him for implementing it.
 */

package packets.packetcapture.sniff;

/**
 * This is a simple Ring buffer made to store buffered packets as captured
 * by the sniffer. It stores elements in an array with two indesies traversing
 * the array. The indexes wrap arround to zero when hitting the limit of the
 * array. The first index is the write index and the second the read index.
 * Write adds elements and increments the counter while the read does the same
 * when reading the elements. The buffer also doubles in size if the ring buffer
 * is full and copies the elements into the newly created buffer of double the
 * size in the corresponding locations in the new array.
 *
 * @param <T> Generic type, in Sniffer.java its used to store TcpPackets
 */
public class RingBuffer<T> {

    private static final byte EMPTY = 0, NORMAL = 1, FULL = 2;
    private T[] buffer;
    private int readPointer = 0, writePointer = 0;
    private byte state = EMPTY;

    /**
     * Constructor with initial capacity
     *
     * @param capacity Initial capacity of buffer size.
     */
    public RingBuffer(int capacity) {
        buffer = (T[]) new Object[capacity];
    }

    /**
     * Is empty check
     *
     * @return True if buffer is empty.
     */
    public synchronized boolean isEmpty() {
        return EMPTY == state;
    }

    /**
     * Returns the number of elements currently in the buffer.
     *
     * @return The number of buffered elements in the buffer.
     */
    public synchronized int size() {
        if (readPointer > writePointer) {
            return writePointer + (buffer.length - readPointer);
        } else {
            return writePointer - readPointer;
        }
    }

    /**
     * Puts objects into the ring buffer.
     * If the ring buffer fills up to the max,
     * doubles the size of the buffer.
     *
     * @param item Items to be inserted into the buffer.
     */
    public synchronized void push(T item) {
        if ((writePointer + 1) % buffer.length == readPointer) {
            state = FULL;
        } else {
            if (state == FULL) {
                T[] next = (T[]) new Object[buffer.length << 1];
                /*
                    [-----[writePointer,readPointer]-------]
                    start from zero to writePointer or readPointer given they
                    are the same point and write it to the new array.
                 */
                System.arraycopy(buffer, 0, next, 0, writePointer);
                /*
                    Write also from writePointer to the end of the old array.
                    into new
                 */
                System.arraycopy(buffer, writePointer, next, buffer.length + writePointer, buffer.length - writePointer);
                readPointer += buffer.length;
                buffer = next;
            }
            state = NORMAL;
        }
        writePointer = writePointer % buffer.length;
        buffer[writePointer++] = item;
    }

    /**
     * Removes and returns the oldest entry into the buffer.
     * The elements are extracted as per FIFO
     *
     * @return Returns the oldest element in the buffer and removes it.
     */
    public synchronized T pop() {
        if (readPointer + 1 == writePointer) {
            state = EMPTY;
        } else if (state == EMPTY) {
            return null;
        } else {
            state = NORMAL;
        }
        T buf = buffer[readPointer];
        readPointer = (readPointer + 1) % buffer.length;
        return buf;
    }
}