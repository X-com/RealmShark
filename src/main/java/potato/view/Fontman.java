package potato.view;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTVertex;
import org.lwjgl.stb.STBTruetype;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;

import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;

public class Fontman {

    public static void main(String[] args) throws Exception {
        new Fontman().run();
    }

    private void run() throws IOException {
        InputStream input = Fontman.class.getResourceAsStream("/font/Inconsolata.ttf");
        if (input == null) {
            System.out.println("Fuck you");
            return;
        }
        byte[] file = new byte[input.available()];
        input.read(file);

        ByteBuffer buffer = BufferUtils.createByteBuffer(file.length);
        buffer = (ByteBuffer) buffer.put(file).flip();

        STBTTFontinfo info = STBTTFontinfo.create();
        stbtt_InitFont(info, buffer);

        STBTTVertex.Buffer buff = STBTruetype.stbtt_GetGlyphShape(info, 68);

        BufferedImage bi = new BufferedImage(1700, 1700, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.getGraphics();

        for(STBTTVertex v : buff) {
            System.out.println(v.x() +" "+ v.y());
            System.out.println(v.cx() +" "+ v.cy());

            g.setColor(Color.ORANGE);
            g.drawRect(v.x(), v.y(), 4, 4);
            g.drawRect(v.cx(), v.cy(), 2, 2);

            System.out.println();
        }

        g.dispose();

        try {
            ImageIO.write(bi, "PNG", new File("font.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
