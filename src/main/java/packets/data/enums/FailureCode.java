package packets.data.enums;

/**
 * The error codes of messages which can be received in the FailurePacket
 */
public enum FailureCode {
    /**
     * Received when the game version sent in the HelloPacket is not updated
     */
    IncorrectVersion(4),
    /**
     * Received when an incorrect key is sent in the HelloPacket
     */
    BadKey(5),
    /**
     * Received when the target of a TeleportPacket was not a valid target
     */
    InvalidTeleportTarget(6),
    /**
     * Received when the account that has connected does not have a verified email
     */
    EmailVerificationNeeded(7),
    /**
     * Received on teleport when the client has the non-guild cooldown
     */
    TeleportRealmBlock(9),
    /**
     * Received when the client enters the wrong server
     */
    WrongServerEntered(10),
    /**
     * Received when the server is full or you try enter an area without a valid reconnect key
     */
    ServerFull(11),
    /**
     * Received when the server the client enters has a queue
     */
    ServerQueue(15);

    private final int index;

    FailureCode(int i) {
        index = i;
    }

    public static FailureCode byOrdinal(int ord) {
        for (FailureCode o : FailureCode.values()) {
            if (o.index == ord) {
                return o;
            }
        }
        return null;
    }
}
