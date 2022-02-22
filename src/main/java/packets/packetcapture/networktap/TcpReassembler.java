package packets.packetcapture.networktap;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.namednumber.TcpPort;
import org.pcap4j.util.NifSelector;

public class TcpReassembler {

    public static void main(String[] args) throws PcapNativeException, NotOpenException {
        PcapNetworkInterface nif;
        try {
            nif = new NifSelector().selectNetworkInterface();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (nif == null) {
            return;
        }

        PcapHandle handle = nif.openLive(65536, PromiscuousMode.PROMISCUOUS, 10);

        handle.setFilter(
                "tcp port 443",
                BpfCompileMode.OPTIMIZE
        );

        Map<TcpPort, TcpSession> sessions = new HashMap<TcpPort, TcpSession>();
        while (true) {
            try {
                Packet packet = handle.getNextPacketEx();
                TcpPacket tcp = packet.get(TcpPacket.class);
                if (tcp == null) {
                    continue;
                }

                boolean isToServer = true;
                TcpPort port = tcp.getHeader().getSrcPort();
                if (port.value() == 443) {
                    port = tcp.getHeader().getDstPort();
                    isToServer = false;
                }

                boolean syn = tcp.getHeader().getSyn();
                boolean fin = tcp.getHeader().getFin();

                if (syn) {
                    TcpSession session;
                    if (isToServer) {
                        session = new TcpSession();
                        sessions.put(port, session);
                    }
                    else {
                        session = sessions.get(port);
                    }

                    long seq = tcp.getHeader().getSequenceNumberAsLong();
                    session.setSeqNumOffset(isToServer, seq + 1L);

                }
                else if (fin) {
                    TcpSession session = sessions.get(port);
                    session.getPackets(isToServer).add(tcp);

//                    byte[] reassembledPayload
//                            = doReassemble(
//                            session.getPackets(isToServer),
//                            session.getSeqNumOffset(isToServer),
//                            tcp.getHeader().getSequenceNumberAsLong(),
//                            tcp.getPayload().length()
//                    );
//
//                    int len = reassembledPayload.length;
//                    for (int i = 0; i < len;) {
//                        try {
//                            TlsPacket tls = TlsPacket.newPacket(reassembledPayload, i, len - i);
//                            System.out.println(tls);
//                            i += tls.length();
//                        } catch (IllegalRawDataException e) {
//                            e.printStackTrace();
//                        }
//                    }
                }
                else {
                    if (tcp.getPayload() != null && tcp.getPayload().length() != 0) {
                        TcpSession session = sessions.get(port);
                        session.getPackets(isToServer).add(tcp);
                    }
                }
            } catch (TimeoutException e) {
                continue;
            } catch (EOFException e) {
                break;
            }
        }

        handle.close();
    }

    private static byte[] doReassemble(
            List<TcpPacket> packets, long seqNumOffset, long lastSeqNum, int lastDataLen
    ) {
        // This cast is not safe.
        // The sequence number is unsigned int and so
        // (int) (lastSeqNum - seqNumOffset) may be negative.
        byte[] buffer = new byte[(int) (lastSeqNum - seqNumOffset) + lastDataLen];

        for (TcpPacket p: packets) {
            byte[] payload = p.getPayload().getRawData();
            long seq = p.getHeader().getSequenceNumberAsLong();
            System.arraycopy(payload, 0, buffer, (int) (seq - seqNumOffset), payload.length);
        }

        return buffer;
    }

    public static final class TcpSession {

        private final List<TcpPacket> packetsToServer = new ArrayList<TcpPacket>();
        private final List<TcpPacket> packetsToClient = new ArrayList<TcpPacket>();
        private long serverSeqNumOffset;
        private long clientSeqNumOffset;

        public List<TcpPacket> getPackets(boolean toServer) {
            if (toServer) {
                return packetsToServer;
            }
            else {
                return packetsToClient;
            }
        }

        public long getSeqNumOffset(boolean toServer) {
            if (toServer) {
                return clientSeqNumOffset;
            }
            else {
                return serverSeqNumOffset;
            }
        }

        public void setSeqNumOffset(boolean toServer, long seqNumOffset) {
            if (toServer) {
                this.clientSeqNumOffset = seqNumOffset;
            }
            else {
                this.serverSeqNumOffset = seqNumOffset;
            }
        }

    }

}
