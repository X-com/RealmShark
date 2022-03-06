/*
 * Copyright (c) 2020-2021 Pcap Project
 * SPDX-License-Identifier: MIT OR Apache-2.0
 */
package packets.packetcapture.networktap.ardikars;

import pcap.spi.Interface;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DefaultInterfaceIterator implements Iterator<Interface> {

    private Interface next;

    DefaultInterfaceIterator(Interface next) {
        this.next = next;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Interface next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Interface previous = next;
        next = next.next();
        return previous;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
