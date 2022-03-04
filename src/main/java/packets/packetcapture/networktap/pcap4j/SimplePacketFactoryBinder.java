/*_##########################################################################
  _##
  _##  Copyright (C) 2013-2014  Pcap4J.org
  _##
  _##########################################################################
*/

package packets.packetcapture.networktap.pcap4j;

/**
 * @author Kaito Yamada
 * @since pcap4j 0.9.16
 */
final class SimplePacketFactoryBinder {

  private static final SimplePacketFactoryBinder INSTANCE = new SimplePacketFactoryBinder();

  private SimplePacketFactoryBinder() {}

  public static SimplePacketFactoryBinder getInstance() {
    return INSTANCE;
  }

  @SuppressWarnings("unchecked")
  public <T, N extends NamedNumber<?, ?>> PacketFactory<T, N> getPacketFactory(
      Class<T> targetClass, Class<N> numberClass) {
    if (Packet.class.isAssignableFrom(targetClass)) {
      return (PacketFactory<T, N>) StaticUnknownPacketFactory.getInstance();
    } else {
      StringBuilder sb = new StringBuilder(100);
      sb.append("targetClass: ").append(targetClass).append(" numberClass: ").append(numberClass);
      throw new IllegalArgumentException(sb.toString());
    }
  }
}
