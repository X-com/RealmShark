package packets.packetcapture.logger;

import packets.PacketType;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class used to log data traffic over the wire
 * <p>
 * TODO: clean up this mess of a class
 */
public class PacketLogger {
    private static final String[] suffix = {"B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
    private long time;
    private final int slotIntervalInSeconds = 60;
    private Log[] timeSlotsIn;
    private Log[] timeSlotsOut;
    private Log inTotal;
    private Log outTotal;
    private HashMap<Byte, Log> packets = new HashMap<>();
    private int inInterval = -1;
    private int outInterval = -1;

    /**
     * Used when starting the sniffer and resetting all the logging data.
     */
    public void startLogger() {
        time = System.currentTimeMillis();
        timeSlotsIn = new Log[slotIntervalInSeconds];
        timeSlotsOut = new Log[slotIntervalInSeconds];
        for (int i = 0; i < slotIntervalInSeconds; i++) timeSlotsIn[i] = new Log();
        for (int i = 0; i < slotIntervalInSeconds; i++) timeSlotsOut[i] = new Log();
        packets.clear();
        inTotal = new Log(0);
        outTotal = new Log(0);
    }

    /**
     * Add amount of data that is incoming from realm servers.
     *
     * @param length Number of bytes, only the TCP packet.
     */
    public void addIncoming(int length) {
        length += 38; // Add IP and Ethernet header bytes as well.
        inTotal.add(length);
        int interval = getInterval();
        if (interval != inInterval) {
            inInterval = interval;
            timeSlotsIn[interval].set(length);
        } else {
            timeSlotsIn[interval].add(length);
        }
    }

    /**
     * Add amount of data that is outgoing to realm servers.
     *
     * @param length Number of bytes, only the TCP packet.
     */
    public void addOutgoing(int length) {
        length += 58; // Add TCP (20 bytes) + IP (20 bytes) + Ethernet (18 bytes) header and tail bytes as well.
        outTotal.add(length);
        int interval = getInterval();
        if (interval != outInterval) {
            outInterval = interval;
            timeSlotsOut[interval].set(length);
        } else {
            timeSlotsOut[interval].add(length);
        }
    }

    /**
     * Get a time interval in seconds based on the set interval "slotIntervalInSeconds"
     *
     * @return The time interval based on "slotIntervalInSeconds"
     */
    private int getInterval() {
        return ((int) (System.currentTimeMillis() / 1000) % slotIntervalInSeconds);
    }

    /**
     * Adds Number of bytes to the specified type of packet received
     *
     * @param type Type of packet being logged
     * @param size Number of bytes the specified type has
     */
    public void addPacket(byte type, int size) {
        if (packets.containsKey(type)) {
            packets.get(type).add(size);
        } else {
            packets.put(type, new Log(type, size));
        }
    }

    /**
     * Converts current time milliseconds to readable time.
     *
     * @return String of readable time from System.currentTimeMillis()
     */
    private String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date date = new Date(time);
        return sdf.format(date);
    }

    /**
     * Gets the data in the allocated log array.
     *
     * @param inout Log array to be computed the data from.
     * @return A merged Log object with both number of bytes (size)
     * and the amount of packets merged into one Log object.
     */
    private Log getMinData(Log[] inout) {
        Log min = new Log();
        for (Log l : inout) {
            min.merge(l);
        }
        return min;
    }

    /**
     * @return Text output of all logged data.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Started " + getTime() + "\n");

        sb.append("\n");
        sb.append("Incoming " + inTotal + "\n");
        sb.append(getMinData(timeSlotsIn) + " per min\n");

        sb.append("\n");
        sb.append("Outgoing " + outTotal + "\n");
        sb.append(getMinData(timeSlotsOut) + " per min\n");

        sb.append("\n");
        sb.append("Packets\n");

        packets.values().stream().sorted().forEach(l -> sb.append(l));

        return sb.toString();
    }

    /**
     * Log class to keep track of incoming number of bytes and the total number of packets.
     */
    class Log implements Comparable<Log> {
        private int count;
        private int size;
        private byte type;

        public Log() {
            count = 0;
            size = 0;
            type = -1;
        }

        public Log(int s) {
            count = 0;
            size = s;
            type = -1;
        }

        public Log(byte t, int s) {
            count = 1;
            size = s;
            type = t;
        }

        void add(int s) {
            count++;
            size += s;
        }

        public void set(int s) {
            count = 0;
            size = s;
        }

        public void merge(Log l) {
            count += l.count;
            size += l.size;
        }

        public String toString() {
            int suffix = 0;
            float bytes = (float) size;
            while (bytes > 5000) {
                suffix++;
                bytes /= 1000;
            }

            String s;
            if (suffix == 0) s = size + " " + PacketLogger.suffix[suffix];
            else s = String.format("%.2f %s", bytes, PacketLogger.suffix[suffix]);

            String t = "";
            if (type != -1) {
                return String.format("Num:%5d Siz: %s %s\n", count, s, PacketType.byOrdinal(type).toString());
            } else {
                return String.format("Num:%d Siz: %s", count, s);
            }
        }

        @Override
        public int compareTo(Log o) {
            return o.size - size;
        }
    }
}
