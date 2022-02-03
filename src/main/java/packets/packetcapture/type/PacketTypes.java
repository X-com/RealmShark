package packets.packetcapture.type;

import packets.Packet.IPacket;
import packets.incoming.depricated.Ping;

import java.util.HashMap;

/**
 * Packet are matched with the packet index sent as a header of packets and returned.
 */
public class PacketTypes {

    private static HashMap<Integer, IPacket> PACKET_TYPE = new HashMap<>();

    static {
        PACKET_TYPE.put(8, Ping::new);
    }

    /**
     * Retrieves the packet type from the PACKET_TYPE list.
     *
     * @param type Index of the packet needing to be retrieved.
     * @return Returns the interface IPacket of the class being retrieved.
     */
    public static IPacket getPacket(int type) {
        return PACKET_TYPE.get(type);
    }

    /**
     * Checks if packet type exists in the PACKET_TYPE list.
     *
     * @param type Index of the packet.
     * @return Returns if the packet exists in the list of packets in PACKET_TYPE.
     */
    public static boolean containsKey(int type) {
        return PACKET_TYPE.containsKey(type);
    }
}
