package packets.packetcapture.networktap;

import pcap.spi.Address;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DefaultAddressIterator implements Iterator<Address> {
    private Address next;

    DefaultAddressIterator(Address next) {
        this.next = next;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Address next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Address previous = next;
        next = next.next();
        return previous;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
