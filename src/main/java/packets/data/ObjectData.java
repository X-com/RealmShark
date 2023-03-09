package packets.data;

import packets.reader.BufferReader;
import assets.AssetMissingException;
import assets.IdToAsset;

public class ObjectData {
    /**
     * The type of this object
     */
    public int objectType;
    /**
     * The status of this object
     */
    public ObjectStatusData status;

    /**
     * Deserializer method to extract data from the buffer.
     *
     * @param buffer Data that needs deserializing.
     * @return Returns this object after deserializing.
     */
    public ObjectData deserialize(BufferReader buffer) {
        objectType = buffer.readUnsignedShort();
        status = new ObjectStatusData().deserialize(buffer);

        return this;
    }

    @Override
    public String toString() {
        String name = "";
        try {
            name = IdToAsset.objectName(objectType);
        } catch (AssetMissingException e) {
            e.printStackTrace();
        }
        return "\n    " + (name.equals("") ? ("objectType=" + objectType) : ("objectType=" + objectType + " " + name)) +
                status;
    }
}
