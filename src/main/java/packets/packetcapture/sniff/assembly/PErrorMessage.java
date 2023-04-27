package packets.packetcapture.sniff.assembly;

/**
 * Called on errors when constructing the TCP stream.
 */
public interface PErrorMessage {

    /**
     * Called when error messages are sent.
     *
     * @param error Error message
     * @param dump  Error dump for debugger
     */
    void errorLogs(String error, String dump);
}
