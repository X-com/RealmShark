package assets;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Buffered image class used to crop images from the sprite atlas.
 */
public class ImageBuffer {

    private static final HashMap<Long, ImageIcon> outlinedImages = new HashMap<>();
    private static HashMap<Integer, BufferedImage> images = new HashMap<>();
    private static HashMap<Integer, float[]> colors = new HashMap<>();
//    private static final SpriteJson spriteJson = new SpriteJson();
    private static final SpriteFlatBuffer spriteFlatBuffer = new SpriteFlatBuffer();
    private static BufferedImage emptyImg;

    private static String[] spriteSheets = {"assets/sprites/groundTiles.png", "assets/sprites/characters.png", "assets/sprites/characters_masks.png", "assets/sprites/mapObjects.png"};
    private static BufferedImage[] bigImages = new BufferedImage[4];

    private static BufferedImage emptyImage() {
        if (emptyImg != null) return emptyImg;
        emptyImg = createTransparentBufferedImage(8, 8);
        return emptyImg;
    }

    /**
     * Image method used to get image from object id.
     *
     * @param id Type ID of the object.
     * @return Sprite of based of the object type ID.
     * @throws IOException Thrown if the sprite atlas file is missing.
     */
    public static BufferedImage getImage(int id) throws IOException {
        if (id <= 0) return null;
//        if (images.containsKey(id)) return images.get(id);
        String name = IdToAsset.getObjectTextureName(id, 0);
        if (name == null) return emptyImage();
        int index = IdToAsset.getObjectTextureIndex(id, 0);
        int[] spriteData = spriteFlatBuffer.getSpriteData(name, index);
        if (spriteData == null) return emptyImage();
        BufferedImage sprite = getSprite(spriteData);
        images.put(id, sprite);
        return sprite;
    }

    /**
     * Color method used to get most common color from tile id.
     *
     * @param id Type ID of the tile.
     * @return Most common color of the tile.
     */
    public static float[] getColor(int id) {
        if (id <= 0) return null;
        if (colors.containsKey(id)) return colors.get(id);
        if (!IdToAsset.tileIdExists(id)) return null;
        String name = IdToAsset.getTileTextureName(id, 0);
        if (name == null) return null;
        int index = IdToAsset.getTileTextureIndex(id, 0);
        float[] color = spriteFlatBuffer.getSpriteColor(name, index);
        colors.put(id, color);
        return color;
    }

    /**
     * Retrieves the sprite from the sprite atlas based on image location on the atlas.
     *
     * @param data The coordinate, size and altas ID of the sprite being requested.
     * @return Buffered image of the sprite cropped from a sprite atlas.
     * @throws IOException Thrown if the sprite atlas file is missing.
     */
    private static BufferedImage getSprite(int[] data) throws IOException { // data:{x, y, w, h, aId}
        if (data == null) return null;
        if (bigImages[data[4] - 1] == null) bigImages[data[4] - 1] = ImageIO.read(new File(spriteSheets[data[4] - 1]));

        return bigImages[data[4] - 1].getSubimage(data[0], data[1], data[2], data[3]);
    }

    /**
     * Resets the assets after loading new ones.
     */
    public static void clear() {
        images.clear();
        colors.clear();
        bigImages = new BufferedImage[4];
    }

    /**
     * Creates a transparent buffered image.
     *
     * @param width  Width of the transparent image.
     * @param height Height of the transparent image.
     * @return Transparent buffered image.
     */
    public static BufferedImage createTransparentBufferedImage(int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setBackground(new Color(0, true));
        graphics.clearRect(0, 0, width, height);
        graphics.dispose();

        return bufferedImage;
    }

    /**
     * Returns an image with outlines, empty image if image isn't found.
     * Image is buffered after outline is created.
     *
     * @param id ID of the image
     * @param size Size of the requested image
     * @return Outlined image with specific size
     */
    public static ImageIcon getOutlinedIcon(int id, int size) {
        long l = ((long) id << 10) + size;
        if (outlinedImages.containsKey(l)) return outlinedImages.get(l);

        BufferedImage img;
        if (id == -1) {
            img = ImageBuffer.getEmptyImg();
        } else {
            try {
                img = ImageBuffer.getImage(id);
            } catch (IOException e) {
                img = ImageBuffer.getEmptyImg();
            }
        }

        Image scaledInstance = img.getScaledInstance(size - 2, size - 2, Image.SCALE_DEFAULT);

        BufferedImage bimage = new BufferedImage(scaledInstance.getWidth(null) + 2, scaledInstance.getHeight(null) + 2, BufferedImage.TYPE_INT_ARGB);
        BufferedImage bimage2 = new BufferedImage(scaledInstance.getWidth(null) + 2, scaledInstance.getHeight(null) + 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(scaledInstance, 1, 1, null);
        bGr.dispose();

        int w = bimage.getWidth();
        int h = bimage.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = bimage.getRGB(x, y);
                int alpha = (rgb & 0xff000000) >> 24;
                if (alpha == 0) {
                    if (
                            ((bimage.getRGB(x + (x < (w-1) ? 1 : 0), y) & 0xff000000) >> 24) != 0 ||
                                    ((bimage.getRGB(x - (x != 0 ? 1 : 0), y) & 0xff000000) >> 24) != 0 ||
                                    ((bimage.getRGB(x, y + (y < (h-1) ? 1 : 0)) & 0xff000000) >> 24) != 0 ||
                                    ((bimage.getRGB(x, y - (y != 0 ? 1 : 0)) & 0xff000000) >> 24) != 0
                    ) {
                        bimage2.setRGB(x, y, 0xff000000);
                    }
                } else {
                    bimage2.setRGB(x, y, rgb);
                }
            }
        }
        ImageIcon i = new ImageIcon(bimage2);
        outlinedImages.put(l, i);

        return i;
    }

    public static ImageIcon getOutlinedIconWithGlow(int id, int size, Color glowColor, int glowSize) {
        // Check if we have a matching hash for the desired id, size, glowColor and glowSize
        long hashingNumber = (((long) id << 10) + size) ^ glowColor.getRGB() ^ glowSize;
        if (outlinedImages.containsKey(hashingNumber)) return outlinedImages.get(hashingNumber);

        BufferedImage img;
        if (id == -1) {
            img = ImageBuffer.getEmptyImg();
        } else {
            try {
                img = ImageBuffer.getImage(id);
            } catch (IOException e) {
                img = ImageBuffer.getEmptyImg();
            }
        }

        Image scaledInstance = img.getScaledInstance(size - 2, size - 2, Image.SCALE_SMOOTH);

        int baseW = scaledInstance.getWidth(null);
        int baseH = scaledInstance.getHeight(null);
        // Increase the size of the image by the outline and glow on all 4 edges
        int w = baseW + glowSize * 2 + 2;
        int h = baseH + glowSize * 2 + 2;

        // Base image with necessary padding added
        BufferedImage base = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gBase = base.createGraphics();
        gBase.drawImage(scaledInstance, glowSize + 1, glowSize + 1, null);
        gBase.dispose();

        // Step 2: Outline image
        BufferedImage outlineOnly = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        BufferedImage iconOnly = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int rgb = base.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;

                if (alpha == 0) {
                    // Check neighbors for edge
                    if (((base.getRGB(x + 1, y) >> 24) & 0xff) != 0 ||
                            ((base.getRGB(x - 1, y) >> 24) & 0xff) != 0 ||
                            ((base.getRGB(x, y + 1) >> 24) & 0xff) != 0 ||
                            ((base.getRGB(x, y - 1) >> 24) & 0xff) != 0) {
                        outlineOnly.setRGB(x, y, 0xFF000000); // black outline
                    }
                } else {
                    iconOnly.setRGB(x, y, rgb); // icon itself
                }
            }
        }

        // Step 3: Create glow around the outline only
        BufferedImage glow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = glowSize; y < h - glowSize; y++) {
            for (int x = glowSize; x < w - glowSize; x++) {
                int pixel = outlineOnly.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                int rgb = pixel & 0x00FFFFFF;
                if (alpha != 0 && rgb == 0x000000) {
                    for (int dy = -glowSize; dy <= glowSize; dy++) {
                        for (int dx = -glowSize; dx <= glowSize; dx++) {
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx >= 0 && ny >= 0 && nx < w && ny < h) {
                                int distSq = dx * dx + dy * dy;
                                if (distSq <= glowSize * glowSize) {
                                    int glowAlpha = Math.min(255, ((glowSize * glowSize - distSq) * 255) / (glowSize * glowSize));
                                    int existing = glow.getRGB(nx, ny);
                                    int existingAlpha = (existing >> 24) & 0xff;
                                    int combinedAlpha = Math.max(existingAlpha, glowAlpha / 2);
                                    glow.setRGB(nx, ny, (combinedAlpha << 24) | (glowColor.getRGB() & 0x00FFFFFF));
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mask the glow where icon has any alpha to prevent the glow bleeding into the icon
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int iconAlpha = (base.getRGB(x, y) >> 24) & 0xff;
                if (iconAlpha > 0) {
                    // Block glow at this pixel
                    glow.setRGB(x, y, 0x00000000); // fully transparent
                }
            }
        }

        // Step 4: Combine layers: glow + outlined icon + icon, in that order
        BufferedImage finalImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gFinal = finalImg.createGraphics();
        gFinal.drawImage(glow, 0, 0, null);        // glow under everything
        gFinal.drawImage(outlineOnly, 0, 0, null); // black outline
        gFinal.drawImage(iconOnly, 0, 0, null);    // icon on top
        gFinal.dispose();

        ImageIcon icon = new ImageIcon(finalImg);
        outlinedImages.put(hashingNumber, icon);
        return icon;
    }

    /**
     * Getter for transparent 8x8 image
     */
    public static BufferedImage getEmptyImg() {
        return emptyImage();
    }
}
