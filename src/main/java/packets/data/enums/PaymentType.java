package packets.data.enums;

import java.io.Serializable;

public enum PaymentType implements Serializable {
    Invalid(-1),
    Gold(0),
    Fame(1),
    GuildFame(2),
    FortuneTokens(3);

    private final int index;

    PaymentType(int i) {
        index = i;
    }

    public static PaymentType byOrdinal(int ord) {
        for (PaymentType o : PaymentType.values()) {
            if (o.index == ord) {
                return o;
            }
        }
        return null;
    }
}
