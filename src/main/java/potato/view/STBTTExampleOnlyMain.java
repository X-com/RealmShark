package potato.view;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointBitmap;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;


public class STBTTExampleOnlyMain {
    private static ByteBuffer loadByteBufferFromResource(String resource) throws IOException {
        try(InputStream stream = STBTTExampleOnlyMain.class.getResourceAsStream(resource)) {
            byte[] bytes = new byte[1000000];
            stream.read(bytes);

            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();

            return buffer;
        }
    }

    public static void main(String[] args) throws IOException {
        File f = new File("assets/font/Inconsolata.ttf");
        System.out.println(f.exists());
        ByteBuffer data = loadByteBufferFromResource(f.getAbsolutePath());

        STBTTFontinfo font = STBTTFontinfo.create();
        stbtt_InitFont(font, data);

        IntBuffer bufWidth = BufferUtils.createIntBuffer(1);
        IntBuffer bufHeight = BufferUtils.createIntBuffer(1);
        ByteBuffer bitmap = stbtt_GetCodepointBitmap(font, 0, 1, 'a', bufWidth, bufHeight, null, null);

        System.out.println(bitmap);
    }
}
