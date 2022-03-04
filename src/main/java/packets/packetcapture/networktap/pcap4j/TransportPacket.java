/*_##########################################################################
  _##
  _##  Copyright (C) 2019  Pcap4J.org
  _##
  _##########################################################################
*/

package packets.packetcapture.networktap.pcap4j;

/**
 * Transport layer packet (e.g. TCP and UDP)
 *
 * @author Ferran Altimiras
 * @since pcap4j 1.7.5
 */
public interface TransportPacket extends Packet {

  @Override
  public TransportHeader getHeader();

  /**
   * The interface representing the Transport layer packet's header.
   *
   * @author Ferran Altimiras
   * @since pcap4j 1.7.5
   */
  public interface TransportHeader extends Header {

    /** @return Source port */
    public Port getSrcPort();

    /** @return Destination port */
    public Port getDstPort();
  }
}
