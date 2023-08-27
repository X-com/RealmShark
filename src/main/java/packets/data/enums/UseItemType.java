package packets.data.enums;


import java.io.Serializable;

/*
 * A class representation of the `useItemType` packet variable with helper functions.
 */
public enum UseItemType implements Serializable {
    INVALID(-1, "Invalid"),
    /**
     * Sent for consumables and single-use abilities such as spells and tomes.
     */
    DEFAULT(0, "Default"),
    /**
     * Sent to start using an ability such as a Ninja's shuriken.
     */
    START(1, "StartUse"),
    /**
     * Sent to end using an ability.
     */
    END(2, "EndUse");

    private final int useType;
    private final String useName;

    UseItemType(byte useType, String useName) {
        this.useType = useType;
        this.useName = useName;
    }

    UseItemType(int useType, String useName) {
        this.useType = (byte) useType & 0xff;
        this.useName = useName;
    }

    /**
     * Return an ItemUseType object from a given ID.
     *
     * @param useType The integer ID of the usage type code.
     * @return A UseItemType object or null if not found.
     */
    public static UseItemType fromCode(int useType) {
        // TODO: make this more efficient by not looping
        for (UseItemType index : UseItemType.values()) {
            if (index.useType == useType) return index;
        }
        return null;
    }

    /**
     * Return an ItemUseType object from a given ID.
     *
     * @param useType The byte ID of the usage type code.
     * @return A UseItemType object or null if not found.
     */
    public static UseItemType fromCode(byte useType) {
        return fromCode((int) useType);
    }

    /**
     * Return an ItemUseType object from a given name.
     *
     * @param useName The name of the usage type.
     * @return A UseItemType object or null if not found.
     */
    public static UseItemType fromName(String useName) {
        for (UseItemType index : UseItemType.values()) {
            if (index.useName.equals(useName)) return index;
        }
        return null;
    }

    public String toString() {
        return this.useName;
    }
}