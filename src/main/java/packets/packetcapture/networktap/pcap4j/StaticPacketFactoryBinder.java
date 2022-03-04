/*_##########################################################################
  _##
  _##  Copyright (C) 2013-2019 Pcap4J.org
  _##
  _##########################################################################
*/

package packets.packetcapture.networktap.pcap4j;

/**
 * @author Kaito Yamada
 * @since pcap4j 1.8.0
 */
final class StaticPacketFactoryBinder implements PacketFactoryBinder {

  private static final PacketFactoryBinder INSTANCE = new StaticPacketFactoryBinder();

  private StaticPacketFactoryBinder() {}

  public static PacketFactoryBinder getInstance() {
    return INSTANCE;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T, N extends NamedNumber<?, ?>> PacketFactory<T, N> getPacketFactory(
      Class<T> targetClass, Class<N> numberClass) {
    switch (targetClass.getName()) {
      case "packets.packetcapture.networktap.pcap4j.Packet":
        switch (numberClass.getName()) {
          case "packets.packetcapture.networktap.pcap4j.DataLinkType":
            return (PacketFactory<T, N>) StaticDataLinkTypePacketFactory.getInstance();
          case "packets.packetcapture.networktap.pcap4j.EtherType":
            return (PacketFactory<T, N>) StaticEtherTypePacketFactory.getInstance();
          case "packets.packetcapture.networktap.pcap4j.IpNumber":
            return (PacketFactory<T, N>) StaticIpNumberPacketFactory.getInstance();
          default:
            return (PacketFactory<T, N>) StaticUnknownPacketFactory.getInstance();
        }
      case "packets.packetcapture.networktap.pcap4j.IpV4Packet$IpV4Tos":
        return (PacketFactory<T, N>) StaticIpV4TosFactory.getInstance();
      default:
        throw new IllegalStateException("Unsupported target: " + targetClass);
    }
  }
}
