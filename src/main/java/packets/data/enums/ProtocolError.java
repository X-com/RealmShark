package packets.data.enums;

import java.io.Serializable;

/**
 * Most possible protocol error codes and their meaning
 */
public enum ProtocolError implements Serializable {
    /**
     * Received if you send a MOVE packet not in response to a new tick
     */
    IncorrectMove(5),
    /**
     * Received if you send a pong packet not in response to a ping
     */
    IncorrectPong(9),
    /**
     * Received if a pong packet is sent with an invalid serial number
     */
    IncorrectPongSerial(10),
    /**
     * Received if you send an UPDATEACK not in response to an UPDATE
     */
    IncorrectUpdateAck(11),
    /**
     * Received if you send a HELLO packet while already in-game
     */
    IncorrectHello(15),
    /**
     * Received when an ACK packet is not sent (ping), goto), newtick), update)
     */
    IgnoredAck(21),
    /**
     * Received when too many packets are sent in a short duration (1200+ at once)
     */
    TooManyPackets(42),
    /**
     * Received if there are too many in-game entities for the server to handle
     */
    TooManyEntities(48),
    /**
     * Received when sending packets too quickly after getting "action not permitted at the moment" in-game
     */
    RateLimit(64);

    private final int index;

    ProtocolError(int i) {
        index = i;
    }

    public static ProtocolError byOrdinal(int ord) {
        for (ProtocolError o : ProtocolError.values()) {
            if (o.index == ord) {
                return o;
            }
        }
        return null;
    }
}
