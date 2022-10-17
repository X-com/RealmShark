package opengl;

import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;


public class VertexBufferLayout {
    private int stride;
    private ArrayList<Element> elements;

    public VertexBufferLayout() {
        elements = new ArrayList<>();
    }

    public int getStride() {
        return stride;
    }

    public Element[] getElements() {
        return elements.toArray(new VertexBufferLayout.Element[0]);
    }

    public void addFloat(int count) {
        elements.add(new Element(GL_FLOAT, count, false, Float.BYTES));
        stride += count * 4;
    }

    public void addUnsignedInt(int count) {
        elements.add(new Element(GL_UNSIGNED_INT, count, false, Float.BYTES));
        stride += count * 4;
    }

    public void addUnsignedChar(int count) {
        elements.add(new Element(GL_UNSIGNED_BYTE, count, true, Byte.BYTES));
        stride += count;
    }

    public int size() {
        return elements.size();
    }

    public int getCount(int i) {
        return elements.get(i).count;
    }

    public int getType(int i) {
        return elements.get(i).type;
    }

    public boolean getNormalized(int i) {
        return elements.get(i).normalized;
    }

    public int getOffset(int i) {
        return elements.get(i).count * elements.get(i).typeSize;
    }

    @Override
    public String toString() {
        return "VertexBufferLayout{" +
                "\n   stride=" + stride +
                "\n   elements=" + Arrays.toString(getElements());
    }

    private class Element {
        private final int type;
        private final int count;
        private final boolean normalized;
        private final int typeSize;

        public Element(int type, int count, boolean normalized, int typeSize) {
            this.type = type;
            this.count = count;
            this.normalized = normalized;
            this.typeSize = typeSize;
        }

        @Override
        public String toString() {
            return "Element{" +
                    "\n   count=" + count +
                    "\n   type=" + type +
                    "\n   normalized=" + normalized;
        }
    }
}
