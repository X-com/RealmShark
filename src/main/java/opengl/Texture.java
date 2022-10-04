package opengl;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class Texture {
    int textureId;

    public void bind() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void bind(int slot) {
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public Texture(String imagePath) {
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        BufferedImage image = readImage(imagePath);

//        BufferedImage heroImages = new BufferedImage(72 * 13, 72, heroes[0].getType());
//        for (int i = 0; i < 13; i++) {
//            BufferedImage image = heroes[i];
//            heroImages.getGraphics().drawImage(image, i * 72, 0, null);
//        }

        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        pixels = convertPixels(pixels);
        IntBuffer intBuffer = createByteBuffer(pixels);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, intBuffer);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void dispose() {
        glDeleteTextures(textureId);
    }

    public BufferedImage readImage(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            BufferedImage formatted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            formatted.getGraphics().drawImage(image, 0, 0, null);
            return formatted;
        } catch (IOException e) {
            throw new RuntimeException("Unable to read image: \"" + path + "\"");
        }
    }

    // converts argb to rgba
    public static int[] convertPixels(int[] pixels) {
        int[] rgba = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            int a = (argb & 0xff000000) >> 24;
            int r = (argb & 0xff0000) >> 16;
            int g = (argb & 0xff00) >> 8;
            int b = (argb & 0xff);
            rgba[i] = a << 24 | b << 16 | g << 8 | r;
        }
        return rgba;
    }

    public static IntBuffer createByteBuffer(int[] data) {
        IntBuffer result = ByteBuffer.allocateDirect(data.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        result.put(data).flip();
        return result;
    }
}
