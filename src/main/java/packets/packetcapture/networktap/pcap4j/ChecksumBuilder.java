/*_##########################################################################
  _##
  _##  Copyright (C) 2012 Pcap4J.org
  _##
  _##########################################################################
*/

package packets.packetcapture.networktap.pcap4j;

/**
 * @author Kaito Yamada
 * @since pcap4j 0.9.11
 * @param <T> the type of object this builder builds.
 */
public interface ChecksumBuilder<T> {

  /**
   * @param correctChecksumAtBuild correctChecksumAtBuild
   * @return ChecksumBuilder
   */
  public ChecksumBuilder<T> correctChecksumAtBuild(boolean correctChecksumAtBuild);

  /** @return a new object. */
  public T build();
}
