package packets.incoming.ip;

import packets.Packet;
import packets.reader.BufferReader;
import util.Util;

import java.util.Arrays;

/**
 * Emits this class when IP changes happen on incoming packets.
 */
public class IpAddress extends Packet {

    /**
     * IP of incoming packets
     */
    public byte[] srcAddress;
    /**
     * IP of incoming packets as a single integer
     */
    public int srcAddressAsInt;
    /**
     * The IP address server name, i.e. USSouth, EUWest2.
     */
    public String ipAddressName;

    public IpAddress() {
    }

    public IpAddress(byte[] srcAddr) {
        super();
        srcAddress = srcAddr;
        srcAddressAsInt = Util.decodeInt(srcAddr);
        setName(srcAddressAsInt);
    }

    private void setName(int ip) {
        switch (ip) {
            case 921430924:
                ipAddressName = "USWest4";
                break;
            case 311434905:
                ipAddressName = "USWest3";
                break;
            case 911617968:
                ipAddressName = "USWest";
                break;
            case 916000068:
                ipAddressName = "USSouthWest";
                break;
            case 886033951:
                ipAddressName = "USSouth3";
                break;
            case 55737872:
                ipAddressName = "USSouth";
                break;
            case 586068087:
                ipAddressName = "USNorthWest";
                break;
            case 59571845:
                ipAddressName = "USMidWest2";
                break;
            case 59051031:
                ipAddressName = "USMidWest";
                break;
            case 878180357:
                ipAddressName = "USEast2";
                break;
            case 921362968:
                ipAddressName = "USEast";
                break;
            case 873486039:
                ipAddressName = "EUWest2";
                break;
            case 267205855:
                ipAddressName = "EUWest";
                break;
            case 599016312:
                ipAddressName = "EUSouthWest";
                break;
            case 312444280:
                ipAddressName = "EUNorth";
                break;
            case 314104494:
                ipAddressName = "EUEast";
                break;
            case 233592826:
                ipAddressName = "Australia";
                break;
            case 50369407:
                ipAddressName = "Asia";
                break;
            default:
                ipAddressName = "";
        }
    }

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
    }

    @Override
    public String toString() {
        return "IpAddress{" +
                "\n   srcAddress=" + String.format("%d.%d.%d.%d", Byte.toUnsignedInt(srcAddress[0]), Byte.toUnsignedInt(srcAddress[1]), Byte.toUnsignedInt(srcAddress[2]), Byte.toUnsignedInt(srcAddress[3])) +
                "\n   srcAddressAsInt=" + srcAddressAsInt +
                "\n   ipAddressName=" + ipAddressName;
    }
}
