//package potato.view;
//
//import com.util.ResourceLoader;
//import org.lwjgl.BufferUtils;
//import org.lwjgl.stb.STBTTFontinfo;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.nio.IntBuffer;
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//
//import static org.lwjgl.opengl.GL30.*;
//import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointBitmap;
//import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics;
//import static org.lwjgl.stb.STBTruetype.stbtt_GetFontNameString;
//import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
//import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
//import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight;
//
//
//public class Font {
//    public static final int PLAIN	= 0b00;
//    public static final int BOLD	= 0b01;
//    public static final int ITALIC	= 0b10;
//
//
//    private static class FontInfoIdentifier {
//        public final String family;
//        public final int style;
//
//        public FontInfoIdentifier(String family, int style) {
//            this.family = family;
//            this.style = style;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if(obj == this) {
//                return true;
//            } else if(obj == null) {
//                return false;
//            } else if(!(obj instanceof FontInfoIdentifier)) {
//                return false;
//            } else {
//                FontInfoIdentifier fii = (FontInfoIdentifier) obj;
//                return family.equalsIgnoreCase(fii.family) && style == fii.style;
//            }
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(family, style);
//        }
//    }
//
//
//
//    private static final Map<FontInfoIdentifier, STBTTFontinfo> fontInfoMap = new HashMap<>();
//    private static final Map<FontIdentifier, Font> fontMap = new HashMap<>();
//
//
//    private static String getFontNameString(STBTTFontinfo fontInfo, int nameID) {
//        ByteBuffer buf = stbtt_GetFontNameString(fontInfo, 3, 1, 0x0409, nameID);
//        byte[] bytes = new byte[buf.remaining()];
//        buf.get(bytes);
//        return new String(bytes, StandardCharsets.UTF_16);
//    }
//
//    private static STBTTFontinfo initFont(ByteBuffer data) {
//        ByteBuffer buffer = BufferUtils.createByteBuffer(STBTTFontinfo.SIZEOF);
//        STBTTFontinfo fontInfo = STBTTFontinfo.malloc();//create();
//        stbtt_InitFont(fontInfo, data);
//        return fontInfo;
//    }
//
//    private static FontInfoIdentifier createIdentifier(STBTTFontinfo fontInfo) {
//        String fontFamily = getFontNameString(fontInfo, 1);
//        String styleStr = getFontNameString(fontInfo, 2);
//        String[] styleSplit = styleStr.split(" ");
//
//        int style = PLAIN;
//        for(String s : styleSplit) {
//            if(s.equalsIgnoreCase("italic")) {
//                style |= ITALIC;
//            } else if(s.equalsIgnoreCase("bold")) {
//                style |= BOLD;
//            } else if(!s.equalsIgnoreCase("regular")) {
//                throw new RuntimeException("Unknown style modifier");
//            }
//        }
//
//        return new FontInfoIdentifier(fontFamily, style);
//    }
//
//    public static void loadFontFromFile(String file) throws IOException {
//        InputStream in = new FileInputStream(file);
//        byte[] bytes = in.readAllBytes();
//        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
//        buffer.put(bytes);
//        buffer.flip();
//
//        STBTTFontinfo fontInfo = initFont(buffer);
//        fontInfoMap.put(createIdentifier(fontInfo), fontInfo);
//    }
//
//    public static void loadFontFromResource(String resource) throws IOException {
//        ByteBuffer buffer = ResourceLoader.loadByteBufferFromResource(resource);
//        STBTTFontinfo fontInfo = initFont(buffer);
//        fontInfoMap.put(createIdentifier(fontInfo), fontInfo);
//    }
//
//    public static Font getFont(FontIdentifier fontIdentifier) {
//        Font result = fontMap.get(fontIdentifier);
//        if(result == null) {
//            STBTTFontinfo fontInfo = fontInfoMap.get(new FontInfoIdentifier(fontIdentifier.family, fontIdentifier.style));
//            if(fontInfo == null) {
//                throw new RuntimeException("Font not loaded yet");
//            }
//
//            result = new Font(fontInfo, fontIdentifier.family, fontIdentifier.style, fontIdentifier.size);
//            fontMap.put(fontIdentifier, result);
//        }
//
//        return result;
//    }
//
//
//
//
//    public final STBTTFontinfo fontInfo;
//
//    private final String family;
//    private final int style;
//    private final int size;
//
//    public final float scale;
//
//    private final Map<Character, Glyph> glyphMap = new HashMap<>();
//
//    public Font(STBTTFontinfo fontInfo, String family, int style, int size) {
//        this.fontInfo = fontInfo;
//
//        this.family = family;
//        this.style = style;
//        this.size = size;
//
//        this.scale = stbtt_ScaleForPixelHeight(fontInfo, size);
//    }
//
//    public String getFamily() {
//        return this.family;
//    }
//
//    public int getStyle() {
//        return this.style;
//    }
//
//    public int getSize() {
//        return this.size;
//    }
//
//    public Glyph getCodepointGlyph(char codepoint) {
//        Glyph result = glyphMap.get(codepoint);
//        if(result == null) {
//            IntBuffer bufWidth = BufferUtils.createIntBuffer(1);
//            IntBuffer bufHeight = BufferUtils.createIntBuffer(1);
//            IntBuffer bufBearingX = BufferUtils.createIntBuffer(1);
//            IntBuffer bufAdvance = BufferUtils.createIntBuffer(1);
//            IntBuffer bufAscent = BufferUtils.createIntBuffer(1);
//            IntBuffer bufDescent = BufferUtils.createIntBuffer(1);
//            IntBuffer bufLineGap = BufferUtils.createIntBuffer(1);
//
//            stbtt_GetCodepointHMetrics(fontInfo, codepoint, bufAdvance, bufBearingX);
//            stbtt_GetFontVMetrics(fontInfo, bufAscent, bufDescent, bufLineGap);
//            ByteBuffer bitmap = stbtt_GetCodepointBitmap(fontInfo, 0, scale, codepoint, bufWidth, bufHeight, null, null);
//
//            int width = bufWidth.get();
//            int height = bufHeight.get();
//            int bearingX = bufBearingX.get();
//            int advance = bufAdvance.get();
//            int ascent = bufAscent.get();
//            int descent = bufDescent.get();
//            int lineGap = bufLineGap.get();
//
//
//            int textureID = glGenTextures();
//            glBindTexture(GL_TEXTURE_2D, textureID);
//
//            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
//            glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, width, height, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
//            glGenerateMipmap(GL_TEXTURE_2D);
//
//            result = new Glyph(textureID, width, height, bearingX, advance, ascent, descent, lineGap);
//            glyphMap.put(codepoint, result);
//        }
//
//        return result;
//    }
//}