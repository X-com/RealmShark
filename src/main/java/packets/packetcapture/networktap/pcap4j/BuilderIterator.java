/*_##########################################################################
  _##
  _##  Copyright (C) 2012  Pcap4J.org
  _##
  _##########################################################################
*/

package packets.packetcapture.networktap.pcap4j;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Kaito Yamada
 * @since pcap4j 0.9.9
 */
public final class BuilderIterator implements Iterator<Packet.Builder> {

    private Packet.Builder next;
    private Packet.Builder previous = null;

    /**
     * @param b b
     */
    public BuilderIterator(Packet.Builder b) {
        this.next = b;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Packet.Builder next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        previous = next;
        next = next.getPayloadBuilder();

        return previous;
    }

    /**
     * @throws UnsupportedOperationException always.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
